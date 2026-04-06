package com.servalabs.perms.export.core

import androidx.annotation.StringRes
import com.servalabs.perms.R

enum class ExportFormat(
    val extension: String,
    val mimeType: String,
    @StringRes val labelRes: Int,
) {
    MARKDOWN("md", "text/markdown", R.string.export_format_markdown_label),
    CSV("csv", "text/csv", R.string.export_format_csv_label),
    JSON("json", "application/json", R.string.export_format_json_label),
}
