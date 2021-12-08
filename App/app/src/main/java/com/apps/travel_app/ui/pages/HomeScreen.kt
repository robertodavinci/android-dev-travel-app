package com.apps.travel_app.ui.pages

import android.util.Log
import com.apps.travel_app.R
import androidx.compose.material.MaterialTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.apps.travel_app.MainActivity
import com.apps.travel_app.models.Country
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.Trip
import com.apps.travel_app.models.addCountry
import com.apps.travel_app.ui.components.Heading
import com.apps.travel_app.ui.components.Loader
import com.apps.travel_app.ui.components.MainCard
import com.apps.travel_app.ui.components.TripCard
import com.apps.travel_app.ui.theme.cardPadding
import com.apps.travel_app.ui.theme.textHeading
import com.apps.travel_app.ui.utils.sendPostRequest
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.libraries.maps.model.LatLng
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.skydoves.landscapist.glide.GlideImage
import java.lang.Math.random

lateinit var trips: MutableState<ArrayList<Destination>>
var activeTrip: MutableState<Trip?> = mutableStateOf(null)
var images: MutableState<Boolean> = mutableStateOf(false)

@Composable
fun HomeScreen(navController: NavController, mainActivity: MainActivity) {


    Log.d("CurUsr --- ", Firebase.auth.currentUser.toString())
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = MaterialTheme.colors.background
    )

    images = remember { mutableStateOf(false) }
    trips = remember { mutableStateOf(ArrayList()) }

    if (!images.value) {
        getImages()
        getActiveTrip()
    }


    // TRY
    //addCountry(Firebase.firestore, Country("Germany", "GER", "EUR"))


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = cardPadding, start = cardPadding, top = cardPadding, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Hi ${mainActivity.user.displayName ?: mainActivity.user.email}",
                fontSize = textHeading,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.surface
            )
            IconButton(onClick = {
                navController.navigate("profile") {
                    navController.graph.startDestinationRoute?.let { route ->
                        popUpTo(route) {
                            saveState = true
                        }
                    }
                    launchSingleTop = true
                    restoreState = true
                }

            }) {
                GlideImage(
                    imageModel = R.mipmap.icon,
                    contentDescription = "",
                    modifier = Modifier
                        .width(40.dp)
                        .height(40.dp)
                        .graphicsLayer {
                            shape = RoundedCornerShape(100)
                            clip = true
                        }
                )
            }
        }

        if (activeTrip.value == null) {
            val destination = Destination()
            destination.name = "Milan"
            destination.thumbnailUrl =
                "https://www.welcometoitalia.com/wp-content/uploads/2020/10/galleria_Milan.jpg"
            MainCard(
                destination = destination,
                rating = 3.5f,
                badges = arrayListOf("Cultural", "Youth"),
                views = 206,
                mainActivity = mainActivity,
                imageMaxHeight = 200f
            )
        } else {
            Heading("Active trip")
            TripCard(
                trip = activeTrip.value!!,
                rating = 4.8f,
                imageMaxHeight = 200f,
                mainActivity = mainActivity,
                active = true
            )
        }



        Heading(
            "Top trips"
        )

        Box(
            modifier = Modifier.padding(bottom = 40.dp)
        ) {
            if (trips.value.size <= 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .alpha(0.5f)
                        .padding(50.dp)
                ) {
                    Loader()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(cardPadding)
                ) {
                    val loadedTrips = ArrayList<Destination>()
                    item(trips.value.size) {
                        trips.value.forEachIndexed { index, trip ->
                            if (!loadedTrips.contains(trip)) {
                                loadedTrips.add(trip)
                                Row {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(0.dp, 120.dp)
                                    ) {
                                        MainCard(
                                            destination = trip,
                                            rating = trip.rating,
                                            padding = 5.dp,
                                            shadow = 10.dp,
                                            mainActivity = mainActivity
                                        )
                                    }
                                    if (trip.rating <= 2.5f && index < trips.value.size - 1) {
                                        val trip2 = trips.value[index + 1]
                                        loadedTrips.add(trip2)
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(0.dp, 120.dp)
                                        ) {
                                            MainCard(
                                                destination = trip2,
                                                rating = trip2.rating,
                                                padding = 5.dp,
                                                shadow = 10.dp,
                                                mainActivity = mainActivity
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
            Box(
                modifier = Modifier
                    .padding(cardPadding)
                    .fillMaxWidth()
                    .height(30.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colors.background,
                                Color.Transparent
                            )
                        )
                    )
            )
        }

    }
}

fun getActiveTrip(){
    Thread {
        val request = "125"
        val tripText = sendPostRequest(request, action = "trip")
        if (!tripText.isNullOrEmpty()) {
            val gson = Gson()
            val itemType = object : TypeToken<Trip>() {}.type
            val trip: Trip = gson.fromJson(tripText, itemType)
            activeTrip.value = trip
        }
    }.start()
}

fun getImages() {
    images.value = true

    if (trips.value.size <= 0) {
        Thread {
            val result = ArrayList<Destination>()
            val points = arrayListOf(
                LatLng(0.0, 0.0),
                LatLng(80.0, 0.0),
                LatLng(80.0, 20.0),
                LatLng(0.0, 20.0),
                LatLng(0.0, 0.0)
            )
            val request = points.joinToString(",", "[", "]") { e ->
                "[${e.latitude},${e.longitude}]"
            }
            val citiesText = sendPostRequest(request, action = "polygonCities")
            if (!citiesText.isNullOrEmpty()) {
                val gson = Gson()
                val itemType = object : TypeToken<List<Destination>>() {}.type
                val cities: List<Destination> = gson.fromJson(citiesText, itemType)
                for (city in cities) {

                    city.rating = random().toFloat() * 5f
                    result.add(city)
                }
                trips.value = result
            }
        }.start()
    }
}




