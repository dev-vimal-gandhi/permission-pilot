package com.servalabs.perms.settings.ui.index

import dagger.hilt.android.lifecycle.HiltViewModel
import com.servalabs.perms.common.PrivacyPolicy
import com.servalabs.perms.common.WebpageTool
import com.servalabs.perms.common.coroutine.DispatcherProvider
import com.servalabs.perms.common.debug.logging.logTag
import com.servalabs.perms.common.uix.ViewModel4
import javax.inject.Inject

@HiltViewModel
class SettingsIndexViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val webpageTool: WebpageTool,
) : ViewModel4(dispatcherProvider) {

    fun openChangelog() {
        webpageTool.open("https://myperm.darken.eu/changelog")
    }

    fun openPrivacyPolicy() {
        webpageTool.open(PrivacyPolicy.URL)
    }

    companion object {
        private val TAG = logTag("Settings", "Index", "VM")
    }
}
