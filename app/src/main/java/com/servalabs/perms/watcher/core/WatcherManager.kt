package com.servalabs.perms.watcher.core

import com.servalabs.perms.apps.core.AppRepo
import com.servalabs.perms.common.coroutine.AppScope
import com.servalabs.perms.common.debug.logging.Logging.Priority.WARN
import com.servalabs.perms.common.debug.logging.asLog
import com.servalabs.perms.common.debug.logging.log
import com.servalabs.perms.common.debug.logging.logTag
import com.servalabs.perms.common.room.entity.TriggerReason
import com.servalabs.perms.settings.core.GeneralSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatcherManager @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val appRepo: AppRepo,
    private val watcherDiffRunner: WatcherDiffRunner,
    private val generalSettings: GeneralSettings,
    private val watcherWorkScheduler: WatcherWorkScheduler,
) {

    init {
        generalSettings.isWatcherEnabled.flow.onEach {
            try {
                watcherWorkScheduler.ensureScheduled()
            } catch (e: Exception) {
                log(TAG, WARN) { "Failed to ensure schedule: ${e.asLog()}" }
            }
        }.launchIn(appScope)
    }

    enum class Phase {
        SCANNING,
        CHECKING_CHANGES,
    }

    private val _phase = MutableStateFlow<Phase?>(null)
    val phase: StateFlow<Phase?> = _phase.asStateFlow()

    private val mutex = Mutex()

    suspend fun scanDiffAndPrune(reason: TriggerReason) = mutex.withLock {
        log(TAG) { "scanDiffAndPrune(reason=$reason)" }
        try {
            _phase.value = Phase.SCANNING
            appRepo.scanAndSave(reason)
            _phase.value = Phase.CHECKING_CHANGES
            watcherDiffRunner.processNewSnapshots()
            val anchorId = generalSettings.lastDiffedSnapshotId.value()
            if (anchorId != null) appRepo.pruneSnapshotsBefore(anchorId)
        } finally {
            _phase.value = null
        }
    }

    suspend fun processChanges() = mutex.withLock {
        log(TAG) { "processChanges()" }
        try {
            _phase.value = Phase.CHECKING_CHANGES
            watcherDiffRunner.processNewSnapshots()
        } finally {
            _phase.value = null
        }
    }

    companion object {
        private val TAG = logTag("Watcher", "Manager")
    }
}
