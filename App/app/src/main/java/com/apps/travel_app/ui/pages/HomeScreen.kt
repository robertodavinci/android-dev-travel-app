package com.apps.travel_app.ui.pages

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
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
import com.apps.travel_app.ui.components.*
import com.apps.travel_app.ui.theme.cardPadding
import com.apps.travel_app.ui.theme.pacifico
import com.apps.travel_app.ui.theme.primaryColor
import com.apps.travel_app.ui.theme.textHeading
import com.apps.travel_app.ui.utils.getTriangularMask
import com.apps.travel_app.ui.utils.isOnline
import com.apps.travel_app.ui.utils.sendPostRequest
import com.apps.travel_app.user
import com.google.android.libraries.maps.model.LatLng
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.skydoves.landscapist.glide.GlideImage


@Composable
fun HomeScreen(navController: NavController, mainActivity: MainActivity) {

    val trips = remember { mutableStateOf(ArrayList<Trip>()) }
    val adventures = remember { mutableStateOf(ArrayList<Trip>()) }
    val cities = remember { mutableStateOf(ArrayList<Destination>()) }
    val tabs = arrayListOf(
        "Destinations",
        "Adventures",
        "Trips")

    fun getImages() {

        if (trips.value.size <= 0 && adventures.value.size <= 0 && cities.value.size <= 0) {
            Thread {

                val citiesText = sendPostRequest("", action = "home")
                if (!citiesText.isNullOrEmpty()) {
                    val gson = Gson()
                    val itemType = object : TypeToken<HomeResponse>() {}.type
                    val output: HomeResponse = gson.fromJson(citiesText, itemType)

                    mainActivity.runOnUiThread {
                        trips.value = output.trips
                        cities.value = output.cities
                        adventures.value = output.adventures
                    }
                }
            }.start()
        }
    }


    if (trips.value.size <= 0 && adventures.value.size <= 0 && cities.value.size <= 0) {
        getImages()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
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
                var tab by remember { mutableStateOf(0) }
                TabRow(
                    selectedTabIndex = tab,
                    divider = {},
                    backgroundColor = Transparent,
                    contentColor = Transparent
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            text = {
                                Column {
                                    Text(title, color = colors.surface)
                                    Spacer(
                                        Modifier
                                            .height(3.dp)
                                            .width(20.dp)
                                            .background(
                                                if (tab == index) primaryColor else Transparent
                                            )
                                    )

                                }
                            },
                            selected = tab == index,
                            onClick = { tab = index }
                        )
                    }
                }
                Heading(
                    "Top ${tabs[tab]}"
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
                            if (tab == 0) {
                                val loadedCities = ArrayList<Destination>()
                                cities.value.forEachIndexed { index, destination ->
                                    if (!loadedCities.contains(destination)) {
                                        loadedCities.add(destination)
                                        Row {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(0.dp, 120.dp)
                                            ) {
                                                MainCard(
                                                    destination = destination,
                                                    rating = destination.rating,
                                                    padding = 5.dp,
                                                    shadow = 10.dp,
                                                    mainActivity = mainActivity
                                                )
                                            }
                                            if (destination.rating <= 2.5f && index < cities.value.size - 1) {
                                                val trip2 = cities.value[index + 1]
                                                loadedCities.add(trip2)
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
                            } else {
                                val loadedTrips = ArrayList<Trip>()
                                val array = (if (tab == 1) adventures.value else trips.value)
                                array.forEachIndexed { index, trip ->
                                    if (!loadedTrips.contains(trip)) {
                                        loadedTrips.add(trip)
                                        Row {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(0.dp, 120.dp)
                                            ) {
                                                TripCard(
                                                    trip = trip,
                                                    rating = trip.rating,
                                                    padding = 5.dp,
                                                    shadow = 10.dp,
                                                    mainActivity = mainActivity
                                                )
                                            }
                                            if (trip.rating <= 2.5f && index < array.size - 1) {
                                                val trip2 = array[index + 1]
                                                loadedTrips.add(trip2)
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .heightIn(0.dp, 120.dp)
                                                ) {
                                                    TripCard(
                                                        trip = trip2,
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
                            Spacer(Modifier.height(120.dp))
                        }


                    }
                }

            }
        }
    }

}

class HomeResponse {
    var adventures: ArrayList<Trip> = arrayListOf()
    var cities: ArrayList<Destination> = arrayListOf()
    var trips: ArrayList<Trip> = arrayListOf()
}





