package com.servalabs.perms.permissions.core.container

import android.content.pm.PermissionInfo
import com.servalabs.perms.permissions.core.*
import com.servalabs.perms.permissions.core.features.PermissionTag


data class DeclaredPermission(
    val permissionInfo: PermissionInfo,
    override val requestingApps: List<PermissionAppRef> = emptyList(),
    override val declaringApps: List<PermissionAppRef> = emptyList(),
    override val tags: Collection<PermissionTag>,
    override val groupIds: Collection<PermissionGroup.Id>,
) : BasePermission() {

    override val id: Permission.Id
        get() = Permission.Id(permissionInfo.name)

    override val grantingApps: List<PermissionAppRef> by lazy {
        requestingApps.filter { it.status.isGranted }
    }

    val protectionType: ProtectionType by lazy { permissionInfo.protectionTypeCompat }

    val protectionFlags: Set<ProtectionFlag> by lazy { permissionInfo.protectionFlagsCompat }
}
