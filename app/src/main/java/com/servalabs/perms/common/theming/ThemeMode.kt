package com.servalabs.perms.common.theming

import androidx.annotation.StringRes
import com.servalabs.perms.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ThemeMode(
    @StringRes val labelRes: Int,
) {
    @SerialName("theme.mode.system") SYSTEM(R.string.ui_theme_mode_system_label),
    @SerialName("theme.mode.dark") DARK(R.string.ui_theme_mode_dark_label),
    @SerialName("theme.mode.light") LIGHT(R.string.ui_theme_mode_light_label),
}
