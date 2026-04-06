package com.servalabs.perms.common.upgrade.ui

import java.lang.System
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import com.servalabs.perms.common.WebpageTool
import com.servalabs.perms.common.coroutine.DispatcherProvider
import com.servalabs.perms.common.debug.logging.logTag
import com.servalabs.perms.common.flow.SingleEventFlow
import com.servalabs.perms.common.uix.ViewModel4
import com.servalabs.perms.common.upgrade.core.FossUpgrade
import com.servalabs.perms.common.upgrade.core.UpgradeControlFoss
import javax.inject.Inject

@HiltViewModel
class UpgradeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val upgradeControlFoss: UpgradeControlFoss,
    private val webpageTool: WebpageTool,
) : ViewModel4(dispatcherProvider) {

    sealed interface SponsorEvent {
        data object ReturnedTooEarly : SponsorEvent
    }

    val sponsorEvents = SingleEventFlow<SponsorEvent>()

    fun sponsor() {
        savedStateHandle[KEY_SPONSOR_OPENED_AT] = System.currentTimeMillis()
        webpageTool.open("https://github.com/sponsors/d4rken")
    }

    fun onResume() {
        val openedAt = savedStateHandle.get<Long>(KEY_SPONSOR_OPENED_AT) ?: return
        savedStateHandle.remove<Long>(KEY_SPONSOR_OPENED_AT)

        if (System.currentTimeMillis() - openedAt >= 5_000L) {
            upgradeControlFoss.upgrade(FossUpgrade.Reason.DONATED)
            navUp()
        } else {
            sponsorEvents.tryEmit(SponsorEvent.ReturnedTooEarly)
        }
    }

    companion object {
        private const val KEY_SPONSOR_OPENED_AT = "sponsor_opened_at"
        private val TAG = logTag("Upgrade", "Foss", "VM")
    }
}
