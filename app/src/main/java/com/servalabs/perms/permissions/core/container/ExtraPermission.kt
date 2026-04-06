package com.servalabs.perms.permissions.core.container

import com.servalabs.perms.permissions.core.Permission
import com.servalabs.perms.permissions.core.PermissionGroup
import com.servalabs.perms.permissions.core.features.PermissionTag


data class ExtraPermission(
    override val id: Permission.Id,
    override val requestingApps: List<PermissionAppRef> = emptyList(),
    override val declaringApps: List<PermissionAppRef> = emptyList(),
    override val tags: Collection<PermissionTag>,
    override val groupIds: Collection<PermissionGroup.Id>,
) : BasePermission() {

    override val grantingApps: List<PermissionAppRef> by lazy {
        requestingApps.filter { it.status.isGranted }
    }
}
