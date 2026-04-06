package com.servalabs.perms.watcher.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import com.servalabs.perms.apps.core.Pkg
import com.servalabs.perms.apps.core.toNotificationId
import com.servalabs.perms.common.debug.logging.log
import com.servalabs.perms.common.debug.logging.logTag
import com.servalabs.perms.common.room.dao.PermissionChangeDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WatcherActionReceiver : BroadcastReceiver() {

    @Inject lateinit var changeDao: PermissionChangeDao
    @Inject lateinit var notificationManager: NotificationManagerCompat

    override fun onReceive(context: Context, intent: Intent) {
        val reportId = intent.getLongExtra(EXTRA_REPORT_ID, -1L)
        if (reportId < 0) return

        val packageName = Pkg.Name(intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: return)
        val notificationId = packageName.toNotificationId()

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_MARK_SEEN -> {
                        log(TAG) { "Marking report $reportId as seen" }
                        changeDao.markSeen(reportId)
                    }
                    ACTION_DELETE -> {
                        log(TAG) { "Deleting report $reportId" }
                        changeDao.deleteById(reportId)
                    }
                }
                notificationManager.cancel(notificationId)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_MARK_SEEN = "com.servalabs.perms.watcher.ACTION_MARK_SEEN"
        const val ACTION_DELETE = "com.servalabs.perms.watcher.ACTION_DELETE"
        const val EXTRA_REPORT_ID = "extra_report_id"
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        private val TAG = logTag("Watcher", "ActionReceiver")
    }
}
