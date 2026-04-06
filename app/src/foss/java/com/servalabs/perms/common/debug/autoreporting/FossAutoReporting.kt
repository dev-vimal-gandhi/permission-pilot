package com.servalabs.perms.common.debug.autoreporting

import android.app.Application
import com.servalabs.perms.common.debug.autoreport.AutomaticBugReporter
import com.servalabs.perms.common.debug.logging.log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FossAutoReporting @Inject constructor() : AutomaticBugReporter {
    override fun setup(application: Application) {
        // NOOP
    }

    override fun notify(throwable: Throwable) {
        throw IllegalStateException("Who initliazed this? Without setup no calls to here!")
    }
}