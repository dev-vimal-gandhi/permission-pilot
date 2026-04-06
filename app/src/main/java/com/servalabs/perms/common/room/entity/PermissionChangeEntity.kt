package com.servalabs.perms.common.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.servalabs.perms.apps.core.Pkg
import com.servalabs.perms.watcher.core.WatcherEventType

@Entity(
    tableName = "permission_change_reports",
    indices = [
        Index("isSeen", "detectedAt"),
        Index("packageName", "userHandleId", "sourceSnapshotId", unique = true),
    ],
)
data class PermissionChangeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: Pkg.Name,
    val userHandleId: Int,
    val appLabel: String?,
    val versionCode: Long,
    val versionName: String?,
    val eventType: WatcherEventType,
    val changesJson: String,
    val previousVersionCode: Long? = null,
    val previousVersionName: String? = null,
    val detectedAt: Long,
    val isSeen: Boolean = false,
    val sourceSnapshotId: String? = null,
)
