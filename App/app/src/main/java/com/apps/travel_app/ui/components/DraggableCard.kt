package com.apps.travel_app.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import com.apps.travel_app.ui.theme.cardElevation
import com.apps.travel_app.ui.theme.cardPadding
import com.apps.travel_app.ui.theme.cardRadius
import com.apps.travel_app.ui.theme.danger
import kotlin.math.roundToInt


@Composable
fun DraggableCard(
    units: ArrayList<Unit>,
) {
    var cursor by remember {
        mutableStateOf(0)
    }

    Column(
        Modifier.padding(cardPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Box(
            Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            units.forEachIndexed { i, it ->
                Card(
                    elevation = cardElevation,
                    shape = RoundedCornerShape(cardRadius),
                    backgroundColor = Color.White,
                    modifier = Modifier.zIndex((units.size - i).toFloat())
                        .scale(1 - 0.1f * (i - cursor))
                ) {
                    it.run {  }
                }

            }
        }
        Row(

        ) {
            Button {
                Text("Skip")
            }
            Button {
                Text("Next")
            }
        }
    }


}