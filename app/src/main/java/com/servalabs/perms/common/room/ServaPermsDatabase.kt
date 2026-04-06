package com.servalabs.perms.common.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.withTransaction
import com.servalabs.perms.apps.core.features.BatteryOptimization
import com.servalabs.perms.apps.core.features.InternetAccess
import com.servalabs.perms.apps.core.manifest.ManifestHintEntity
import com.servalabs.perms.common.room.dao.ManifestHintDao
import com.servalabs.perms.common.room.dao.PendingSnapshotEventDao
import com.servalabs.perms.common.room.dao.PermissionChangeDao
import com.servalabs.perms.common.room.dao.SnapshotDao
import com.servalabs.perms.common.room.dao.SnapshotPkgDao
import com.servalabs.perms.common.room.entity.PendingSnapshotEventEntity
import com.servalabs.perms.common.room.entity.PermissionChangeEntity
import com.servalabs.perms.common.room.entity.PkgType
import com.servalabs.perms.common.room.entity.SnapshotEntity
import com.servalabs.perms.common.room.entity.SnapshotPkgDeclaredPermEntity
import com.servalabs.perms.common.room.entity.SnapshotPkgEntity
import com.servalabs.perms.common.room.entity.SnapshotPkgPermEntity
import com.servalabs.perms.watcher.core.WatcherEventType

@TypeConverters(
    WatcherEventType.Converter::class,
    InternetAccess.Converter::class,
    BatteryOptimization.Converter::class,
    PkgType.Converter::class,
)
@Database(
    entities = [
        SnapshotEntity::class,
        SnapshotPkgEntity::class,
        SnapshotPkgPermEntity::class,
        SnapshotPkgDeclaredPermEntity::class,
        PermissionChangeEntity::class,
        PendingSnapshotEventEntity::class,
        ManifestHintEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class ServaPermsDatabase : RoomDatabase() {
    abstract fun snapshotDao(): SnapshotDao
    abstract fun snapshotPkgDao(): SnapshotPkgDao
    abstract fun permissionChangeDao(): PermissionChangeDao
    abstract fun pendingSnapshotEventDao(): PendingSnapshotEventDao
    abstract fun manifestHintDao(): ManifestHintDao

    open suspend fun <R> inTransaction(block: suspend () -> R): R = withTransaction(block)
}
