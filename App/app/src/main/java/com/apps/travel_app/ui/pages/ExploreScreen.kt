package com.apps.travel_app.ui.pages

import FaIcons
import android.content.Intent
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.apps.travel_app.MainActivity
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.Trip
import com.apps.travel_app.ui.components.Heading
import com.apps.travel_app.ui.components.MainCard
import com.apps.travel_app.ui.components.TripCard
import com.apps.travel_app.ui.components.login.LoginActivity
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.ui.utils.markerPopUp
import com.apps.travel_app.ui.utils.numberedMarker
import com.apps.travel_app.ui.utils.rememberMapViewWithLifecycle
import com.apps.travel_app.ui.utils.sendPostRequest
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MapStyleOptions
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.guru.fontawesomecomposelib.FaIcon
import com.guru.fontawesomecomposelib.FaIconType
import com.skydoves.landscapist.CircularReveal
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Math.random
import kotlin.math.roundToInt


val filterIcons = arrayListOf(
    FilterIcon(FaIcons.Cocktail, "Bar"),
    FilterIcon(FaIcons.DrumstickBite, "Restaurant"),
    FilterIcon(FaIcons.Hotel, "Lodging"),
    FilterIcon(FaIcons.DollarSign, "Bank"),
    FilterIcon(FaIcons.Opencart, "Supermarket"),
    FilterIcon(FaIcons.GasPump, "Gas_Station"),
    FilterIcon(FaIcons.HatCowboy, "Tourist_attraction"),
    FilterIcon(FaIcons.Monument, "Museum"),
    FilterIcon(FaIcons.Running, "Gym"),
    FilterIcon(FaIcons.Taxi, "Taxi_stand"),
    FilterIcon(FaIcons.Bus, "Bus_station"),
    FilterIcon(FaIcons.Train, "Train_station"),
    FilterIcon(FaIcons.Parking, "parking"),
    FilterIcon(FaIcons.Film, "cinema"),
    FilterIcon(FaIcons.Coffee, "cafe"),
    FilterIcon(FaIcons.Church, "church"),
    FilterIcon(FaIcons.Mosque, "mosque")
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExploreScreen(navController: NavController, mainActivity: MainActivity) {

    val trips = remember {
        mutableStateOf(ArrayList<Trip>())
    }
    val cities = remember {
        mutableStateOf(ArrayList<Destination>())
    }
    val places = remember {
        mutableStateOf(ArrayList<Destination>())
    }

    var openFilters by remember { mutableStateOf(false) }
    val filters = remember { mutableStateListOf<String>() }
    filters.add("Bar")
    filters.add("Restaurant")

    val loadingScreen = remember { mutableStateOf(0) }
    val map: MutableState<GoogleMap?> = remember { mutableStateOf(null) }
    val mapLoaded = remember { mutableStateOf(false) }
    var mapVisible by remember { mutableStateOf(true) }
    var mapView: MapView? = null
    if (loadingScreen.value > 5)
        mapView = rememberMapViewWithLifecycle()




    fun addMarker(position: LatLng, index: Int, name: String) {
        if (map.value == null)
            return
        val markerOptions = MarkerOptions()
            .position(
                position
            )
            .icon(numberedMarker(index + 1))
            .title(name)
            .zIndex(5f)
        val marker = map.value!!.addMarker(markerOptions)

        markerPopUp(marker)
    }


    fun Search(text: String, types: List<String>) {
        val region = map.value?.projection?.visibleRegion ?: return

        Thread {
            val points = arrayListOf(
                region.farLeft,
                region.nearLeft,
                region.nearRight,
                region.farRight,
                region.farLeft
            )
            val request = "{\"area\":" + points.joinToString(",", "[", "]") { e ->
                "[${e.latitude},${e.longitude}]"
            } + ", \"text\": \"$text\", \"type\": \"" + types.joinToString("|").lowercase() + "\"}"
            println(request)
            val resultText = sendPostRequest(request, action = "search")
            if (!resultText.isNullOrEmpty()) {
                val gson = Gson()
                val itemType = object : TypeToken<Response>() {}.type
                val response: Response = gson.fromJson(resultText, itemType)

                trips.value = response.trips
                cities.value = response.cities
                places.value = response.places

                var index = 0

                mainActivity.runOnUiThread {
                    if (map.value != null) {
                        map.value!!.clear()
                        trips.value.forEach {
                            addMarker(
                                LatLng(
                                    it.startingPoint.latitude,
                                    it.startingPoint.longitude
                                ), index++, it.name
                            )
                        }
                        cities.value.forEach {
                            addMarker(
                                LatLng(it.latitude, it.longitude),
                                index++,
                                it.name
                            )
                        }
                        places.value.forEach {
                            addMarker(
                                LatLng(it.latitude, it.longitude),
                                index++,
                                it.name
                            )
                        }
                    }
                }
            }
        }.start()
    }

    fun mapInit() {
        map.value!!.uiSettings.isZoomControlsEnabled = false

        map.value?.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                mainActivity,
                mapStyle
            )
        )

        map.value!!.uiSettings.isMapToolbarEnabled = false

        map.value?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    45.0,
                    11.0
                ), 12f
            )
        )
    }

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = colors.background
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),

        ) {
        Heading(
            "Take a deep breath...",
            color = Color.White,
            modifier = Modifier.padding(cardPadding)
        )
        Text(
            "Just a moment for you to get inspired by the wonder of our world",
            color = White,
            modifier = Modifier
                .padding(cardPadding)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = textSmall
        )
        Heading(
            "... and make it yours",
            color = Color.White,
            modifier = Modifier.padding(cardPadding)
        )
        Row( verticalAlignment = Alignment.CenterVertically) {
            Card(
                modifier = Modifier
                    .padding(cardPadding)
                    .weight(1f)
                    .heightIn(0.dp, 150.dp),
                elevation = cardElevation,
                shape = RoundedCornerShape(cardRadius)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    val intent =
                                        Intent(mainActivity, InspirationActivity::class.java)
                                    startActivity(mainActivity, intent, null)
                                }
                            )
                        }
                ) {
                    GlideImage(
                        modifier = Modifier.fillMaxSize(),
                        imageModel = "https://safeitaly.org/travel/3.png?n=1",
                        contentScale = ContentScale.Crop,
                    )
                    Heading(
                        "Map drawing",
                        modifier = Modifier
                            .align(CenterStart)
                            .padding(cardPadding),
                        color = Color.White
                    )
                }

            }
        }
        Row( verticalAlignment = Alignment.CenterVertically) {
            Card(
                modifier = Modifier
                    .padding(cardPadding)
                    .weight(1f)
                    .heightIn(0.dp, 150.dp),
                elevation = cardElevation,
                shape = RoundedCornerShape(cardRadius)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTapGestures (
                                onTap = {
                                    val intent = Intent(mainActivity, InspirationActivity::class.java)
                                    startActivity(mainActivity, intent, null)
                                }
                            )
                        }
                ) {
                    GlideImage(
                        modifier = Modifier.fillMaxSize(),
                        imageModel = "https://safeitaly.org/travel/2.png?n=1",
                        contentScale = ContentScale.Crop,
                    )
                    Heading(
                        "The wall",
                        modifier = Modifier
                            .align(CenterStart)
                            .padding(cardPadding),
                        color = Color.White
                    )
                }

            }

            Card(
                modifier = Modifier
                    .padding(cardPadding)
                    .weight(1f)
                    .heightIn(0.dp, 150.dp),
                elevation = cardElevation,
                shape = RoundedCornerShape(cardRadius)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTapGestures (
                                onTap = {
                                    val intent = Intent(mainActivity, AroundMeActivity::class.java)
                                    startActivity(mainActivity, intent, null)
                                }
                            )
                        }
                ) {
                    GlideImage(
                        modifier = Modifier.fillMaxSize(),
                        imageModel = "https://safeitaly.org/travel/1.png?n=1",
                        contentScale = ContentScale.Crop,
                    )
                    Heading(
                        "Around you",
                        modifier = Modifier
                            .align(CenterStart)
                            .padding(cardPadding),
                        color = Color.White
                    )
                }

            }
        }
    }
}


class FilterIcon(var icon: FaIconType, var name: String) {
}

class Response {
    var places: ArrayList<Destination> = arrayListOf()
    var cities: ArrayList<Destination> = arrayListOf()
    var trips: ArrayList<Trip> = arrayListOf()
}
