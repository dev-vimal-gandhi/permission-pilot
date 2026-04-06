package com.servalabs.perms.watcher.ui.dashboard

import dagger.hilt.android.lifecycle.HiltViewModel
import com.servalabs.perms.apps.core.Pkg
import com.servalabs.perms.common.coroutine.DispatcherProvider
import com.servalabs.perms.common.debug.logging.Logging.Priority.WARN
import com.servalabs.perms.common.debug.logging.asLog
import com.servalabs.perms.common.debug.logging.log
import com.servalabs.perms.common.debug.logging.logTag
import com.servalabs.perms.common.flow.combine
import com.servalabs.perms.common.navigation.Nav
import com.servalabs.perms.common.room.dao.PermissionChangeDao
import com.servalabs.perms.common.room.entity.PermissionChangeEntity
import com.servalabs.perms.common.room.entity.TriggerReason
import com.servalabs.perms.common.uix.ViewModel4
import com.servalabs.perms.common.upgrade.UpgradeRepo
import com.servalabs.perms.settings.core.GeneralSettings
import com.servalabs.perms.watcher.core.PermissionDiff
import com.servalabs.perms.watcher.core.WatcherManager
import com.servalabs.perms.watcher.core.WatcherNotificationCapability
import com.servalabs.perms.watcher.core.WatcherNotifications
import com.servalabs.perms.watcher.core.WatcherWorkScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class WatcherDashboardViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val generalSettings: GeneralSettings,
    private val changeDao: PermissionChangeDao,
    private val upgradeRepo: UpgradeRepo,
    private val capability: WatcherNotificationCapability,
    private val watcherWorkScheduler: WatcherWorkScheduler,
    private val watcherManager: WatcherManager,
    private val watcherNotifications: WatcherNotifications,
    private val batteryCapability: WatcherBatteryCapability,
    private val json: Json,
) : ViewModel4(dispatcherProvider) {

    data class State(
        val isWatcherEnabled: Boolean = false,
        val isPro: Boolean = false,
        val reports: List<WatcherReportItem> = emptyList(),
        val showNotificationPermissionCard: Boolean = false,
        val canRequestNotificationPermission: Boolean = false,
        val showBatteryOptimizationCard: Boolean = false,
        val refreshPhase: WatcherManager.Phase? = null,
        val filterOptions: WatcherFilterOptions = WatcherFilterOptions(),
        val hasUnseen: Boolean = false,
        val totalReportCount: Int = 0,
        val lockedReportCount: Int = 0,
    )

    private val notificationsAvailable = MutableStateFlow(capability.areNotificationsEnabled())
    private val showBatteryCard = MutableStateFlow(computeBatteryCard())
    private val searchTerm = MutableStateFlow<String?>(null)

    fun refreshNotificationState() {
        notificationsAvailable.value = capability.areNotificationsEnabled()
    }

    private fun computeBatteryCard(): Boolean {
        val isOptimized = !batteryCapability.isBatteryOptimizationIgnored()
        if (!isOptimized) return false
        val lastPoll = generalSettings.watcherLastSuccessfulPollAt.valueBlocking
        val intervalMs = generalSettings.watcherPollingIntervalHours.valueBlocking.toLong() * 3_600_000L
        val staleSince = System.currentTimeMillis() - lastPoll
        return lastPoll > 0L && staleSince > intervalMs * 4
    }

    fun refreshBatteryState() {
        showBatteryCard.value = computeBatteryCard()
    }

    fun dismissBatteryHint() = launch {
        generalSettings.isWatcherBatteryHintDismissed.value(true)
    }

    val state = combine(
        generalSettings.isWatcherEnabled.flow,
        upgradeRepo.upgradeInfo.map { it.isPro },
        changeDao.getAll(),
        generalSettings.isWatcherNotificationsEnabled.flow,
        notificationsAvailable,
        watcherManager.phase,
        searchTerm,
        generalSettings.watcherFilterOptions.flow,
        showBatteryCard,
        generalSettings.isWatcherBatteryHintDismissed.flow,
    ) { isEnabled, isPro, entities, notificationsEnabled, notifAvailable, phase, search, filterOpts, batteryCardVisible, batteryDismissed ->
        val allItems = entities.map { it.toItem() }
        val filteredItems = allItems
            .filter { filterOpts.matches(it) }
            .filter {
                val term = search?.lowercase() ?: return@filter true
                if (it.packageName.value.lowercase().contains(term)) return@filter true
                if (it.appLabel?.lowercase()?.contains(term) == true) return@filter true
                false
            }

        val duplicateLabels = filteredItems
            .filter { it.appLabel != null }
            .groupBy { it.appLabel }
            .filterValues { items -> items.distinctBy { it.packageName }.size > 1 }
            .keys

        val allReports = filteredItems.map { item ->
            item.copy(showPkgName = item.appLabel in duplicateLabels)
        }

        val reports = if (!isPro && allReports.size > FREE_REPORT_LIMIT) {
            allReports.take(FREE_REPORT_LIMIT)
        } else {
            allReports
        }
        val lockedCount = if (!isPro) (allReports.size - reports.size).coerceAtLeast(0) else 0

        State(
            isWatcherEnabled = isEnabled,
            isPro = isPro,
            reports = reports,
            showNotificationPermissionCard = isEnabled && notificationsEnabled && !notifAvailable && isPro,
            canRequestNotificationPermission = capability.isRuntimePermissionDenied(),
            showBatteryOptimizationCard = isEnabled && batteryCardVisible && !batteryDismissed,
            refreshPhase = phase,
            filterOptions = filterOpts,
            hasUnseen = reports.any { !it.isSeen },
            totalReportCount = allItems.size,
            lockedReportCount = lockedCount,
        )
    }.asStateFlow(State())

    fun toggleWatcher() = launch {
        val current = generalSettings.isWatcherEnabled.value()
        log(TAG) { "Toggling watcher: $current -> ${!current}" }
        generalSettings.isWatcherEnabled.value(!current)
        watcherWorkScheduler.ensureScheduled()
    }

    fun onReportClicked(item: WatcherReportItem) = launch {
        if (!upgradeRepo.upgradeInfo.value.isPro) {
            log(TAG) { "Not pro, navigating to upgrade instead of detail" }
            navTo(Nav.Main.Upgrade)
            return@launch
        }
        changeDao.markSeen(item.id)
        watcherNotifications.cancelForPackage(item.packageName)
        navTo(Nav.Watcher.ReportDetail(item.id))
    }

    fun goToUpgrade() {
        navTo(Nav.Main.Upgrade)
    }

    fun refreshNow() = launch {
        log(TAG) { "refreshNow()" }
        try {
            watcherManager.scanDiffAndPrune(TriggerReason.MANUAL_REFRESH)
            generalSettings.watcherLastSuccessfulPollAt.value(System.currentTimeMillis())
        } catch (e: Exception) {
            log(TAG, WARN) { "Refresh failed: ${e.asLog()}" }
        }
    }

    fun markAllSeen() = launch {
        changeDao.markAllSeen()
        watcherNotifications.cancelAllChangeNotifications()
    }

    fun disableNotifications() = launch {
        generalSettings.isWatcherNotificationsEnabled.value(false)
    }

    fun goToSettings() {
        navTo(Nav.Settings.Index)
    }

    fun onSearchInputChanged(term: String?) {
        log(TAG) { "onSearchInputChanged(term=$term)" }
        searchTerm.value = term
    }

    fun updateFilterOptions(action: (WatcherFilterOptions) -> WatcherFilterOptions) = launch {
        generalSettings.watcherFilterOptions.update { action(it) }
    }

    private fun PermissionChangeEntity.toItem(): WatcherReportItem {
        val diff = runCatching {
            json.decodeFromString<PermissionDiff>(changesJson)
        }.onFailure { e ->
            log(TAG, WARN) { "Failed to deserialize changesJson for report $id: ${e.asLog()}" }
        }.getOrNull()

        return WatcherReportItem(
            id = id,
            packageName = packageName,
            appLabel = appLabel,
            versionName = versionName,
            previousVersionName = previousVersionName,
            eventType = eventType,
            detectedAt = detectedAt,
            isSeen = isSeen,
            hasAddedPermissions = diff?.let { it.addedPermissions.isNotEmpty() || it.addedDeclared.isNotEmpty() } ?: false,
            hasLostPermissions = diff?.let { it.removedPermissions.isNotEmpty() || it.removedDeclared.isNotEmpty() } ?: false,
            gainedCount = diff?.gainedCount ?: 0,
            lostCount = diff?.lostCount ?: 0,
        )
    }

    companion object {
        private const val FREE_REPORT_LIMIT = 5
        private val TAG = logTag("Watcher", "Dashboard", "VM")
    }
}
