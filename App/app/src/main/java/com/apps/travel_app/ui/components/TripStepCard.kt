package com.apps.travel_app.ui.components

import androidx.compose.material.MaterialTheme
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.Rating
import com.apps.travel_app.models.Trip
import com.apps.travel_app.models.TripDestination
import com.apps.travel_app.ui.theme.*
import com.guru.fontawesomecomposelib.FaIcon
import com.skydoves.landscapist.CircularReveal
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun TripStepCard(trip: Trip, destination: TripDestination, index: Int, onComplete: (Boolean) -> Unit = {}) {

    val openDialog = remember { mutableStateOf(false) }
    val done = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()

    ) {
        if (destination != null) {
            Row(
                //horizontalArrangement = SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                openDialog.value = true
                            }
                        )
                    }
            ) {
                Row {
                    Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                        GlideImage(
                            imageModel = destination.thumbnailUrl,
                            contentDescription = "",
                            modifier = Modifier
                                .width(50.dp)
                                .height(50.dp)
                                .border(
                                    5.dp,
                                    MaterialTheme.colors.surface,
                                    shape = RoundedCornerShape(100)
                                )
                                .graphicsLayer {
                                    shape = RoundedCornerShape(100)
                                    clip = true
                                }
                        )

                        val scale: Float by animateFloatAsState(
                            if (done.value) 1f else 0f, animationSpec = tween(
                                durationMillis = 500,
                                easing = LinearOutSlowInEasing
                            )
                        )

                        IconButton(onClick = { done.value = !done.value; destination.visited = done.value; onComplete(done.value) }, modifier = Modifier
                            .width(50.dp)
                            .height(50.dp)
                            .graphicsLayer {
                                shape = RoundedCornerShape(100)
                                clip = true
                            }
                            .background(if (done.value) Color(0x88111122) else Color.Transparent)) {
                            FaIcon(FaIcons.Check, tint = textDarkColor, modifier = Modifier.scale(scale))
                        }

                    }
                    Column {
                        Row {
                            Text(
                                destination.name,
                                color = MaterialTheme.colors.surface,
                                modifier = Modifier
                                    .padding(5.dp)
                                    .weight(1f),
                                fontWeight = FontWeight.Bold,
                                fontSize = textNormal,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                destination.hour,
                                color = MaterialTheme.colors.surface,
                                modifier = Modifier.padding(5.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = textSmall
                            )
                        }

                        Row {

                            Text(
                                destination.description,
                                color = MaterialTheme.colors.surface,
                                modifier = Modifier
                                    .padding(5.dp)
                                    .weight(1f),
                                fontWeight = FontWeight.Bold,
                                fontSize = textSmall,
                                maxLines = 1,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${destination.minutes} min",
                                color = MaterialTheme.colors.surface,
                                modifier = Modifier.padding(5.dp),
                                fontSize = textSmall
                            )
                        }
                    }
                }
            }
        }

        if (openDialog.value) {
            Dialog(openDialog, destination)
        }

    }
}

@Composable
fun Dialog(openDialog: MutableState<Boolean>, destination: TripDestination) {

    androidx.compose.ui.window.Dialog(
        onDismissRequest = {
            openDialog.value = false
        },

        ) {
        Column(
            modifier = Modifier
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
                    imageModel = destination.thumbnailUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
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
                                    if (destination.name.isNotEmpty()) MaterialTheme.colors.surface else Color.Transparent
                                )
                            )

                        )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = destination.name,
                            Modifier
                                .padding(start = cardPadding)
                                .fillMaxWidth(),
                            color = Color.White,
                            fontSize = textHeading,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (destination.rating > 0) {
                        Row(
                            modifier = Modifier.padding(
                                bottom = cardPadding,
                                start = cardPadding
                            )
                        ) {
                            RatingBar(
                                rating = destination.rating,
                                modifier = Modifier
                                    .height(15.dp),
                                emptyColor = Color(0x88FFFFFF)
                            )
                        }
                    }
                }

            }

            Text(
                destination.description,
                color = MaterialTheme.colors.surface,
                modifier = Modifier.padding(cardPadding),
                fontWeight = FontWeight.Bold,
                fontSize = textNormal
            )

            Row(
                horizontalArrangement = SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(cardPadding)
            ) {
                FaIcon(
                    FaIcons.Heart,
                    tint = danger
                )
                FaIcon(
                    FaIcons.ClockRegular,
                    tint = MaterialTheme.colors.surface
                )

            }
        }

    }
}