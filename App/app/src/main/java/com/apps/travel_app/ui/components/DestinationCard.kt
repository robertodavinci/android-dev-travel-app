package com.apps.travel_app.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.apps.travel_app.models.Destination
import com.apps.travel_app.ui.theme.*
import androidx.compose.material.MaterialTheme
import com.skydoves.landscapist.glide.GlideImage


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DestinationCard(modifier: Modifier = Modifier, destination: Destination?, open: Boolean = false, maxHeightValue: Float = 130f) {

    val maxHeight: Float by animateFloatAsState(
        if (open) maxHeightValue else 0f, animationSpec = tween(
            durationMillis = 1000,
            easing = LinearOutSlowInEasing
        )
    )

    Card(
        elevation = cardElevation,
        backgroundColor = MaterialTheme.colors.onBackground,
        shape = RoundedCornerShape(cardRadius),
        modifier = modifier
            .heightIn(0.dp, maxHeight.dp)
            .wrapContentSize()
            .padding(cardPadding),
        onClick = {

        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GlideImage(
                imageModel = destination?.thumbnailUrl,
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(0.dp, maxHeightValue.dp)
                    .align(Alignment.CenterVertically)
            )

            Column(
                modifier = Modifier.padding(cardPadding)
            ) {
                Text(
                    text = destination?.name ?: "",
                    color = MaterialTheme.colors.surface,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    fontSize = textNormal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Start)
                )
                Text(
                    text = "Second Level",
                    color = MaterialTheme.colors.surface,
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
