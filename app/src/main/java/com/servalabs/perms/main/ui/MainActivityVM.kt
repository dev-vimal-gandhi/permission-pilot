package com.servalabs.perms.main.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import android.content.Intent
import com.servalabs.perms.common.coroutine.DispatcherProvider
import com.servalabs.perms.common.debug.logging.log
import com.servalabs.perms.common.flow.SingleEventFlow
import com.servalabs.perms.common.navigation.Nav
import com.servalabs.perms.common.navigation.NavigationDestination
import com.servalabs.perms.common.room.dao.PermissionChangeDao
import com.servalabs.perms.common.theming.ThemeState
import com.servalabs.perms.settings.core.themeState
import com.servalabs.perms.settings.core.themeStateBlocking
import com.servalabs.perms.common.uix.ViewModel2
import com.servalabs.perms.common.upgrade.UpgradeRepo
import com.servalabs.perms.settings.core.GeneralSettings
import com.servalabs.perms.watcher.core.WatcherNotifications
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import javax.inject.Inject

@HiltViewModel
class MainActivityVM @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    @Suppress("UNUSED_PARAMETER") handle: SavedStateHandle,
    private val upgradeRepo: UpgradeRepo,
    private val generalSettings: GeneralSettings,
    changeDao: PermissionChangeDao,
) : ViewModel2(dispatcherProvider = dispatcherProvider) {

    private val _readyState = MutableStateFlow(false)
    val readyState: StateFlow<Boolean> = _readyState

    val themeState: StateFlow<ThemeState> = generalSettings.themeState.stateIn(
        vmScope,
        SharingStarted.Eagerly,
        generalSettings.themeStateBlocking,
    )

    val isOnboardingFinished: Boolean
        get() = generalSettings.isOnboardingFinished.valueBlocking

    val deepLinkNav = SingleEventFlow<NavigationDestination>()

    init {
        upgradeRepo.upgradeInfo
            .take(1)
            .onEach { _readyState.value = true }
            .launchInViewModel()
    }

    fun increaseLaunchCount() = launch {
        generalSettings.launchCount.update {
            log { "LaunchCount was $it" }
            it + 1
        }
    }

    fun handleIntent(intent: Intent?) {
        val reportId = intent?.getLongExtra(WatcherNotifications.EXTRA_REPORT_ID, -1L) ?: -1L
        if (reportId > 0) {
            log { "Deep-link to watcher report: $reportId" }
            launch {
                deepLinkNav.emit(Nav.Watcher.ReportDetail(reportId))
            }
        }
    }
}
