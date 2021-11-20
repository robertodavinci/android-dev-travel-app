package com.apps.travel_app.ui.components

import androidx.compose.ui.graphics.Color

fun getColorByType(type: ToastType): Color {
    return when(type) {
        ToastType.Info -> Color.White
        ToastType.Error -> Color.Red
        ToastType.Success -> Color.Green
        ToastType.Warning -> Color.Yellow
    }
}

class Toast {
    var message: String = ""
    var duration: Int = 1000
    var type: ToastType = ToastType.Info
}


enum class ToastType {
    Info,
    Warning,
    Error,
    Success
}