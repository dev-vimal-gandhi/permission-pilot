package com.servalabs.perms.common.error

import com.servalabs.perms.common.flow.SingleEventFlow

interface ErrorEventSource2 {
    val errorEvents: SingleEventFlow<Throwable>
}
