package com.servalabs.perms.common.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.servalabs.perms.apps.core.Pkg
import com.servalabs.perms.watcher.core.WatcherEventType

@Entity(
    tableName = "pending_snapshot_events",
    indices = [Index("packageName", "userHandleId")],
)
data class PendingSnapshotEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: Pkg.Name,
    val eventType: WatcherEventType,
    val userHandleId: Int,
    val createdAt: Long,
)
