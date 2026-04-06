package com.servalabs.perms

import android.app.Application
import android.os.DeadObjectException
import android.os.TransactionTooLargeException
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.Coil
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import com.servalabs.perms.common.debug.autoreport.AutomaticBugReporter
import com.servalabs.perms.common.debug.logging.LogCatLogger
import com.servalabs.perms.common.debug.logging.Logging
import com.servalabs.perms.common.debug.logging.Logging.Priority.WARN
import com.servalabs.perms.common.debug.logging.asLog
import com.servalabs.perms.common.debug.logging.log
import com.servalabs.perms.common.debug.logging.logTag
import com.servalabs.perms.common.debug.recording.core.RecorderModule
import com.servalabs.perms.common.error.causes
import com.servalabs.perms.settings.core.GeneralSettings
import javax.inject.Inject

@HiltAndroidApp
open class App : Application(), Configuration.Provider {

    @Inject lateinit var bugReporter: AutomaticBugReporter
    @Inject lateinit var recorderModule: RecorderModule
    @Inject lateinit var imageLoaderFactory: ImageLoaderFactory
    @Inject lateinit var generalSettings: GeneralSettings
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Logging.install(LogCatLogger())
            log(TAG) { "BuildConfig.DEBUG=true" }
        }

        bugReporter.setup(this)

        Coil.setImageLoader(imageLoaderFactory)

        // https://github.com/d4rken-org/serva-perms/issues/186
        Thread.setDefaultUncaughtExceptionHandler(ipcGuard)

        log(TAG) { "onCreate() done! ${Exception().asLog()}" }
    }

    private val ipcGuard = object : Thread.UncaughtExceptionHandler {

        private val defaultHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

        override fun uncaughtException(thread: Thread, throwable: Throwable) {
            val ipcMessages = setOf(
                "Package manager has died",
                "DeadSystemException"
            )
            val ipcExceptions = setOf(
                DeadObjectException::class,
                TransactionTooLargeException::class,
            )
            val isIpcIssue = ipcMessages.any { it == throwable.message }
                    || throwable.causes.any { cause -> ipcExceptions.any { it.isInstance(cause) } }

            if (isIpcIssue) {
                log(TAG, WARN) { "Crashing due to IPC buffer overflow!" }
                if (generalSettings.ipcParallelisation.valueBlocking == 0) {
                    log(TAG, WARN) { "Reducing `ipcParallelisation` from AUTO (0) to 1" }
                    generalSettings.ipcParallelisation.valueBlocking = 1
                }
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    companion object {
        internal val TAG = logTag("App")
    }
}
