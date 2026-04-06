package com.servalabs.perms.permissions.ui.list

import android.content.Context
import android.os.Parcelable
import androidx.annotation.StringRes
import com.servalabs.perms.R
import com.servalabs.perms.permissions.core.ProtectionFlag
import com.servalabs.perms.permissions.core.ProtectionType
import com.servalabs.perms.permissions.core.container.BasePermission
import com.servalabs.perms.permissions.core.container.DeclaredPermission
import com.servalabs.perms.permissions.core.isHighlighted
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class PermsSortOptions(
    val mainSort: Sort = Sort.RELEVANCE,
    val reversed: Boolean = false
) : Parcelable {

    @Serializable
    enum class Sort(
        @StringRes val labelRes: Int,
    ) {
        @SerialName("RELEVANCE")
        RELEVANCE(
            labelRes = R.string.permissions_sort_apps_relevance_label,
        ) {
            override fun getComparator(c: Context): Comparator<BasePermission> =
                Comparator.comparing<BasePermission, Int> { perm ->
                    if (perm.isHighlighted) return@comparing Int.MAX_VALUE
                    if (perm !is DeclaredPermission) return@comparing Int.MIN_VALUE

                    val flags = perm.protectionFlags
                    when {
                        flags.contains(ProtectionFlag.RUNTIME_ONLY) -> 3
                        perm.protectionType == ProtectionType.DANGEROUS -> 2
                        flags.contains(ProtectionFlag.APPOP) -> 1
                        else -> 0
                    }
                }.reversed()
        },
        @SerialName("APPS_GRANTED")
        APPS_GRANTED(
            labelRes = R.string.permissions_sort_apps_granted_label,
        ) {
            override fun getComparator(c: Context): Comparator<BasePermission> =
                Comparator.comparing<BasePermission, Int> { perm ->
                    perm.grantingApps.size
                }.reversed()
        },
        @SerialName("APPS_REQUESTED")
        APPS_REQUESTED(
            labelRes = R.string.permissions_sort_apps_requested_label,
        ) {
            override fun getComparator(c: Context): Comparator<BasePermission> =
                Comparator.comparing<BasePermission, Int> { perm ->
                    perm.requestingApps.size
                }.reversed()
        },
        ;

        abstract fun getComparator(c: Context): Comparator<BasePermission>
    }
}
