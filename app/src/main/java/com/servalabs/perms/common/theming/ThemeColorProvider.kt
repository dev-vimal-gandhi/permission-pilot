package com.servalabs.perms.common.theming

import androidx.compose.material3.ColorScheme

object ThemeColorProvider {

    fun getLightColorScheme(color: ThemeColor, style: ThemeStyle): ColorScheme = when (color) {
        ThemeColor.BLUE -> ServaPermsColorsBlue.lightScheme(style)
        ThemeColor.GREEN -> ServaPermsColorsGreen.lightScheme(style)
        ThemeColor.AMBER -> ServaPermsColorsAmber.lightScheme(style)
    }

    fun getDarkColorScheme(color: ThemeColor, style: ThemeStyle): ColorScheme = when (color) {
        ThemeColor.BLUE -> ServaPermsColorsBlue.darkScheme(style)
        ThemeColor.GREEN -> ServaPermsColorsGreen.darkScheme(style)
        ThemeColor.AMBER -> ServaPermsColorsAmber.darkScheme(style)
    }
}
