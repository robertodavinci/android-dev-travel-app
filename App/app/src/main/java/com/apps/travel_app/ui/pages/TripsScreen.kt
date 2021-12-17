package com.apps.travel_app.ui.pages

import FaIcons
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.apps.travel_app.MainActivity
import com.apps.travel_app.data.room.AppDatabase
import com.apps.travel_app.data.room.Location
import com.apps.travel_app.data.room.Trip
import com.apps.travel_app.models.Destination
import com.apps.travel_app.ui.components.Button
import com.apps.travel_app.ui.components.Heading
import com.apps.travel_app.ui.components.MainCard
import com.apps.travel_app.ui.components.TripCard
import com.apps.travel_app.ui.theme.cardPadding
import com.apps.travel_app.ui.theme.primaryColor
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.guru.fontawesomecomposelib.FaIcon

@Composable
fun TripsScreen(mainActivity: MainActivity) {

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = primaryColor
    )

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
        val locations = db.locationDao().getAll() as ArrayList<Location>
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
                        onClick = {
                            mainActivity.createTrip()
                        },
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
                            MainCard(
                                destination = destination,
                                rating = destination.rating,
                                mainActivity = mainActivity,
                                imageMaxHeight = 100f
                            )
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

                    Spacer(
                        modifier = Modifier.height(80.dp)
                    )
                }
            }
        }
    }
}
