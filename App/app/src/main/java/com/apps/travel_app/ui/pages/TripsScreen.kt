package com.apps.travel_app.ui.pages

import FaIcons
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.apps.travel_app.MainActivity
import com.apps.travel_app.data.room.AppDatabase
import com.apps.travel_app.data.room.entity.Location
import com.apps.travel_app.data.room.entity.Trip
import com.apps.travel_app.models.Destination
import com.apps.travel_app.ui.components.*
import com.apps.travel_app.ui.theme.cardPadding
import com.apps.travel_app.ui.theme.cardRadius
import com.apps.travel_app.ui.theme.danger
import com.apps.travel_app.ui.theme.primaryColor
import com.apps.travel_app.user
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.guru.fontawesomecomposelib.FaIcon

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TripsScreen(mainActivity: MainActivity) {

    /* val systemUiController = rememberSystemUiController()
     systemUiController.setSystemBarsColor(
         color = primaryColor
     )*/

    val db = Room.databaseBuilder(
        mainActivity,
        AppDatabase::class.java, "database-name"
    ).build()

    val saved = remember {
        mutableStateOf(
            ArrayList<Destination>()
        )
    }
    val savedTrips = remember {
        mutableStateOf(
            ArrayList<com.apps.travel_app.models.Trip>()
        )
    }

    Thread {
        val locations = db.locationDao().getAllSaved() as ArrayList<Location>
        val savedLocations = arrayListOf<Destination>()
        locations.forEach {
            val destination = Destination()
            destination.fromLocation(it)
            savedLocations.add(destination)
        }
        val trips = db.tripDao().getAll() as ArrayList<Trip>
        val finalSavedTrips = arrayListOf<com.apps.travel_app.models.Trip>()
        trips.forEach {
            val trip = com.apps.travel_app.models.Trip()
            trip.fromTripDb(it)
            finalSavedTrips.add(trip)
        }
        mainActivity.runOnUiThread {
            saved.value = savedLocations
            savedTrips.value = finalSavedTrips
        }
    }.start()

    Column(modifier = Modifier.background(MaterialTheme.colors.background)) {

        Row(
            modifier = Modifier
                .graphicsLayer {
                    shape = RoundedCornerShape(bottomStart = 50.dp)
                    clip = true
                }
                .background(primaryColor)

        ) {
            Heading(
                "${user.displayName ?: user.email}'s trips",
                color = White,
                modifier = Modifier.padding(top = cardPadding * 3, bottom = cardPadding * 2)
            )
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
                    .align(Center),
                horizontalAlignment = Alignment.End
            ) {
                item {

                    Button(
                        onClick = {
                            mainActivity.createTrip()
                        },
                        background = primaryColor,
                        modifier = Modifier
                            .padding(top = 15.dp, end = 25.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Row {
                            FaIcon(FaIcons.Hiking, tint = White, size = 18.dp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Create yours!", color = White)

                        }
                    }

                    Spacer(modifier = Modifier.height(cardPadding))

                    if (saved.value.size > 0) {
                        Heading("Saved")

                        Column {
                            saved.value.forEachIndexed { index, destination ->
                                val state = rememberDismissState(
                                    confirmStateChange = {
                                        if (it == DismissValue.DismissedToStart || it == DismissValue.DismissedToEnd) {
                                            Thread {
                                                db.locationDao().delete(destination.toLocation())
                                            }.start()
                                            val _saved =
                                                saved.value.clone() as ArrayList<Destination>
                                            _saved.removeAt(index)
                                            saved.value = _saved
                                        }
                                        true
                                    }
                                )
                                SwipeToDismiss(state = state, background = {
                                    Row(
                                        Modifier
                                            .fillMaxSize()
                                            .padding(cardPadding)
                                            .graphicsLayer {
                                                shape = RoundedCornerShape(cardRadius)
                                                clip = true
                                            }
                                            .background(danger),
                                        verticalAlignment = CenterVertically,
                                        horizontalArrangement = SpaceBetween
                                    ) {
                                        FaIcon(
                                            FaIcons.Trash,
                                            tint = White,
                                            modifier = Modifier.padding(cardPadding)
                                        )
                                        FaIcon(
                                            FaIcons.Trash,
                                            tint = White,
                                            modifier = Modifier.padding(cardPadding)
                                        )
                                    }
                                }) {
                                    MainCard(
                                        destination = destination,
                                        rating = destination.rating,
                                        mainActivity = mainActivity,
                                        imageMaxHeight = 100f
                                    )
                                }
                            }

                            savedTrips.value.forEach { trip ->
                                TripCard(
                                    trip = trip,
                                    rating = trip.rating,
                                    mainActivity = mainActivity,
                                    imageMaxHeight = 100f
                                )
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            Spacer(Modifier.size(40.dp))
                            FaIcon(
                                FaIcons.Searchengin,
                                tint = MaterialTheme.colors.surface,
                                size = 50.dp
                            )

                            Heading(
                                "There is nothing saved yet",
                                Modifier.padding(
                                    cardPadding
                                )
                            )
                            Button(onClick = {
                                mainActivity.goHome()
                            }

                            ) {
                                Text(
                                    "Get inspired", color = MaterialTheme.colors.surface,
                                    modifier = Modifier.padding(
                                        cardPadding
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


