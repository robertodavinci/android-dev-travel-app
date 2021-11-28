package com.apps.travel_app.ui.components
import androidx.compose.material.MaterialTheme
import FaIcons
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.apps.travel_app.MainActivity
import com.apps.travel_app.models.Trip
import com.apps.travel_app.ui.theme.*
import com.guru.fontawesomecomposelib.FaIcon
import com.guru.fontawesomecomposelib.FaIconType
import com.skydoves.landscapist.CircularReveal
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun TripCard(
    trip: Trip,
    rating: Float,
    squaredImage: Boolean = false,
    badges: List<String> = ArrayList(),
    views: Int = 0,
    padding: Dp = cardPadding,
    infoScale: Float = 1f,
    mainActivity: MainActivity? = null,
    shadow: Dp = cardElevation,
    radius: Dp = cardRadius,
    icon: FaIconType? = null,
    imageMaxHeight: Float = Float.POSITIVE_INFINITY
) {

    val openDialog = remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier
            .padding(padding)
            .fillMaxWidth()
            .widthIn(0.dp, 250.dp),
        elevation = shadow,
        shape = RoundedCornerShape(radius)
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colors.onBackground)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                mainActivity?.setTrip(trip, true)
                            }
                        )
                    }
            ) {
                val modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(0.dp, imageMaxHeight.dp)
                if (squaredImage)
                    modifier.aspectRatio(1f)
                GlideImage(
                    imageModel = trip.thumbnailUrl ?: trip.thumbnail,
                    modifier = modifier,
                    contentScale = if (squaredImage) ContentScale.Fit else ContentScale.Crop,
                    circularReveal = CircularReveal(duration = 700),

                    )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    if (trip.name.isNotEmpty()) textLightColor else Color.Transparent
                                )
                            )

                        )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (icon != null) {
                            FaIcon(
                                icon,
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(start = cardPadding * infoScale)
                                    .scale(infoScale)
                            )
                        }
                        Text(
                            text = trip.name,
                            Modifier
                                .padding(start = cardPadding * infoScale)
                                .fillMaxWidth(),
                            color = Color.White,
                            fontSize = textHeading * infoScale,
                            fontWeight = FontWeight.Bold
                        )
                        if (views > 0) {
                            Row(
                                modifier = Modifier.padding(5.dp)
                            ) {
                                FaIcon(FaIcons.EyeRegular, tint = Color.White)
                                Text(
                                    text = views.toString(),
                                    color = Color.White,
                                    fontSize = textSmall * infoScale,
                                    modifier = Modifier.padding(5.dp),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    if (rating > 0) {
                        Row(
                            modifier = Modifier.padding(
                                bottom = cardPadding * infoScale,
                                start = cardPadding * infoScale
                            )
                        ) {
                            RatingBar(
                                rating = rating,
                                modifier = Modifier
                                    .height(15.dp * infoScale),
                                emptyColor = Color(0x88FFFFFF)
                            )
                        }
                    }
                }

            }

            if (badges.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(cardPadding)
                ) {
                    badges.forEach { badge ->
                        Badge(
                            text = badge
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                }
            }
        }
    }
    val scale: Float by animateFloatAsState(
        if (openDialog.value) 1f else 0f, animationSpec = tween(
            durationMillis = 500,
            easing = LinearOutSlowInEasing
        )
    )
    if (openDialog.value) {

        Dialog(
            onDismissRequest = {
                openDialog.value = false
            },

            ) {
            Column(
                modifier = Modifier
                    .scale(scale)
                    .padding(0.dp)
                    .graphicsLayer {
                        shape = RoundedCornerShape(cardRadius)
                        clip = true
                    }
                    .background(MaterialTheme.colors.background)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    GlideImage(
                        imageModel = trip.thumbnailUrl ?: trip.thumbnail,
                        modifier = Modifier
                            .fillMaxWidth().height(200.dp),
                        circularReveal = CircularReveal(duration = 700),

                        )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        if (trip.name.isNotEmpty()) MaterialTheme.colors.surface else Color.Transparent
                                    )
                                )

                            )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (icon != null) {
                                FaIcon(
                                    icon,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(start = cardPadding)
                                )
                            }
                            Text(
                                text = trip.name,
                                Modifier
                                    .padding(start = cardPadding)
                                    .fillMaxWidth(),
                                color = Color.White,
                                fontSize = textHeading,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (rating > 0) {
                            Row(
                                modifier = Modifier.padding(
                                    bottom = cardPadding,
                                    start = cardPadding
                                )
                            ) {
                                RatingBar(
                                    rating = rating,
                                    modifier = Modifier
                                        .height(15.dp)
                                        .graphicsLayer {
                                            shadowElevation = 3f
                                        },
                                    emptyColor = Color(0x88FFFFFF)
                                )
                            }
                        }
                    }

                }
            }
        }
    }
}