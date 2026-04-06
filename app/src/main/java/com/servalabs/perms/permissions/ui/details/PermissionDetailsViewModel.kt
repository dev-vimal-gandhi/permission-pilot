package com.servalabs.perms.permissions.ui.details

import android.annotation.SuppressLint
import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import com.servalabs.perms.apps.core.Pkg
import com.servalabs.perms.apps.core.features.UsesPermission
import com.servalabs.perms.common.coroutine.DispatcherProvider
import com.servalabs.perms.common.debug.logging.log
import com.servalabs.perms.common.debug.logging.logTag
import com.servalabs.perms.common.navigation.Nav
import com.servalabs.perms.common.uix.ViewModel4
import com.servalabs.perms.permissions.core.Permission
import com.servalabs.perms.permissions.core.PermissionRepo
import com.servalabs.perms.permissions.core.ProtectionFlag
import com.servalabs.perms.permissions.core.ProtectionType
import com.servalabs.perms.permissions.core.container.BasePermission
import com.servalabs.perms.permissions.core.container.DeclaredPermission
import com.servalabs.perms.permissions.core.container.PermissionAppRef
import com.servalabs.perms.permissions.core.features.Highlighted
import com.servalabs.perms.permissions.core.features.InstallTimeGrant
import com.servalabs.perms.permissions.core.features.ManifestDoc
import com.servalabs.perms.permissions.core.features.NotNormalPerm
import com.servalabs.perms.permissions.core.features.PermissionTag
import com.servalabs.perms.permissions.core.features.RuntimeGrant
import com.servalabs.perms.permissions.core.features.SpecialAccess
import com.servalabs.perms.permissions.core.permissions
import com.servalabs.perms.common.room.entity.PkgType
import com.servalabs.perms.settings.core.GeneralSettings
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class PermissionDetailsViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    @ApplicationContext private val context: Context,
    private val permissionsRepo: PermissionRepo,
    private val generalSettings: GeneralSettings,
) : ViewModel4(dispatcherProvider = dispatcherProvider) {

    var permId: Permission.Id = Permission.Id("")
        private set
    var initialLabel: String? = null
        private set

    fun init(route: Nav.Details.PermissionDetails) {
        permId = Permission.Id(route.permissionId)
        initialLabel = route.permLabel
    }

    data class DeclaringAppItem(
        val pkgName: Pkg.Name,
        val pkg: Pkg,
        val label: String,
        val isSystemApp: Boolean,
        val userHandle: Int,
    )

    data class RequestingAppItem(
        val pkgName: Pkg.Name,
        val pkg: Pkg,
        val label: String,
        val isSystemApp: Boolean,
        val status: UsesPermission.Status,
        val userHandle: Int,
    )

    data class State(
        val label: String = "",
        val permissionId: String = "",
        val permission: BasePermission? = null,
        val description: String? = null,
        val fullDescription: String? = null,
        val protectionType: ProtectionType? = null,
        val protectionFlags: List<ProtectionFlag> = emptyList(),
        val protectionFlagOverflow: Int = 0,
        val tags: List<PermissionTag> = emptyList(),
        val grantedUserCount: Int = 0,
        val totalUserCount: Int = 0,
        val grantedSystemCount: Int = 0,
        val totalSystemCount: Int = 0,
        val otherProfileCount: Int = 0,
        val uninstalledCount: Int = 0,
        val declaringApps: List<DeclaringAppItem> = emptyList(),
        val requestingApps: List<RequestingAppItem> = emptyList(),
        val isLoading: Boolean = true,
        val filterOptions: PermissionDetailsFilterOptions = PermissionDetailsFilterOptions(),
    )

    val state by lazy {
        combine(
            permissionsRepo.permissions.map { perms -> perms.singleOrNull { it.id == permId } },
            generalSettings.permissionDetailsFilterOptions.flow,
        ) { perm, filterOpts ->
            if (perm == null) return@combine State(label = initialLabel ?: permId.value, isLoading = true)

            val declaring = perm.declaringApps.map { ref ->
                DeclaringAppItem(
                    pkgName = ref.pkgName,
                    pkg = Pkg.Container(Pkg.Id(ref.pkgName)),
                    label = ref.label,
                    isSystemApp = ref.isSystemApp,
                    userHandle = ref.userHandleId,
                )
            }

            val filteredRefs = perm.requestingApps
                .filter { ref -> filterOpts.filters.any { filter -> filter.matches(ref) } }

            val statusRank = mapOf(
                UsesPermission.Status.GRANTED to 0,
                UsesPermission.Status.GRANTED_IN_USE to 1,
                UsesPermission.Status.DENIED to 2,
                UsesPermission.Status.UNKNOWN to 3,
            )

            val requesting = filteredRefs
                .map { ref ->
                    RequestingAppItem(
                        pkgName = ref.pkgName,
                        pkg = Pkg.Container(Pkg.Id(ref.pkgName)),
                        label = ref.label,
                        isSystemApp = ref.isSystemApp,
                        status = ref.status,
                        userHandle = ref.userHandleId,
                    )
                }
                .sortedWith(
                    compareBy<RequestingAppItem> { statusRank[it.status] ?: 99 }
                        .thenBy { it.isSystemApp }
                        .thenBy { it.label }
                )

            val allProtectionFlags = (perm as? DeclaredPermission)?.protectionFlags
                ?.sortedBy { it.ordinal } ?: emptyList()

            State(
                label = perm.id.value.split(".").lastOrNull() ?: perm.id.value,
                permissionId = perm.id.value,
                permission = perm,
                description = perm.getLabel(context),
                fullDescription = perm.getDescription(context),
                protectionType = (perm as? DeclaredPermission)?.protectionType,
                protectionFlags = allProtectionFlags.take(3),
                protectionFlagOverflow = (allProtectionFlags.size - 3).coerceAtLeast(0),
                tags = perm.tags.sortedBy { tag ->
                    when (tag) {
                        is RuntimeGrant -> 0
                        is SpecialAccess -> 1
                        is InstallTimeGrant -> 2
                        is ManifestDoc -> 3
                        is Highlighted -> 4
                        is NotNormalPerm -> 5
                    }
                },
                grantedUserCount = filteredRefs.count { it.pkgType == PkgType.PRIMARY && !it.isSystemApp && it.status.isGranted },
                totalUserCount = filteredRefs.count { it.pkgType == PkgType.PRIMARY && !it.isSystemApp },
                grantedSystemCount = filteredRefs.count { it.pkgType == PkgType.PRIMARY && it.isSystemApp && it.status.isGranted },
                totalSystemCount = filteredRefs.count { it.pkgType == PkgType.PRIMARY && it.isSystemApp },
                otherProfileCount = perm.requestingApps.count {
                    it.pkgType == PkgType.SECONDARY_PROFILE || it.pkgType == PkgType.SECONDARY_USER
                },
                uninstalledCount = perm.requestingApps.count { it.pkgType == PkgType.UNINSTALLED },
                declaringApps = declaring,
                requestingApps = requesting,
                isLoading = false,
                filterOptions = filterOpts,
            )
        }.asStateFlow()
    }

    fun updateFilterOptions(action: (PermissionDetailsFilterOptions) -> PermissionDetailsFilterOptions) = launch {
        generalSettings.permissionDetailsFilterOptions.update { action(it) }
    }

    fun onAppClicked(pkgName: Pkg.Name, userHandle: Int) {
        log(TAG) { "Navigating to app $pkgName" }
        navTo(Nav.Details.AppDetails(pkgName = pkgName.value, userHandle = userHandle))
    }

    companion object {
        private val TAG = logTag("PermissionDetails", "VM")
    }
}
