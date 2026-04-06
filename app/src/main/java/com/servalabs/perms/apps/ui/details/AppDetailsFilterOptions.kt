package com.servalabs.perms.apps.ui.details

import android.os.Parcelable
import androidx.annotation.StringRes
import com.servalabs.perms.R
import com.servalabs.perms.apps.core.features.UsesPermission
import com.servalabs.perms.apps.core.PermissionUse
import com.servalabs.perms.permissions.core.container.BasePermission
import com.servalabs.perms.permissions.core.features.RuntimeGrant
import com.servalabs.perms.permissions.core.features.SpecialAccess
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class AppDetailsFilterOptions(
    val filters: Set<Filter> = setOf(Filter.GRANTED, Filter.DENIED, Filter.CONFIGURABLE)
) : Parcelable {

    @Serializable
    enum class Filter(
        @StringRes val labelRes: Int
    ) {
        @SerialName("GRANTED") GRANTED(R.string.filter_granted_label),
        @SerialName("DENIED") DENIED(R.string.filter_denied_label),
        @SerialName("CONFIGURABLE") CONFIGURABLE(R.string.filter_configurable_label);

        fun matches(cachedPerm: PermissionUse, basePerm: BasePermission): Boolean = when (this) {
            GRANTED -> cachedPerm.status.isGranted

            DENIED -> cachedPerm.status == UsesPermission.Status.DENIED
            CONFIGURABLE -> basePerm.tags.contains(RuntimeGrant) || basePerm.tags.contains(SpecialAccess)
        }
    }
}
