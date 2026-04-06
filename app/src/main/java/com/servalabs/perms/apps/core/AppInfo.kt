package com.servalabs.perms.apps.core

import com.servalabs.perms.apps.core.features.BatteryOptimization
import com.servalabs.perms.apps.core.features.InternetAccess
import com.servalabs.perms.apps.core.features.UsesPermission
import com.servalabs.perms.common.room.entity.PkgType
import java.time.Instant

data class AppInfo(
    val pkgName: Pkg.Name,
    val userHandleId: Int,
    val label: String,
    val versionName: String?,
    val versionCode: Long,
    val isSystemApp: Boolean,
    val installerPkgName: Pkg.Name?,
    val apiTargetLevel: Int?,
    val apiCompileLevel: Int?,
    val apiMinimumLevel: Int?,
    val internetAccess: InternetAccess,
    val batteryOptimization: BatteryOptimization,
    val installedAt: Instant?,
    val updatedAt: Instant?,
    val requestedPermissions: List<PermissionUse>,
    val declaredPermissionCount: Int,
    val pkgType: PkgType,
    val twinCount: Int,
    val siblingCount: Int,
    val hasAccessibilityServices: Boolean,
    val hasDeviceAdmin: Boolean,
    val allInstallerPkgNames: List<Pkg.Name>,
    val sharedUserId: String? = null,
    val hasManifestFlags: Boolean? = null,
)

data class PermissionUse(
    val permissionId: String,
    val status: UsesPermission.Status,
)
