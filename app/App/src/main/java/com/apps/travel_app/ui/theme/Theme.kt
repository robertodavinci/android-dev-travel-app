package com.apps.travel_app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.apps.travel_app.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorPalette = darkColors(
    primary = primaryColor,
    secondary = secondaryColor,
    background = darkBackground,
    surface = textDarkColor,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = carddarkBackground,
    onSurface = Color.Black,
)

private val LightColorPalette = lightColors(
    primary = primaryColor,
    secondary = secondaryColor,
    background = lightBackground,
    surface = textLightColor,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = cardlightBackground,
    onSurface = Color.Black,

)

var mapStyle = R.raw.style

@Composable
fun Travel_AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {
    val colors = if (darkTheme) {
        mapStyle = R.raw.style_dark
        DarkColorPalette
    } else {
        mapStyle = R.raw.style
        LightColorPalette
    }

    val systemUiController = rememberSystemUiController()
    if(darkTheme){
        systemUiController.setSystemBarsColor(
            color = darkBackground // TODO: dark theme status bar color
        )
    }else{
        systemUiController.setSystemBarsColor(
            color = lightBackground
        )
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
