package com.servalabs.perms.settings.ui.general

import dagger.hilt.android.lifecycle.HiltViewModel
import com.servalabs.perms.common.coroutine.DispatcherProvider
import com.servalabs.perms.common.debug.logging.logTag
import com.servalabs.perms.common.navigation.Nav
import com.servalabs.perms.common.theming.ThemeColor
import com.servalabs.perms.common.theming.ThemeMode
import com.servalabs.perms.common.theming.ThemeStyle
import com.servalabs.perms.common.uix.ViewModel4
import com.servalabs.perms.common.upgrade.UpgradeRepo
import com.servalabs.perms.settings.core.GeneralSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class GeneralSettingsViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val generalSettings: GeneralSettings,
    private val upgradeRepo: UpgradeRepo,
) : ViewModel4(dispatcherProvider) {

    val themeMode: Flow<ThemeMode> = generalSettings.themeMode.flow
    val themeStyle: Flow<ThemeStyle> = generalSettings.themeStyle.flow
    val themeColor: Flow<ThemeColor> = generalSettings.themeColor.flow
    val isPro: StateFlow<Boolean> = upgradeRepo.upgradeInfo
        .map { it.isPro }
        .stateIn(vmScope, SharingStarted.Eagerly, upgradeRepo.upgradeInfo.value.isPro)

    fun setThemeMode(mode: ThemeMode) = launch {
        generalSettings.themeMode.value(mode)
    }

    fun setThemeStyle(style: ThemeStyle) = launch {
        generalSettings.themeStyle.value(style)
    }

    fun setThemeColor(color: ThemeColor) = launch {
        generalSettings.themeColor.value(color)
    }

    val ipcParallelisation: Flow<Int> = generalSettings.ipcParallelisation.flow

    fun setIpcParallelisation(value: Int) = launch {
        generalSettings.ipcParallelisation.value(value)
    }

    fun onUpgrade() {
        navTo(Nav.Main.Upgrade)
    }

    companion object {
        private val TAG = logTag("Settings", "General", "VM")
    }
}
