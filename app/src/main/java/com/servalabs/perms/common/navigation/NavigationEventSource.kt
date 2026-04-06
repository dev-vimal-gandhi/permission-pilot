package com.servalabs.perms.common.navigation

import com.servalabs.perms.common.flow.SingleEventFlow

interface NavigationEventSource {
    val navEvents: SingleEventFlow<NavEvent>
}
