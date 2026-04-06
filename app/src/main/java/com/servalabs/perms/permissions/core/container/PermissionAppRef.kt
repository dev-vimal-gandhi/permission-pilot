package com.servalabs.perms.permissions.core.container

import com.servalabs.perms.apps.core.Pkg
import com.servalabs.perms.apps.core.features.UsesPermission
import com.servalabs.perms.common.room.entity.PkgType

data class PermissionAppRef(
    val pkgName: Pkg.Name,
    val userHandleId: Int,
    val label: String,
    val isSystemApp: Boolean,
    val status: UsesPermission.Status,
    val pkgType: PkgType,
)
