package com.servalabs.perms.settings.core

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import com.servalabs.perms.apps.ui.details.AppDetailsFilterOptions
import com.servalabs.perms.apps.ui.list.AppsFilterOptions
import com.servalabs.perms.apps.ui.list.AppsSortOptions
import com.servalabs.perms.common.datastore.createValue
import com.servalabs.perms.common.datastore.kotlinxReader
import com.servalabs.perms.common.datastore.kotlinxWriter
import com.servalabs.perms.common.debug.logging.logTag
import com.servalabs.perms.common.theming.ThemeColor
import com.servalabs.perms.watcher.core.WatcherScope
import com.servalabs.perms.watcher.ui.dashboard.WatcherFilterOptions
import com.servalabs.perms.common.theming.ThemeMode
import com.servalabs.perms.common.theming.ThemeStyle
import com.servalabs.perms.permissions.ui.details.PermissionDetailsFilterOptions
import com.servalabs.perms.permissions.ui.list.PermsFilterOptions
import com.servalabs.perms.permissions.ui.list.PermsSortOptions
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(
    name = "settings_core",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "settings_core"))
    },
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
)

@Singleton
class GeneralSettings @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) {

    private val dataStore get() = context.dataStore

    val launchCount = dataStore.createValue("core.stats.launches", 0)

    val isOnboardingFinished = dataStore.createValue("core.onboarding.finished", false)

    val themeMode = dataStore.createValue(
        key = stringPreferencesKey("core.ui.theme.mode"),
        reader = kotlinxReader(json, ThemeMode.SYSTEM, fallbackToDefault = true),
        writer = kotlinxWriter(json),
    )
    val themeStyle = dataStore.createValue(
        key = stringPreferencesKey("core.ui.theme.style"),
        reader = kotlinxReader(json, ThemeStyle.DEFAULT, fallbackToDefault = true),
        writer = kotlinxWriter(json),
    )
    val themeColor = dataStore.createValue(
        key = stringPreferencesKey("core.ui.theme.color"),
        reader = kotlinxReader(json, ThemeColor.BLUE, fallbackToDefault = true),
        writer = kotlinxWriter(json),
    )

    val appsFilterOptions = dataStore.createValue(
        key = stringPreferencesKey("apps.list.options.filter"),
        reader = kotlinxReader(json, AppsFilterOptions(), fallbackToDefault = true),
        writer = kotlinxWriter(json),
    )
    val appsSortOptions = dataStore.createValue(
        key = stringPreferencesKey("apps.list.options.sort"),
        reader = kotlinxReader(json, AppsSortOptions(), fallbackToDefault = true),
        writer = kotlinxWriter(json),
    )

    val appDetailsFilterOptions = dataStore.createValue(
        key = stringPreferencesKey("apps.details.options.filter"),
        reader = kotlinxReader(json, AppDetailsFilterOptions(), fallbackToDefault = true),
        writer = kotlinxWriter(json),
    )

    val permissionsFilterOptions = dataStore.createValue(
        key = stringPreferencesKey("permissions.list.options.filter"),
        reader = kotlinxReader(json, PermsFilterOptions(), fallbackToDefault = true),
        writer = kotlinxWriter(json),
    )
    val permissionsSortOptions = dataStore.createValue(
        key = stringPreferencesKey("permissions.list.options.sort"),
        reader = kotlinxReader(json, PermsSortOptions(), fallbackToDefault = true),
        writer = kotlinxWriter(json),
    )

    val permissionDetailsFilterOptions = dataStore.createValue(
        key = stringPreferencesKey("permissions.details.options.filter"),
        reader = kotlinxReader(json, PermissionDetailsFilterOptions(), fallbackToDefault = true),
        writer = kotlinxWriter(json),
    )

    val isWatcherEnabled = dataStore.createValue("watcher.enabled", false)
    val watcherScope = dataStore.createValue(
        key = stringPreferencesKey("watcher.scope"),
        reader = kotlinxReader(json, WatcherScope.NON_SYSTEM, fallbackToDefault = true),
        writer = kotlinxWriter(json),
    )
    val watcherFilterOptions = dataStore.createValue(
        key = stringPreferencesKey("watcher.dashboard.options.filter"),
        reader = kotlinxReader(json, WatcherFilterOptions(), fallbackToDefault = true),
        writer = kotlinxWriter(json),
    )
    val isWatcherNotificationsEnabled = dataStore.createValue("watcher.notifications.enabled", true)
    val isWatcherNotifyOnlyOnGained = dataStore.createValue("watcher.notifications.only.gained", true)
    val isWatcherBatteryHintDismissed = dataStore.createValue("watcher.battery.hint.dismissed", false)
    val watcherLastSuccessfulPollAt = dataStore.createValue("watcher.last.successful.poll.at", 0L)
    val watcherRetentionDays = dataStore.createValue("watcher.retention.days", 30)

    val watcherPollingIntervalHours = dataStore.createValue("watcher.polling.interval.hours", 4)

    val lastDiffedSnapshotId = dataStore.createValue("watcher.lastDiffedSnapshotId", null as String?)

    val ipcParallelisation = dataStore.createValue("core.ipc.parallelisation", 0)

    companion object {
        internal val TAG = logTag("Core", "Settings")
    }
}
