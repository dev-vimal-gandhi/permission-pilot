package com.servalabs.perms.export.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import com.servalabs.perms.apps.core.AppInfo
import com.servalabs.perms.apps.core.AppRepo
import com.servalabs.perms.common.coroutine.DispatcherProvider
import com.servalabs.perms.common.debug.logging.Logging.Priority.WARN
import com.servalabs.perms.common.debug.logging.log
import com.servalabs.perms.common.debug.logging.logTag
import com.servalabs.perms.common.flow.SingleEventFlow
import com.servalabs.perms.common.navigation.Nav
import com.servalabs.perms.common.uix.ViewModel4
import com.servalabs.perms.common.upgrade.UpgradeRepo
import com.servalabs.perms.export.core.AppExportConfig
import com.servalabs.perms.export.core.ExportEngine
import com.servalabs.perms.export.core.ExportFormat
import com.servalabs.perms.export.core.ExportSelectionStore
import com.servalabs.perms.export.core.ExportWriter
import com.servalabs.perms.export.core.PermissionExportConfig
import com.servalabs.perms.permissions.core.Permission
import com.servalabs.perms.permissions.core.PermissionRepo
import com.servalabs.perms.permissions.core.container.BasePermission
import com.servalabs.perms.permissions.core.permissions
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.TimeSource

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class ExportViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val dispatcherProvider: DispatcherProvider,
    @ApplicationContext private val context: Context,
    private val appRepo: AppRepo,
    private val permissionRepo: PermissionRepo,
    private val exportEngine: ExportEngine,
    private val exportWriter: ExportWriter,
    private val exportSelectionStore: ExportSelectionStore,
    upgradeRepo: UpgradeRepo,
) : ViewModel4(dispatcherProvider = dispatcherProvider) {

    val isPro: StateFlow<Boolean> = upgradeRepo.upgradeInfo
        .map { it.isPro }
        .stateIn(vmScope, SharingStarted.Eagerly, upgradeRepo.upgradeInfo.value.isPro)

    private var mode: ExportMode = restoreMode() ?: ExportMode.Apps(emptyList())

    sealed class ExportMode {
        data class Apps(val ids: List<Pair<String, Int>>) : ExportMode()
        data class Permissions(val ids: List<String>) : ExportMode()
    }

    private val appConfig = MutableStateFlow(AppExportConfig())
    private val permConfig = MutableStateFlow(PermissionExportConfig())
    private val exportResult = MutableStateFlow<ExportResult?>(null)
    private val preview = MutableStateFlow<String?>(null)
    private val isPreviewLoading = MutableStateFlow(false)

    sealed class ExportResult {
        data object InProgress : ExportResult()
        data class Success(
            val uri: Uri,
            val fileName: String,
            val fileSize: Long,
            val duration: Duration,
        ) : ExportResult()

        data class Error(val throwable: Throwable) : ExportResult()
    }

    val events = SingleEventFlow<Event>()

    sealed interface Event {
        data class LaunchSaf(val fileName: String) : Event
    }

    data class State(
        val mode: ExportMode,
        val isPro: Boolean,
        val itemCount: Int,
        val effectiveItemCount: Int,
        val isFreeLimited: Boolean,
        val preview: String? = null,
        val isPreviewLoading: Boolean = false,
        val appConfig: AppExportConfig? = null,
        val permConfig: PermissionExportConfig? = null,
        val exportResult: ExportResult? = null,
    )

    val state: StateFlow<State?> = combine(
        appRepo.appData,
        permissionRepo.state,
        appConfig,
        permConfig,
        combine(exportResult, preview, isPreviewLoading) { r, p, l -> Triple(r, p, l) },
    ) { appDataState, permState, appCfg, permCfg, (result, previewText, previewLoading) ->
        val allApps = (appDataState as? AppRepo.AppDataState.Ready)?.apps ?: emptyList()
        val allPerms = (permState as? PermissionRepo.State.Ready)?.permissions ?: emptyList()
        val pro = isPro.value

        when (val m = mode) {
            is ExportMode.Apps -> {
                val selectedApps = resolveApps(allApps, m.ids)
                val effectiveCfg = enforceFreemium(appCfg, pro)
                val effectiveApps = if (!pro) selectedApps.take(FREE_EXPORT_LIMIT) else selectedApps

                State(
                    mode = m,
                    isPro = pro,
                    itemCount = selectedApps.size,
                    effectiveItemCount = effectiveApps.size,
                    isFreeLimited = !pro && selectedApps.size > FREE_EXPORT_LIMIT,
                    preview = previewText,
                    isPreviewLoading = previewLoading,
                    appConfig = effectiveCfg,
                    exportResult = result,
                )
            }

            is ExportMode.Permissions -> {
                val selectedPerms = resolvePermissions(allPerms, m.ids)
                val effectiveCfg = enforceFreemium(permCfg, pro)
                val effectivePerms = if (!pro) selectedPerms.take(FREE_EXPORT_LIMIT) else selectedPerms

                State(
                    mode = m,
                    isPro = pro,
                    itemCount = selectedPerms.size,
                    effectiveItemCount = effectivePerms.size,
                    isFreeLimited = !pro && selectedPerms.size > FREE_EXPORT_LIMIT,
                    preview = previewText,
                    isPreviewLoading = previewLoading,
                    permConfig = effectiveCfg,
                    exportResult = result,
                )
            }
        }
    }.asStateFlow()

    init {
        // Async preview generation: debounce config changes, run on IO
        @OptIn(FlowPreview::class)
        combine(appConfig, permConfig, appRepo.appData, permissionRepo.state) { appCfg, permCfg, appData, permState ->
            appCfg to permCfg to (appData to permState)
        }.debounce(200L).map {
            regeneratePreview()
        }.launchInViewModel()
    }

    private suspend fun regeneratePreview() {
        isPreviewLoading.value = true
        val result = withContext(dispatcherProvider.IO) {
            runCatching {
                val allApps = (appRepo.appData.first() as? AppRepo.AppDataState.Ready)?.apps ?: emptyList()
                val allPerms = (permissionRepo.state.first() as? PermissionRepo.State.Ready)?.permissions ?: emptyList()
                val pro = isPro.value

                when (val m = mode) {
                    is ExportMode.Apps -> {
                        val selectedApps = resolveApps(allApps, m.ids)
                        val effectiveApps = if (!pro) selectedApps.take(FREE_EXPORT_LIMIT) else selectedApps
                        val effectiveCfg = enforceFreemium(appConfig.value, pro)
                        exportEngine.previewApps(effectiveApps, allPerms, effectiveCfg)
                    }
                    is ExportMode.Permissions -> {
                        val selectedPerms = resolvePermissions(allPerms, m.ids)
                        val effectivePerms = if (!pro) selectedPerms.take(FREE_EXPORT_LIMIT) else selectedPerms
                        val effectiveCfg = enforceFreemium(permConfig.value, pro)
                        exportEngine.previewPermissions(effectivePerms, effectiveCfg)
                    }
                }
            }
        }
        preview.value = result.getOrElse { e ->
            log(TAG, WARN) { "Preview generation failed: $e" }
            null
        }
        isPreviewLoading.value = false
    }

    fun init(route: Nav.Export.Config) {
        // If already restored from SavedStateHandle, skip re-init
        if (handle.contains(KEY_MODE)) {
            log(TAG) { "Already initialized from SavedStateHandle, skipping" }
            return
        }

        val ids = exportSelectionStore.consume(route.token) ?: emptyList()
        mode = when (route.mode) {
            "apps" -> ExportMode.Apps(ids.map { id ->
                val parts = id.split(":")
                parts[0] to parts[1].toInt()
            })

            "permissions" -> ExportMode.Permissions(ids)
            else -> {
                log(TAG, WARN) { "Unknown export mode: ${route.mode}" }
                ExportMode.Apps(emptyList())
            }
        }
        // Persist for process death restoration
        handle[KEY_MODE] = route.mode
        handle[KEY_IDS] = ArrayList(ids)
        log(TAG) { "Initialized with mode=$mode, ${ids.size} items" }
    }

    private fun restoreMode(): ExportMode? {
        val modeStr = handle.get<String>(KEY_MODE) ?: return null
        val ids = handle.get<ArrayList<String>>(KEY_IDS) ?: return null
        log(TAG) { "Restoring from SavedStateHandle: mode=$modeStr, ${ids.size} items" }
        return when (modeStr) {
            "apps" -> ExportMode.Apps(ids.map { id ->
                val parts = id.split(":")
                parts[0] to parts[1].toInt()
            })
            "permissions" -> ExportMode.Permissions(ids)
            else -> null
        }
    }

    fun updateAppConfig(transform: (AppExportConfig) -> AppExportConfig) {
        appConfig.value = transform(appConfig.value)
    }

    fun updatePermConfig(transform: (PermissionExportConfig) -> PermissionExportConfig) {
        permConfig.value = transform(permConfig.value)
    }

    fun startExport() {
        val format = when (mode) {
            is ExportMode.Apps -> appConfig.value.format
            is ExportMode.Permissions -> permConfig.value.format
        }
        val fileName = "serva-perms-export.${format.extension}"
        events.tryEmit(Event.LaunchSaf(format.mimeType, fileName))
    }

    fun onSafResult(uri: Uri?) = launch {
        if (uri == null) return@launch // User cancelled

        exportResult.value = ExportResult.InProgress
        val startMark = TimeSource.Monotonic.markNow()

        val allApps = (appRepo.appData.first() as? AppRepo.AppDataState.Ready)?.apps ?: emptyList()
        val allPerms = permissionRepo.permissions.first()
        val pro = isPro.value

        val result = when (val m = mode) {
            is ExportMode.Apps -> {
                val apps = resolveApps(allApps, m.ids).let {
                    if (!pro) it.take(FREE_EXPORT_LIMIT) else it
                }
                val cfg = enforceFreemium(appConfig.value, pro)
                exportEngine.exportApps(apps, allPerms, cfg)
            }

            is ExportMode.Permissions -> {
                val perms = resolvePermissions(allPerms, m.ids).let {
                    if (!pro) it.take(FREE_EXPORT_LIMIT) else it
                }
                val cfg = enforceFreemium(permConfig.value, pro)
                exportEngine.exportPermissions(perms, cfg)
            }
        }

        result.onSuccess { content ->
            exportWriter.write(uri, content).onSuccess {
                val elapsed = startMark.elapsedNow()
                val fileName = getFileName(uri) ?: "export"
                val fileSize = getFileSize(uri)
                exportResult.value = ExportResult.Success(
                    uri = uri,
                    fileName = fileName,
                    fileSize = fileSize,
                    duration = elapsed,
                )
            }.onFailure { e ->
                log(TAG, WARN) { "Write failed: $e" }
                exportResult.value = ExportResult.Error(e)
            }
        }.onFailure { e ->
            log(TAG, WARN) { "Export failed: $e" }
            exportResult.value = ExportResult.Error(e)
        }
    }

    private fun getFileName(uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
        }
    }

    private fun getFileSize(uri: Uri): Long {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (cursor.moveToFirst() && sizeIndex >= 0) cursor.getLong(sizeIndex) else -1L
        } ?: -1L
    }

    fun resetExportResult() {
        exportResult.value = null
    }

    fun onUpgrade() {
        navTo(Nav.Main.Upgrade)
    }

    private fun resolveApps(
        allApps: List<AppInfo>,
        ids: List<Pair<String, Int>>,
    ): List<AppInfo> {
        val idSet = ids.toSet()
        return allApps.filter { (it.pkgName.value to it.userHandleId) in idSet }
    }

    private fun resolvePermissions(
        allPerms: Collection<BasePermission>,
        ids: List<String>,
    ): Collection<BasePermission> {
        val idSet = ids.map { Permission.Id(it) }.toSet()
        return allPerms.filter { it.id in idSet }
    }

    private fun enforceFreemium(config: AppExportConfig, isPro: Boolean): AppExportConfig {
        return if (isPro) config else config.copy(format = ExportFormat.MARKDOWN)
    }

    private fun enforceFreemium(config: PermissionExportConfig, isPro: Boolean): PermissionExportConfig {
        return if (isPro) config else config.copy(format = ExportFormat.MARKDOWN)
    }

    companion object {
        private const val FREE_EXPORT_LIMIT = 5
        private const val KEY_MODE = "export_mode"
        private const val KEY_IDS = "export_ids"
        private val TAG = logTag("Export", "VM")
    }
}
