package com.servalabs.perms.permissions.ui.list

import com.servalabs.perms.permissions.core.features.PermissionAction

sealed class PermissionListEvent {
    data class ShowFilterDialog(val options: PermsFilterOptions) : PermissionListEvent()

    data class ShowSortDialog(val options: PermsSortOptions) : PermissionListEvent()

    data class PermissionEvent(val permAction: PermissionAction) : PermissionListEvent()
}
