package com.apps.travel_app.ui.components

import FaIcons.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.apps.travel_app.ui.theme.contrastColor
import com.apps.travel_app.ui.theme.textExtraSmall

@Composable
fun Badge(background: Color = Color(0xCCEEEEEE), text: String) {
    Text(
        text = text,
        fontSize = textExtraSmall,
        color = contrastColor(background),
        modifier = Modifier
            .graphicsLayer {
                shape = RoundedCornerShape(100)
                clip = true
            }
            .background(background)
            .padding(4.dp)
    )
}
