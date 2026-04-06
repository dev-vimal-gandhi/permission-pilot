package com.servalabs.perms.common.debug

import com.servalabs.perms.common.debug.autoreport.AutomaticBugReporter
import com.servalabs.perms.common.debug.logging.Logging.Priority.VERBOSE
import com.servalabs.perms.common.debug.logging.Logging.Priority.WARN
import com.servalabs.perms.common.debug.logging.log
import com.servalabs.perms.common.debug.logging.logTag

object Bugs {
    var reporter: AutomaticBugReporter? = null
    fun report(exception: Exception) {
        log(TAG, VERBOSE) { "Reporting $exception" }
        if (reporter == null) {
            log(TAG, WARN) { "Bug tracking not initialized yet." }
            return
        }
        reporter?.notify(exception)
    }

    private val TAG = logTag("Debug", "Bugs")
}