package com.apps.travel_app.ui.components

import android.app.Activity
import android.content.Intent
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.apps.travel_app.MainActivity
import com.apps.travel_app.models.Destination
import com.apps.travel_app.ui.pages.InspirationActivity
import com.apps.travel_app.ui.theme.*
import com.google.gson.Gson
import com.guru.fontawesomecomposelib.FaIcon
import com.skydoves.landscapist.glide.GlideImage


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DestinationCard(
    modifier: Modifier = Modifier,
    destination: Destination?,
    open: Boolean = false,
    maxHeightValue: Float = 130f,
    activity: Activity
) {

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
            .padding(cardPadding)
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
                    .padding(7.dp)
                    .align(Alignment.CenterVertically)
                    .graphicsLayer {
                        shape = RoundedCornerShape(20)
                        clip = true
                    }
            )

            Column(
                modifier = Modifier.padding(cardPadding).weight(1f)
            ) {
                Heading(
                    destination?.name ?: ""
                )
                if (destination?.rating ?: 0f > 0) {

                    RatingBar(
                        rating = destination?.rating ?: 0f,
                        modifier = Modifier
                            .height(15.dp),
                        emptyColor = Color(0x88FFFFFF)
                    )

                }
            }

            Button(
                background = primaryColor,
                modifier = Modifier.padding(5.dp),
                onClick = {
                    if (destination != null) {
                        val intent = Intent(activity, MainActivity::class.java)
                        intent.action = "destination"
                        val gson = Gson()
                        intent.putExtra("destinationId", gson.toJson(destination))
                        ContextCompat.startActivity(activity, intent, null)
                    }
                }
            ) {
                FaIcon(faIcon = FaIcons.SearchLocation, tint = White)
            }
        }
    }
}
