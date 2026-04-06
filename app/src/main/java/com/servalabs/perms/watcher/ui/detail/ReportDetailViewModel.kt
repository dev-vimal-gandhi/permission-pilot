package com.servalabs.perms.watcher.ui.detail

import android.content.Context
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import com.servalabs.perms.common.coroutine.DispatcherProvider
import com.servalabs.perms.common.debug.logging.Logging.Priority.WARN
import com.servalabs.perms.common.debug.logging.asLog
import com.servalabs.perms.common.debug.logging.log
import com.servalabs.perms.common.debug.logging.logTag
import com.servalabs.perms.common.room.dao.PermissionChangeDao
import com.servalabs.perms.common.room.dao.SnapshotPkgDao
import com.servalabs.perms.common.uix.ViewModel4
import com.servalabs.perms.apps.core.Pkg
import com.servalabs.perms.apps.core.known.AKnownPkg
import com.servalabs.perms.apps.core.getPermissionInfo2
import com.servalabs.perms.permissions.core.Permission
import com.servalabs.perms.permissions.core.ProtectionType
import com.servalabs.perms.permissions.core.ProtectionFlag
import com.servalabs.perms.permissions.core.protectionFlagsCompat
import com.servalabs.perms.permissions.core.protectionTypeCompat
import com.servalabs.perms.common.navigation.Nav
import com.servalabs.perms.watcher.core.PermissionDiff
import com.servalabs.perms.watcher.core.WatcherEventType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json

enum class GrantType { RUNTIME, INSTALL_TIME, SPECIAL_ACCESS, UNKNOWN }

data class EnrichedPermission(
    val id: String,
    val label: String?,
    val description: String?,
    val grantType: GrantType,
)

@HiltViewModel(assistedFactory = ReportDetailViewModel.Factory::class)
class ReportDetailViewModel @AssistedInject constructor(
    @Assisted private val reportId: Long,
    dispatcherProvider: DispatcherProvider,
    private val changeDao: PermissionChangeDao,
    private val snapshotPkgDao: SnapshotPkgDao,
    @ApplicationContext private val context: Context,
    private val json: Json,
) : ViewModel4(dispatcherProvider) {

    @AssistedFactory
    interface Factory {
        fun create(reportId: Long): ReportDetailViewModel
    }

    data class State(
        val packageName: Pkg.Name = Pkg.Name(""),
        val appLabel: String? = null,
        val eventType: WatcherEventType = WatcherEventType.INSTALL,
        val versionName: String? = null,
        val previousVersionName: String? = null,
        val versionCode: Long? = null,
        val previousVersionCode: Long? = null,
        val installerLabel: String? = null,
        val isSystemApp: Boolean = false,
        val userHandleId: Int = 0,
        val detectedAt: Long = 0,
        val diff: PermissionDiff? = null,
        val permissionInfoMap: Map<String, EnrichedPermission> = emptyMap(),
        val isLoading: Boolean = true,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    init {
        launch {
            val entity = changeDao.getById(reportId)
            if (entity != null) {
                val diff = try {
                    json.decodeFromString<PermissionDiff>(entity.changesJson)
                } catch (e: Exception) {
                    log(TAG, WARN) { "Failed to deserialize changesJson for report $reportId: ${e.asLog()}" }
                    null
                }

                val snapshotPkg = entity.sourceSnapshotId?.let { sid ->
                    snapshotPkgDao.getPkgByName(sid, entity.packageName, entity.userHandleId)
                }

                val installerLabel = snapshotPkg?.installerPkgName?.let { name ->
                    AKnownPkg.values.firstOrNull { it.id.pkgName == name }?.labelRes
                        ?.let { context.getString(it) }
                        ?: name.value.substringAfterLast('.')
                }

                val permissionInfoMap = resolvePermissions(diff)

                _state.value = State(
                    packageName = entity.packageName,
                    appLabel = entity.appLabel,
                    userHandleId = entity.userHandleId,
                    eventType = entity.eventType,
                    versionName = entity.versionName,
                    previousVersionName = entity.previousVersionName,
                    versionCode = entity.versionCode,
                    previousVersionCode = entity.previousVersionCode,
                    installerLabel = installerLabel,
                    isSystemApp = snapshotPkg?.isSystemApp ?: false,
                    detectedAt = entity.detectedAt,
                    diff = diff,
                    permissionInfoMap = permissionInfoMap,
                    isLoading = false,
                )
            } else {
                _state.value = State(isLoading = false)
            }
        }
    }

    private fun resolvePermissions(diff: PermissionDiff?): Map<String, EnrichedPermission> {
        if (diff == null) return emptyMap()
        val allIds = buildSet {
            addAll(diff.addedPermissions)
            addAll(diff.removedPermissions)
            addAll(diff.addedDeclared)
            addAll(diff.removedDeclared)
            addAll(diff.grantChanges.map { it.permissionId })
        }

        val pm = context.packageManager
        return allIds.associateWith { permId ->
            val permissionId = Permission.Id(permId)
            val permObj = object : Permission {
                override val id = permissionId
            }

            val label = permObj.getLabel(context)
            val description = permObj.getDescription(context)

            val permInfo = pm.getPermissionInfo2(permissionId)
            val grantType = if (permInfo != null) {
                when {
                    permInfo.protectionTypeCompat == ProtectionType.DANGEROUS -> GrantType.RUNTIME
                    permInfo.protectionFlagsCompat.contains(ProtectionFlag.APPOP) -> GrantType.SPECIAL_ACCESS
                    else -> GrantType.INSTALL_TIME
                }
            } else {
                GrantType.UNKNOWN
            }

            EnrichedPermission(
                id = permId,
                label = label,
                description = description,
                grantType = grantType,
            )
        }
    }

    fun onViewApp() {
        val current = _state.value
        navTo(Nav.Details.AppDetails(current.packageName.value, current.userHandleId, current.appLabel))
    }

    companion object {
        private val TAG = logTag("Watcher", "ReportDetail", "VM")
    }
}
