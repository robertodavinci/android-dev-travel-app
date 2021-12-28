package com.apps.travel_app.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.apps.travel_app.MainActivity
<<<<<<< Updated upstream
=======
import com.apps.travel_app.data.room.AppDatabase
import com.apps.travel_app.data.room.entity.Location
import com.apps.travel_app.data.room.entity.Trip
>>>>>>> Stashed changes
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.Trip
import com.apps.travel_app.ui.components.Button
import com.apps.travel_app.ui.components.Heading
import com.apps.travel_app.ui.components.MainCard
import com.apps.travel_app.ui.components.TripCard
import com.apps.travel_app.ui.theme.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.guru.fontawesomecomposelib.FaIcon

@Composable
fun TripsScreen(mainActivity: MainActivity) {

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = primaryColor
    )
    val saved = remember {
        mutableStateOf(
            arrayListOf(
                Destination(),
                Destination(),
                Destination(),
                Trip()
            )
        )
    }
    val yours = remember {
        mutableStateOf(
            arrayListOf(
                Trip(),
                Destination(),
                Destination(),
                Trip()
            )
        )
    }

    Column(modifier = Modifier.background(MaterialTheme.colors.background)) {

        Row(
            modifier = Modifier
                .graphicsLayer {
                    shape = RoundedCornerShape(bottomStart = 50.dp)
                    clip = true
                }
                .background(primaryColor)
        ) {
            Heading("${mainActivity.user.displayName ?: mainActivity.user.email}'s trips", color = White, modifier = Modifier.padding(cardPadding))
        }

        Box(
            modifier = Modifier.background(primaryColor)
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        shape = RoundedCornerShape(topEnd = 50.dp)
                        clip = true
                    }
                    .background(MaterialTheme.colors.background)
                    .align(Center)
            ) {
                item {

                    Button(
                        onClick = {},
                        background = primaryColor,
                        modifier = Modifier.padding(5.dp)
                    ) {
                        Row {
                            FaIcon(FaIcons.Hiking, tint = White, size = 18.dp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Create yours!", color = White)

                        }
                    }

                    Spacer(modifier = Modifier.height(cardPadding))

                    Heading("Saved")

                    Column {
                        saved.value.forEach { destination ->
                            if (destination is Destination) {
                                MainCard(
                                    destination = destination,
                                    rating = destination.rating,
                                    mainActivity = mainActivity,
                                    imageMaxHeight = 100f
                                )
                            } else if (destination is Trip) {
                                TripCard(
                                    trip = destination,
                                    rating = destination.rating,
                                    mainActivity = mainActivity,
                                    imageMaxHeight = 100f
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        Heading(
                            "Your trips", modifier = Modifier
                                .weight(1f)
                                .align(CenterVertically)
                        )
                        IconButton(
                            onClick = {},
                            modifier = Modifier
                                .padding(end = cardPadding)
                                .size(20.dp)
                                .align(CenterVertically)
                        ) {
                            FaIcon(
                                FaIcons.Plus,
                                tint = MaterialTheme.colors.surface,
                                size = 18.dp
                            )
                        }
                    }

                    Column {
                        yours.value.forEach { destination ->
                            if (destination is Destination) {
                                MainCard(
                                    destination = destination,
                                    rating = destination.rating,
                                    mainActivity = mainActivity,
                                    imageMaxHeight = 100f
                                )
                            } else if (destination is Trip) {
                                TripCard(
                                    trip = destination,
                                    rating = destination.rating,
                                    mainActivity = mainActivity,
                                    imageMaxHeight = 100f
                                )
                            }
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(80.dp)
                    )
                }
            }
        }
    }
}
