package com.servalabs.perms.main.ui.overview

import android.content.Context
import com.servalabs.perms.R
import com.servalabs.perms.common.getQuantityString

data class PkgCount(
    val user: Int,
    val system: Int,
) {
    fun getHR(c: Context): String {
        val sumText = c.getQuantityString(R.plurals.generic_x_apps_label, user + system)
        val userText = c.getQuantityString(R.plurals.generic_x_apps_user_label, user)
        val systemText = c.getQuantityString(R.plurals.generic_x_apps_system_label, system)
        return "$sumText ($userText, $systemText)"
    }
}

