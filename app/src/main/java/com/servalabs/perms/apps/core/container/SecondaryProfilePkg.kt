package com.servalabs.perms.apps.core.container

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.graphics.drawable.Drawable
import android.os.Process
import android.os.UserHandle
import com.servalabs.perms.apps.core.AppRepo
import com.servalabs.perms.apps.core.GET_UNINSTALLED_PACKAGES_COMPAT
import com.servalabs.perms.apps.core.Pkg
import com.servalabs.perms.apps.core.features.AccessibilityService
import com.servalabs.perms.apps.core.features.InstallerInfo
import com.servalabs.perms.apps.core.features.Installed
import com.servalabs.perms.apps.core.features.InternetAccess
import com.servalabs.perms.apps.core.features.SecondaryPkg
import com.servalabs.perms.apps.core.features.UsesPermission
import com.servalabs.perms.apps.core.features.determineSpecialPermissions
import com.servalabs.perms.apps.core.features.getInstallerInfo
import com.servalabs.perms.apps.core.features.getSpecialPermissionStatuses
import com.servalabs.perms.apps.core.isSystemApp
import com.servalabs.perms.common.IPCFunnel
import com.servalabs.perms.common.debug.logging.Logging.Priority.*
import com.servalabs.perms.common.debug.logging.log
import com.servalabs.perms.permissions.core.Permission
import com.servalabs.perms.permissions.core.known.APerm
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class SecondaryProfilePkg(
    override val packageInfo: PackageInfo,
    override val userHandle: UserHandle,
    override val installerInfo: InstallerInfo,
    val launcherAppInfo: ApplicationInfo,
    val extraPermissions: Collection<UsesPermission>,
    private val specialPermissionStatuses: Map<Permission.Id, UsesPermission.Status> = emptyMap(),
) : BasePkg(), SecondaryPkg {

    override val id: Pkg.Id = Pkg.Id(Pkg.Name(packageInfo.packageName), userHandle)


    @Volatile private var _label: String? = null
    @Volatile private var _resolvingLabel = false
    override fun getLabel(context: Context): String {
        _label?.let { return it }
        if (_resolvingLabel) return id.pkgName.value
        _resolvingLabel = true
        try {
            val pm = context.packageManager
            val newLabel = try {
                val loadedLabel = launcherAppInfo.loadLabel(pm).toString()
                pm.getUserBadgedLabel(loadedLabel, userHandle).toString()
            } catch (_: Exception) {
                null
            }
                ?: twins.firstNotNullOfOrNull { it.getLabel(context) }
                ?: super.getLabel(context)
                ?: id.pkgName.value
            _label = newLabel
            return newLabel
        } catch (e: Exception) {
            val fallback = id.pkgName.value
            _label = fallback
            return fallback
        } finally {
            _resolvingLabel = false
        }
    }

    private var _resolvingIcon = false
    override fun getIcon(context: Context): Drawable? {
        if (_resolvingIcon) return null
        _resolvingIcon = true
        return try {
            val pm = context.packageManager
            try {
                val loadedIcon = launcherAppInfo.loadIcon(pm)
                if (loadedIcon != null) {
                    pm.getUserBadgedIcon(loadedIcon, userHandle)
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
                ?: twins.firstNotNullOfOrNull { it.getIcon(context) }
                ?: super.getIcon(context)
        } finally {
            _resolvingIcon = false
        }
    }

    override var siblings: Collection<Pkg> = emptyList()
    override var twins: Collection<Installed> = emptyList()

    override val requestedPermissions: Collection<UsesPermission> by lazy {
        val base = packageInfo.requestedPermissions?.map { permissionId ->
            val permId = Permission.Id(permissionId)
            val overrideStatus = specialPermissionStatuses[permId]
            if (overrideStatus != null) {
                UsesPermission.WithState(id = permId, flags = null, overrideStatus = overrideStatus)
            } else {
                UsesPermission.Unknown(id = permId)
            }
        } ?: emptyList()
        val acsPermissions = accessibilityServices.map {
            UsesPermission.WithState(
                id = APerm.BIND_ACCESSIBILITY_SERVICE.id,
                flags = null
            )
        }
        val deviceAdminPermissions = deviceAdmins.map {
            UsesPermission.WithState(
                id = APerm.BIND_DEVICE_ADMIN.id,
                flags = null
            )
        }
        base + acsPermissions + deviceAdminPermissions
    }

    override val declaredPermissions: Collection<PermissionInfo> by lazy {
        packageInfo.permissions?.toSet() ?: emptyList()
    }

    override val internetAccess: InternetAccess = InternetAccess.UNKNOWN

    override val isSystemApp: Boolean = (applicationInfo?.isSystemApp ?: true) || twins.any { it.isSystemApp }

    override fun toString(): String = "SecondaryProfilePkg(packageName=$packageName, userHandle=$userHandle)"
}

suspend fun getSecondaryProfilePkgs(ipcFunnel: IPCFunnel): Collection<BasePkg> = coroutineScope {

    val profiles = ipcFunnel.userManager.userProfiles()

    if (profiles.size < 2) return@coroutineScope emptySet()

    log(AppRepo.TAG, INFO) { "Found multiple user profiles: $profiles" }
    val extraProfiles = profiles - Process.myUserHandle()

    suspend fun determineForHandle(userHandle: UserHandle): Collection<BasePkg> {
        val launcherInfos = try {
            ipcFunnel.launcherApps.getActivityList(null, userHandle)
        } catch (e: SecurityException) {
            log(AppRepo.TAG, ERROR) { "Failed to retrieve activity list for $userHandle" }
            emptyList()
        }

        return launcherInfos.mapNotNull { lai ->
            val appInfo = lai.applicationInfo

            var pkgInfo = ipcFunnel.packageManager.getPackageArchiveInfo(
                appInfo.packageName,
                GET_UNINSTALLED_PACKAGES_COMPAT
            )

            if (pkgInfo == null) {
                log(AppRepo.TAG, VERBOSE) { "Failed to get info from packagemanager for $appInfo" }
                pkgInfo =
                    ipcFunnel.packageManager.getPackageArchiveInfo(appInfo.sourceDir, PackageManager.GET_PERMISSIONS)
            }

            if (pkgInfo == null) {
                log(AppRepo.TAG, ERROR) { "Failed to read APK: ${appInfo.sourceDir}" }
                return@mapNotNull null
            }

            SecondaryProfilePkg(
                packageInfo = pkgInfo,
                installerInfo = pkgInfo.getInstallerInfo(ipcFunnel),
                launcherAppInfo = appInfo,
                userHandle = userHandle,
                extraPermissions = pkgInfo.determineSpecialPermissions(ipcFunnel, uidOverride = appInfo.uid),
                specialPermissionStatuses = pkgInfo.getSpecialPermissionStatuses(ipcFunnel, uidOverride = appInfo.uid),
            ).also { log(AppRepo.TAG) { "PKG[profile=${userHandle}}: $it" } }
        }
    }

    extraProfiles
        .map { userHandle ->
            async { determineForHandle(userHandle) }
        }
        .awaitAll()
        .flatten()
}
