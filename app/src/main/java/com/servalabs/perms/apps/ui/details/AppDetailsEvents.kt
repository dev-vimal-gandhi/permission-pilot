package com.servalabs.perms.apps.ui.details

import com.servalabs.perms.apps.core.Pkg
import com.servalabs.perms.permissions.core.features.PermissionAction

sealed class AppDetailsEvents {
    data class ShowAppSystemDetails(val pkg: Pkg) : AppDetailsEvents()
    data class PermissionEvent(val permAction: PermissionAction) : AppDetailsEvents()
    data class ShowFilterDialog(val options: AppDetailsFilterOptions) : AppDetailsEvents()
}
