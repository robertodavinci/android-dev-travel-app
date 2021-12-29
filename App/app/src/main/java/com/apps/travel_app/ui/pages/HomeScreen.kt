package com.apps.travel_app.ui.pages

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.apps.travel_app.MainActivity
import com.apps.travel_app.R
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.Trip
import com.apps.travel_app.ui.components.Heading
import com.apps.travel_app.ui.components.Loader
import com.apps.travel_app.ui.components.MainCard
import com.apps.travel_app.ui.components.NetworkError
import com.apps.travel_app.ui.theme.cardPadding
import com.apps.travel_app.ui.theme.pacifico
import com.apps.travel_app.ui.theme.textHeading
import com.apps.travel_app.ui.utils.getTriangularMask
import com.apps.travel_app.ui.utils.isOnline
import com.apps.travel_app.ui.utils.sendPostRequest
import com.apps.travel_app.user
import com.google.android.libraries.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.skydoves.landscapist.glide.GlideImage

lateinit var trips: MutableState<ArrayList<Destination>>
var activeTrip: MutableState<Trip?> = mutableStateOf(null)
var images: MutableState<Boolean> = mutableStateOf(false)

@Composable
fun HomeScreen(navController: NavController, mainActivity: MainActivity) {

/*
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = MaterialTheme.colors.background
    )*/

    images = remember { mutableStateOf(false) }
    trips = remember { mutableStateOf(ArrayList()) }


    fun getActiveTrip() {
        Thread {
            val request = "125"
            val tripText = sendPostRequest(request, action = "trip")
            if (!tripText.isNullOrEmpty()) {
                val gson = Gson()
                val itemType = object : TypeToken<Trip>() {}.type
                val trip: Trip = gson.fromJson(tripText, itemType)
                mainActivity.runOnUiThread {
                    activeTrip.value = trip
                }
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
                        result.add(city)
                    }

                    mainActivity.runOnUiThread {
                        trips.value = result
                    }
                }
            }.start()
        }
    }


    if (!images.value) {
        getImages()
        getActiveTrip()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        item {

            var experienceImage: Bitmap? by remember { mutableStateOf(null) }

            if (experienceImage == null) {
                experienceImage = getTriangularMask(
                    R.drawable.landscape,
                    true,
                    mainActivity.resources
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                val modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                if (experienceImage != null) {
                    GlideImage(
                        imageModel = experienceImage,
                        modifier = modifier,
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.blur),
                    )
                }
                Column(
                    Modifier
                        .fillMaxSize()
                        .height(300.dp)
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,

                    ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                end = cardPadding,
                                start = cardPadding,
                                top = cardPadding * 2,
                                bottom = 10.dp
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val text = with(AnnotatedString.Builder("")) {
                            pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                            append("Hi,")
                            pop()
                            pushStyle(SpanStyle(fontFamily = pacifico))
                            append(user.displayName ?: user.email)
                            toAnnotatedString()
                        }
                        Text(
                            text = text,
                            fontSize = textHeading,
                            color = Color.White
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
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Explore new",
                            color = Color.White,
                            textAlign = Center,
                            fontSize = textHeading,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Experiences",
                            color = Color.White,
                            textAlign = Center,
                            fontFamily = pacifico,
                            fontSize = textHeading * 1.5,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }


            if (!isOnline(mainActivity)) {
                NetworkError()
            } else {
                Spacer(Modifier.height(15.dp))
                Heading(
                    "Top destinations"
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
                        Column(
                            modifier = Modifier
                                .padding(cardPadding)
                        ) {
                            val loadedTrips = ArrayList<Destination>()

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
                            Spacer(Modifier.height(120.dp))
                        }


                    }
                }

            }
        }
    }

}





