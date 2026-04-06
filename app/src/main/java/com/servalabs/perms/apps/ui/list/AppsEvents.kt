package com.servalabs.perms.apps.ui.list

import com.servalabs.perms.apps.core.Pkg
import com.servalabs.perms.permissions.core.Permission
import com.servalabs.perms.permissions.core.features.PermissionAction

sealed class AppsEvents {
    data class ShowFilterDialog(val options: AppsFilterOptions) : AppsEvents()

    data class ShowSortDialog(val options: AppsSortOptions) : AppsEvents()

    data class ShowPermissionSnackbar(val permission: Permission) : AppsEvents()

    data class ShowAppSystemDetails(val pkg: Pkg) : AppsEvents()

    data class PermissionEvent(val permAction: PermissionAction) : AppsEvents()
}
