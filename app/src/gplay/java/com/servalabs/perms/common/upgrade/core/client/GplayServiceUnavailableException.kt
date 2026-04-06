package com.servalabs.perms.common.upgrade.core.client

import android.content.Context
import com.servalabs.perms.R
import com.servalabs.perms.common.error.HasLocalizedError
import com.servalabs.perms.common.error.LocalizedError

class GplayServiceUnavailableException(cause: Throwable) : Exception("Google Play services are unavailable.", cause),
    HasLocalizedError {
    override fun getLocalizedError(context: Context): LocalizedError = LocalizedError(
        throwable = this,
        label = "Google Play Services Unavailable",
        description = context.getString(R.string.upgrades_gplay_unavailable_error)
    )
}