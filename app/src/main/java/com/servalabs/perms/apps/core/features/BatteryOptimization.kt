package com.servalabs.perms.apps.core.features

import android.content.pm.PackageInfo
import androidx.room.TypeConverter
import com.servalabs.perms.common.IPCFunnel
import com.servalabs.perms.common.hasApiLevel
import com.servalabs.perms.permissions.core.known.APerm

enum class BatteryOptimization {
    IGNORED,
    OPTIMIZED,
    MANAGED_BY_SYSTEM,
    UNKNOWN;

    class Converter {
        @TypeConverter
        fun fromEnum(value: BatteryOptimization): String = value.name

        @TypeConverter
        fun toEnum(value: String): BatteryOptimization = valueOf(value)
    }
}

suspend fun PackageInfo.determineBatteryOptimization(ipcFunnel: IPCFunnel): BatteryOptimization {
    if (!hasApiLevel(23)) return BatteryOptimization.IGNORED
    val permissions = requestedPermissions ?: return BatteryOptimization.MANAGED_BY_SYSTEM
    if (permissions.none { it == APerm.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS.id.value }) {
        return BatteryOptimization.MANAGED_BY_SYSTEM
    }

    return if (ipcFunnel.powerManager.isIgnoringBatteryOptimizations(packageName)) {
        BatteryOptimization.IGNORED
    } else {
        BatteryOptimization.OPTIMIZED
    }
}