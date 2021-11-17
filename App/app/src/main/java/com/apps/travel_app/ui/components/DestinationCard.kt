package com.apps.travel_app.ui.components

import android.graphics.BitmapFactory
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.apps.travel_app.R
import com.apps.travel_app.models.Destination
import com.apps.travel_app.ui.pages.drawingEnabled
import com.apps.travel_app.ui.theme.*
import com.guru.fontawesomecomposelib.FaIcon


@Composable
fun DestinationCard(destination: Destination, open: Boolean = false, maxHeightValue: Float = 130f) {

    val maxHeight: Float by animateFloatAsState(
        if (open) maxHeightValue else 0f, animationSpec = tween(
            durationMillis = 1000,
            easing = LinearOutSlowInEasing
        )
    )

    Card(
        elevation = cardElevation,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(cardRadius),
        modifier = Modifier
            .heightIn(0.dp, maxHeight.dp)
            .wrapContentSize()
            .padding(cardPadding)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (destination.thumbnail != null) {
                Image(
                    painter = BitmapPainter(destination.thumbnail!!),
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .fillMaxHeight()
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
}

@Preview
@Composable
fun DestinationCardPreview() {
    val destination = Destination()
    destination.name = "Prova"
    DestinationCard(
        destination = destination,
        true
    )
}