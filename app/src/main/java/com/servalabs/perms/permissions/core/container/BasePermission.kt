package com.servalabs.perms.permissions.core.container

import com.servalabs.perms.permissions.core.Permission

sealed class BasePermission : Permission {

    abstract val requestingApps: List<PermissionAppRef>
    abstract val grantingApps: List<PermissionAppRef>
    abstract val declaringApps: List<PermissionAppRef>

}
