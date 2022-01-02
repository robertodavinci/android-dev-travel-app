package com.apps.travel_app.ui.theme

import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

lateinit var followSystem: MutableState<Boolean>

val primaryColor = Color(0xFF0083FF)
val secondaryColor = Color(0xFF0460D9)
val transparentPrimary = Color(0x220083FF)
val primaryLightColor = Color(0xFF0083FF)
val secondaryLightColor = Color(0xFF0460D9)
val primaryDarkColor = Color(0xFF354253)
val secondaryDarkColor = Color(0xFF063C83)
val transparentPrimaryDark = Color(0x4450555E)
val iconLightColor = Color(0xFF79818F)
val textLightColor = Color(0xFF50555E)
val textDarkColor = Color(0xFFDCE0E7)
val lightBackground = Color(0xFFFFFFFF)
val darkBackground = Color(0xFF21212C)
val yellow = Color(0xFFFFC107)
val danger = Color(0xFFF4364C)
val success = Color(0xFF8CCE3F)
val cardlightBackground = Color(0xFFEAEAF0)
val carddarkBackground = Color(0xFF2F2F3D)

fun contrastColor(color: Color): Color {
    val luminance = color.luminance()
    return if(luminance > 0.5f) textLightColor else textDarkColor
}