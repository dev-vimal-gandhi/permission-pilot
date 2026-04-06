package com.servalabs.perms.main.ui.onboarding

import dagger.hilt.android.lifecycle.HiltViewModel
import com.servalabs.perms.common.coroutine.DispatcherProvider
import com.servalabs.perms.common.debug.logging.logTag
import com.servalabs.perms.common.navigation.Nav
import com.servalabs.perms.common.uix.ViewModel4
import com.servalabs.perms.settings.core.GeneralSettings
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val generalSettings: GeneralSettings,
) : ViewModel4(dispatcherProvider) {

    fun finishOnboarding() = launch {
        generalSettings.isOnboardingFinished.value(true)
        navTo(Nav.Tab.Apps, popUpTo = Nav.Main.Onboarding, inclusive = true)
    }

    companion object {
        private val TAG = logTag("Onboarding", "VM")
    }
}
