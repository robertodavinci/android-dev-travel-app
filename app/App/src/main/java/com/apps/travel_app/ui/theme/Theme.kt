package com.apps.travel_app.ui.theme

import android.content.Context
import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

import androidx.preference.PreferenceManager
import com.apps.travel_app.R
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorPalette = darkColors(
    primary = primaryDarkColor,
    secondary = secondaryDarkColor,
    background = darkBackground,
    surface = textDarkColor,
    onPrimary = Color.DarkGray,
    onSecondary = Color.White,
    onBackground = carddarkBackground,
    onSurface = primaryColor,
    secondaryVariant = transparentPrimaryDark
)

private val LightColorPalette = lightColors(
    primary = primaryColor,
    secondary = secondaryColor,
    background = lightBackground,
    surface = textLightColor,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = cardlightBackground,
    onSurface = primaryColor,
    secondaryVariant = transparentPrimary

)

var mapStyle = R.raw.style


@Composable
fun Travel_AppTheme(systemTheme: Boolean = true, content: @Composable () -> Unit) {

    //val colors = if (isSystemInDarkTheme() && systemTheme)
    val colors = if (systemTheme) {
        mapStyle = R.raw.style_dark
        DarkColorPalette
    } else {
        mapStyle = R.raw.style
        LightColorPalette
    }

    val systemUiController = rememberSystemUiController()
    if(systemTheme){
        systemUiController.setSystemBarsColor(
            color = Color.Transparent // TODO: dark theme status bar color
            //color = darkBackground
        )
    }else{
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            //color = darkBackground,
            darkIcons = true
        )
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

@Composable
fun MainActivity_Travel_AppTheme(systemTheme: Boolean = true, content: @Composable () -> Unit) {
    followSystem = remember { mutableStateOf(systemTheme)}
    Travel_AppTheme(systemTheme = followSystem.value, content = content)
}

fun requireFullscreenMode(window: Window, context: Context) {
    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    val systemTheme = sharedPref.getBoolean("darkTheme", true)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.setDecorFitsSystemWindows(false)
    } else {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }
}