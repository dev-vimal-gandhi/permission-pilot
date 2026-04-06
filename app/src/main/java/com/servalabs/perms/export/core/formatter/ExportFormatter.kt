package com.servalabs.perms.export.core.formatter

import com.servalabs.perms.apps.core.AppInfo
import com.servalabs.perms.export.core.AppExportConfig
import com.servalabs.perms.export.core.ExportFormat
import com.servalabs.perms.export.core.PermissionExportConfig
import com.servalabs.perms.export.core.ResolvedPermissionInfo

interface ExportFormatter {

    val format: ExportFormat

    fun formatApps(
        apps: List<AppInfo>,
        config: AppExportConfig,
        permissionLookup: (String) -> ResolvedPermissionInfo?,
    ): String

    fun formatPermissions(
        permissions: List<ResolvedPermissionInfo>,
        config: PermissionExportConfig,
    ): String
}
