package com.servalabs.perms.common.notifications

import android.app.PendingIntent
import com.servalabs.perms.common.hasApiLevel

object PendingIntentCompat {
    val FLAG_IMMUTABLE: Int = if (hasApiLevel(31)) {
        PendingIntent.FLAG_IMMUTABLE
    } else {
        0
    }
}