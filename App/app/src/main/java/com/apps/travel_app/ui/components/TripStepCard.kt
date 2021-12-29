package com.apps.travel_app.ui.components

import FaIcons
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.apps.travel_app.models.Trip
import com.apps.travel_app.models.TripDestination
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.ui.utils.sendPostRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.guru.fontawesomecomposelib.FaIcon
import com.skydoves.landscapist.CircularReveal
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun TripStepCard(
    destination: TripDestination,
    index: Int,
    onComplete: (Boolean) -> Unit = {},
    changeable: Boolean = false,
    tripId: Int = 0
) {

    val openDialog = remember { mutableStateOf(false) }
    val done = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()

    ) {
        Row(
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
                                colors.surface,
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

                    IconButton(onClick = {
                        done.value = !done.value; destination.visited = done.value; onComplete(
                        done.value
                    )
                    }, modifier = Modifier
                        .width(50.dp)
                        .height(50.dp)
                        .graphicsLayer {
                            shape = RoundedCornerShape(100)
                            clip = true
                        }
                        .background(if (done.value) Color(0x88111122) else Color.Transparent)) {
                        FaIcon(
                            FaIcons.Check,
                            tint = textDarkColor,
                            modifier = Modifier.scale(scale)
                        )
                    }

                }
                Column {
                    Row {
                        Text(
                            destination.name,
                            color = colors.surface,
                            modifier = Modifier
                                .padding(5.dp)
                                .weight(1f),
                            fontWeight = FontWeight.Bold,
                            fontSize = textNormal,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (destination.hour.isNotEmpty()) {
                            Text(
                                destination.hour,
                                color = colors.surface,
                                modifier = Modifier.padding(5.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = textSmall
                            )
                        }
                    }

                    Row {
                        if (destination.description.isNotEmpty()) {
                            Text(
                                destination.description,
                                color = colors.surface,
                                modifier = Modifier
                                    .padding(5.dp)
                                    .weight(1f),
                                fontWeight = FontWeight.Bold,
                                fontSize = textSmall,
                                maxLines = 1,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (destination.minutes > 0) {
                            Text(
                                "${destination.minutes} min",
                                color = colors.surface,
                                modifier = Modifier.padding(5.dp),
                                fontSize = textSmall
                            )
                        }
                    }
                }
            }
        }

        if (openDialog.value) {
            if (changeable) {
                EditDialog(openDialog, destination)
            } else {
                Dialog(openDialog, destination,tripId)
            }
        }

    }
}

@Composable
fun Dialog(openDialog: MutableState<Boolean>, destination: TripDestination, tripId: Int) {

    var note: String? by remember {
        mutableStateOf("")
    }
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
                .background(colors.background)
                .fillMaxWidth(), verticalArrangement = Arrangement.Center
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
                                    if (destination.name.isNotEmpty()) colors.background else Color.Transparent
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
                color = colors.surface,
                modifier = Modifier.padding(cardPadding),
                fontSize = textNormal
            )

            if (destination.notes.isNotEmpty()) {
                Text(
                    "Notes",
                    color = colors.surface,
                    modifier = Modifier
                        .padding(cardPadding)
                        .fillMaxWidth(),
                    textAlign = Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = textNormal
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(cardPadding)
                ) {
                    items(destination.notes) {
                        Card(
                            backgroundColor = colors.onBackground,
                            modifier = Modifier
                                .widthIn(0.dp, 150.dp)
                                .padding(end = 5.dp),
                            shape = RoundedCornerShape(20)
                        ) {
                            Row {
                                FaIcon(FaIcons.StickyNote, size = 13.dp, tint = colors.surface)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(it, color = colors.surface, fontSize = textSmall)
                            }
                        }
                    }
                }
            }

            /*if (destination.images.isNotEmpty()) {

                Text(
                    "Images",
                    color = colors.surface,
                    modifier = Modifier
                        .padding(cardPadding)
                        .fillMaxWidth(),
                    textAlign = Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = textNormal
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(cardPadding)
                ) {
                    items(destination.images) {
                        GlideImage(
                            imageModel = it,
                            modifier = Modifier
                                .size(50.dp)
                                .padding(end = 5.dp)
                                .graphicsLayer {
                                    shape = RoundedCornerShape(10.dp)
                                    clip = true
                                })
                    }
                }
            }*/
            if (tripId > 0   && note != null) {

                TextField(
                    value = note!!,
                    onValueChange = {
                        note = it
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        backgroundColor = Color.Transparent,
                    ),
                    placeholder = {
                        Text(
                            "If you know the place, write a note",
                            color = colors.surface,
                            fontSize = textSmall,
                            modifier = Modifier
                                .alpha(0.5f)
                                .fillMaxWidth()
                        )
                    },
                    textStyle = TextStyle(
                        color = colors.surface,
                        fontSize = textSmall,
                        fontWeight = FontWeight.Bold
                    ),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                Row(
                    horizontalArrangement = SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(cardPadding)
                ) {
                    Button(onClick = {
                        if (note!!.isNotEmpty()) {
                            Thread {
                                val request =
                                    "{\"id\":${tripId},\"step_id\":\"${destination.id}\",\"note\":\"${note}\"}"
                                sendPostRequest(request, action = "addNote")

                                note = null

                            }.start()
                        }
                    }) {
                        Text("Add note", color = colors.surface)
                    }
                }
            }
        }

    }
}

@Composable
fun EditDialog(openDialog: MutableState<Boolean>, destination: TripDestination) {
    var description by remember {
        mutableStateOf(destination.description)
    }
    var hour by remember {
        mutableStateOf(destination.hour)
    }
    var minutes by remember {
        mutableStateOf(destination.minutes.toString())
    }
    androidx.compose.ui.window.Dialog(
        onDismissRequest = {
            openDialog.value = false
            destination.description = description
            destination.hour = hour
            destination.minutes = minutes.toIntOrNull() ?: 0
        },

        ) {
        Column(
            modifier = Modifier
                .padding(0.dp)
                .graphicsLayer {
                    shape = RoundedCornerShape(cardRadius)
                    clip = true
                }
                .background(colors.background)
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
                                    if (destination.name.isNotEmpty()) colors.surface else Color.Transparent
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

                }

            }

            Heading("Description")

            BasicTextField(
                value = description, onValueChange = { description = it },
                singleLine = true,
                textStyle = TextStyle(
                    color = colors.surface,
                    fontWeight = FontWeight.Bold,
                    textAlign = Center
                ),
                modifier = Modifier
                    .padding(cardPadding)
                    .fillMaxWidth(),
                cursorBrush = SolidColor(colors.surface)
            )

            Heading("Arrival hour")

            BasicTextField(
                value = hour, onValueChange = { hour = it },
                singleLine = true,
                textStyle = TextStyle(
                    color = colors.surface,
                    fontWeight = FontWeight.Bold,
                    textAlign = Center
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .padding(cardPadding)
                    .fillMaxWidth(),
                cursorBrush = SolidColor(colors.surface)
            )

            Heading("Minutes of visit duration")

            BasicTextField(
                value = minutes, onValueChange = { minutes = it },
                singleLine = true,
                textStyle = TextStyle(
                    color = colors.surface,
                    fontWeight = FontWeight.Bold,
                    textAlign = Center
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .padding(cardPadding)
                    .fillMaxWidth(),
                cursorBrush = SolidColor(colors.surface)
            )


        }

    }
}