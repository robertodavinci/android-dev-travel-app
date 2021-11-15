package com.apps.travel_app.ui.components

import android.os.Handler
import android.os.SystemClock
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.apps.travel_app.models.Destination
import com.apps.travel_app.ui.theme.*
import kotlin.math.max

val handler = Handler()
val start = SystemClock.uptimeMillis()
const val duration: Long = 1000
val interpolator = AccelerateDecelerateInterpolator()
var animTime = mutableStateOf(0f)

@Composable
fun DestinationCard(destination: Destination, modifier: Modifier?, animation: Boolean = false) {

    animTime = remember { mutableStateOf(0f) }

    Card(
        elevation = cardElevation,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(cardRadius),
        modifier = Modifier
            .fillMaxWidth(if (animation) animTime.value else 1f)
            .wrapContentSize()
            .padding(cardPadding)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (destination.thumbnail != null) {
                Image(
                    painter = BitmapPainter(destination.thumbnail!!),
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .align(Alignment.CenterVertically)
                )
            }
            Column(
                modifier = Modifier.padding(cardPadding)
            ) {
                Text(
                    text = destination.name,
                    color = textLightColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    fontSize = textNormal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Start)
                )
                Text(
                    text = "Second Level",
                    color = textLightColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    fontSize = textSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }

    animation()
}

fun animation() {
    if (animTime.value < 0.99) {
        handler.post(object : Runnable {
            override fun run() {
                val elapsed = SystemClock.uptimeMillis() - start
                val t = max(
                    1 - interpolator.getInterpolation(
                        elapsed.toFloat()
                                / duration
                    ), 0f
                )

                animTime.value = t
                if (t > 0.0 && t < 0.95) {
                    handler.postDelayed(this, 16)
                }
            }
        })
    }
}

@Preview
@Composable
fun DestinationCardPreview() {
    val destination = Destination()
    destination.name = "Prova"
    DestinationCard(
        destination = destination, modifier = Modifier
            .fillMaxWidth(),
        false
    )
}