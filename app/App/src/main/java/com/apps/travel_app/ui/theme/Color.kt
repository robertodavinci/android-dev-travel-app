package com.apps.travel_app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)
val primaryColor = Color(0xFF0083FF)
val secondaryColor = Color(0xFF0460D9)
val iconLightColor = Color(0xFF808ea7)
val textLightColor = Color(0xFF4b5360)
val textDarkColor = Color(0xFFCFD7E4)
val lightBackground = Color(0xFFFFFFFF)
val darkBackground = Color(0xFFFFFFFF)
val yellow = Color(0xFFFFC107)
val danger = Color(0xFFF4364C)
val success = Color(0xFF8CCE3F)
val cardLightBackground = Color(0xFFEEEEF8)

fun contrastColor(color: Color): Color {
    val luminance = color.luminance()
    return if(luminance > 0.5f) textLightColor else textDarkColor
}