package com.servalabs.perms.common.uix

import com.servalabs.perms.common.coroutine.DispatcherProvider
import com.servalabs.perms.common.debug.logging.asLog
import com.servalabs.perms.common.debug.logging.log
import com.servalabs.perms.common.error.ErrorEventSource2
import com.servalabs.perms.common.flow.SingleEventFlow
import com.servalabs.perms.common.flow.setupCommonEventHandlers
import com.servalabs.perms.common.navigation.NavEvent
import com.servalabs.perms.common.navigation.NavigationDestination
import com.servalabs.perms.common.navigation.NavigationEventSource
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn

abstract class ViewModel4(
    dispatcherProvider: DispatcherProvider,
) : ViewModel2(dispatcherProvider), NavigationEventSource, ErrorEventSource2 {

    override val navEvents = SingleEventFlow<NavEvent>()
    override val errorEvents = SingleEventFlow<Throwable>()

    init {
        launchErrorHandler = CoroutineExceptionHandler { _, ex ->
            log(TAG) { "Error during launch: ${ex.asLog()}" }
            errorEvents.emitBlocking(ex)
        }
    }

    override fun <T> Flow<T>.launchInViewModel() = this
        .setupCommonEventHandlers(TAG) { "launchInViewModel()" }
        .launchIn(vmScope)

    fun navTo(
        destination: NavigationDestination,
        popUpTo: NavigationDestination? = null,
        inclusive: Boolean = false,
    ) {
        log(TAG) { "navTo($destination)" }
        navEvents.tryEmit(NavEvent.GoTo(destination, popUpTo, inclusive))
    }

    fun navUp() {
        log(TAG) { "navUp()" }
        navEvents.tryEmit(NavEvent.Up)
    }
}
