package com.apps.travel_app.ui.theme

import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

lateinit var followSystem: MutableState<Boolean>

val primaryColor = Color(0xFF0083FF)
val secondaryColor = Color(0xFF0460D9)
val primaryLightColor = Color(0xFF0460D9)
val secondaryLightColor = Color(0xFF0460D9)
val primaryDarkColor = Color(0xFF354253)
val secondaryDarkColor = Color(0xFF063C83)
val iconLightColor = Color(0xFF808ea7)
val textLightColor = Color(0xFF4b5360)
val iconDarkColor = Color(0xFF808ea7)
val textDarkColor = Color(0xFFCFD7E4)
val lightBackground = Color(0xFFFFFFFF)
val darkBackground = Color(0xFF262638)
val yellow = Color(0xFFFFC107)
val danger = Color(0xFFF4364C)
val success = Color(0xFF8CCE3F)
val cardlightBackground = Color(0xFFFFFFFF)
val carddarkBackground = Color(0xFF333341)

fun contrastColor(color: Color): Color {
    val luminance = color.luminance()
    return if(luminance > 0.5f) textLightColor else textDarkColor
}