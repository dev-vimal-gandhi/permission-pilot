package com.servalabs.perms.watcher.core

import com.servalabs.perms.apps.core.features.UsesPermission
import com.servalabs.perms.common.room.entity.SnapshotPkgDeclaredPermEntity
import com.servalabs.perms.common.room.entity.SnapshotPkgPermEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SnapshotDiffer @Inject constructor() {

    fun diff(
        previousPerms: List<SnapshotPkgPermEntity>,
        previousDeclared: List<SnapshotPkgDeclaredPermEntity>,
        currentPerms: List<CurrentPermission>,
        currentDeclared: List<String>,
    ): PermissionDiff {
        val prevPermIds = previousPerms.map { it.permissionId }.toSet()
        val currPermIds = currentPerms.map { it.permissionId }.toSet()

        val addedPermissions = (currPermIds - prevPermIds).toList()
        val removedPermissions = (prevPermIds - currPermIds).toList()

        val prevStatusMap = previousPerms.associate { it.permissionId to UsesPermission.Status.valueOf(it.status) }
        val grantChanges = currentPerms
            .filter { it.permissionId in prevPermIds }
            .mapNotNull { curr ->
                val prevStatus = prevStatusMap[curr.permissionId] ?: return@mapNotNull null
                if (prevStatus != curr.status) {
                    PermissionDiff.GrantChange(
                        permissionId = curr.permissionId,
                        oldStatus = prevStatus,
                        newStatus = curr.status,
                    )
                } else null
            }

        val prevDeclaredIds = previousDeclared.map { it.permissionId }.toSet()
        val currDeclaredIds = currentDeclared.toSet()
        val addedDeclared = (currDeclaredIds - prevDeclaredIds).toList()
        val removedDeclared = (prevDeclaredIds - currDeclaredIds).toList()

        val currentStatusMap = currentPerms.associate { it.permissionId to it.status }
        val gainedCount = addedPermissions.count { currentStatusMap[it]?.isGranted == true } +
            grantChanges.count { !it.oldStatus.isGranted && it.newStatus.isGranted }
        val lostCount = removedPermissions.count { prevStatusMap[it]?.isGranted == true } +
            grantChanges.count { it.oldStatus.isGranted && !it.newStatus.isGranted }

        return PermissionDiff(
            addedPermissions = addedPermissions,
            removedPermissions = removedPermissions,
            grantChanges = grantChanges,
            addedDeclared = addedDeclared,
            removedDeclared = removedDeclared,
            gainedCount = gainedCount,
            lostCount = lostCount,
        )
    }

    data class CurrentPermission(
        val permissionId: String,
        val status: UsesPermission.Status,
    )
}
