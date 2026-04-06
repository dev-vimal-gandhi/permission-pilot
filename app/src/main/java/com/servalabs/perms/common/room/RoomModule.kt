package com.servalabs.perms.common.room

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.servalabs.perms.common.room.dao.ManifestHintDao
import com.servalabs.perms.common.room.dao.PendingSnapshotEventDao
import com.servalabs.perms.common.room.dao.PermissionChangeDao
import com.servalabs.perms.common.room.dao.SnapshotDao
import com.servalabs.perms.common.room.dao.SnapshotPkgDao
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class RoomModule {

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): ServaPermsDatabase = Room.databaseBuilder(
        context,
        ServaPermsDatabase::class.java,
        "permpilot.db",
    ).build()

    @Provides
    fun snapshotDao(db: ServaPermsDatabase): SnapshotDao = db.snapshotDao()

    @Provides
    fun snapshotPkgDao(db: ServaPermsDatabase): SnapshotPkgDao = db.snapshotPkgDao()

    @Provides
    fun permissionChangeDao(db: ServaPermsDatabase): PermissionChangeDao = db.permissionChangeDao()

    @Provides
    fun pendingSnapshotEventDao(db: ServaPermsDatabase): PendingSnapshotEventDao = db.pendingSnapshotEventDao()

    @Provides
    fun manifestHintDao(db: ServaPermsDatabase): ManifestHintDao = db.manifestHintDao()

}
