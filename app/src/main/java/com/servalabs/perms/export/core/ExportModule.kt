package com.servalabs.perms.export.core

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.servalabs.perms.export.core.formatter.CsvFormatter
import com.servalabs.perms.export.core.formatter.ExportFormatter
import com.servalabs.perms.export.core.formatter.JsonFormatter
import com.servalabs.perms.export.core.formatter.MarkdownFormatter

@Module
@InstallIn(SingletonComponent::class)
object ExportModule {

    @Provides
    fun provideFormatters(
        markdown: MarkdownFormatter,
        csv: CsvFormatter,
        json: JsonFormatter,
    ): Map<ExportFormat, ExportFormatter> = mapOf(
        ExportFormat.MARKDOWN to markdown,
        ExportFormat.CSV to csv,
        ExportFormat.JSON to json,
    )
}
