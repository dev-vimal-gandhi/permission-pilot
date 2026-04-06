package com.servalabs.perms.permissions.ui.details

import com.servalabs.perms.apps.core.Pkg
import com.servalabs.perms.permissions.core.features.PermissionAction

sealed class PermissionDetailsEvents {
    data class ShowAppSystemDetails(val pkg: Pkg) : PermissionDetailsEvents()
    data class PermissionEvent(val permAction: PermissionAction) : PermissionDetailsEvents()
    data class ShowFilterDialog(val options: PermissionDetailsFilterOptions) : PermissionDetailsEvents()
}
