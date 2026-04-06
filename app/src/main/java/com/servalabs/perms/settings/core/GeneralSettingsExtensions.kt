package com.servalabs.perms.settings.core

import com.servalabs.perms.common.theming.ThemeState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

val GeneralSettings.themeState: Flow<ThemeState>
    get() = combine(
        themeMode.flow,
        themeStyle.flow,
        themeColor.flow,
    ) { mode, style, color ->
        ThemeState(mode = mode, style = style, color = color)
    }

val GeneralSettings.themeStateBlocking: ThemeState
    get() = ThemeState(
        mode = themeMode.valueBlocking,
        style = themeStyle.valueBlocking,
        color = themeColor.valueBlocking,
    )
