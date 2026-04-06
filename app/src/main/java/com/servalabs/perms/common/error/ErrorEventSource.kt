package com.servalabs.perms.common.error

import com.servalabs.perms.common.livedata.SingleLiveEvent

interface ErrorEventSource {
    val errorEvents: SingleLiveEvent<Throwable>
}