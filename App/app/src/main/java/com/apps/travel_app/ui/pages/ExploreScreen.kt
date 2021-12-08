package com.apps.travel_app.ui.pages

import FaIcons
import android.content.Intent
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Math.random
import kotlin.math.roundToInt


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

    var searchTerm by remember { mutableStateOf("") }

    val result = places.value.size > 0 || trips.value.size > 0 || cities.value.size > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (!result) Color(0x44000000) else colors.background)
            ,
        verticalArrangement = if (!result) Arrangement.Center else Arrangement.Top
    ) {
        Column(
            modifier = Modifier.padding(cardPadding).graphicsLayer {
                shape = RoundedCornerShape(cardRadius)
                clip = true
            }.background(colors.background)
        ) {
            if (mapView != null && mapVisible) {
                Card(
                    modifier = Modifier
                        .height(200.dp)
                        .padding(cardPadding),
                    elevation = cardElevation,
                    shape = RoundedCornerShape(
                        cardRadius
                    )
                ) {
                    Box {
                        AndroidView({ mapView }) { mapView ->
                            CoroutineScope(Dispatchers.Main).launch {
                                if (!mapLoaded.value) {
                                    mapView.getMapAsync { mMap ->
                                        if (!mapLoaded.value) {
                                            map.value = mMap
                                            mapInit()
                                            mapLoaded.value = true
                                        }
                                    }
                                }
                            }
                        }
                        Text(
                            modifier = Modifier
                                .align(Center)
                                .background(Color(0x44000000)),
                            color = colors.surface,
                            text = "Drag to choose an area"
                        )
                    }
                }

            } else if (mapView == null) {
                Text(
                    "Loading...",
                    color = colors.surface,
                    modifier = Modifier.padding(
                        cardPadding
                    )
                )
                loadingScreen.value++
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .padding(cardPadding),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                TextField(
                    value = searchTerm, onValueChange = { searchTerm = it },
                    shape = RoundedCornerShape(cardRadius),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        backgroundColor = colors.background,
                    ),
                    placeholder = {
                        Text(
                            "Search",
                            color = colors.surface,
                            modifier = Modifier.alpha(0.5f)
                        )
                    },
                    trailingIcon = {
                        Row {
                            IconButton(onClick = {
                                openFilters = true
                            }) {
                                FaIcon(FaIcons.Filter, tint = colors.surface)
                            }
                            IconButton(onClick = {
                                Search(
                                    searchTerm,
                                    types = filters
                                )
                            }) {
                                FaIcon(FaIcons.Search, tint = colors.surface)
                            }
                            IconButton(onClick = {
                                val intent = Intent(mainActivity, AroundMeActivity::class.java)
                                startActivity(mainActivity, intent, null)
                            }) {
                                FaIcon(FaIcons.StreetView, tint = colors.surface)
                            }
                        }
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = colors.surface,
                        fontWeight = FontWeight.Bold
                    ),
                )
            }
        }
        LazyColumn {
            if (places.value.size > 0) {
                item {
                    Heading("Places")
                }
            }
            val loaded = arrayListOf<Destination>()
            item {
                places.value.forEachIndexed { index, place ->
                    if (!loaded.contains(place)) {
                        loaded.add(place)
                        Row {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(0.dp, 120.dp)
                            ) {
                                MainCard(
                                    infoScale = 0.6f,
                                    destination = place,
                                    rating = place.rating,
                                    mainActivity = mainActivity,
                                    icon = FaIcons.Google,
                                    imageMaxHeight = 120f,
                                    imageMinHeight = 120f,
                                    isGooglePlace = true
                                )
                            }
                            if (index < places.value.size - 1) {
                                val place2 = places.value[index + 1]
                                loaded.add(place2)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(0.dp, 120.dp)
                                ) {
                                    MainCard(
                                        infoScale = 0.6f,
                                        destination = place2,
                                        rating = place2.rating,
                                        mainActivity = mainActivity,
                                        icon = FaIcons.Google,
                                        imageMaxHeight = 150f,
                                        imageMinHeight = 120f,
                                        isGooglePlace = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (trips.value.size > 0) {
                item {
                    Heading("Trips")
                }
            }
            val loadedTrips = arrayListOf<Trip>()
            item {
                trips.value.forEachIndexed { index, trip ->
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
                                    imageMaxHeight = 150f,
                                    infoScale = 0.6f,
                                )
                            }
                            if (index < trips.value.size - 1) {
                                val trip2 = trips.value[index + 1]
                                loadedTrips.add(trip2)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(0.dp, 120.dp)
                                ) {
                                    TripCard(
                                        infoScale = 0.6f,
                                        trip = trip2,
                                        rating = trip2.rating,
                                        imageMaxHeight = 150f
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (cities.value.size > 0) {
                item {
                    Heading("Destinations")
                }
            }
            loaded.clear()
            item {
                cities.value.forEachIndexed { index, city ->
                    if (!loaded.contains(city)) {
                        loaded.add(city)
                        Row {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(0.dp, 120.dp)
                            ) {
                                MainCard(
                                    destination = city,
                                    rating = city.rating,
                                    mainActivity = mainActivity,
                                    icon = FaIcons.Google,
                                    imageMaxHeight = 150f,
                                    infoScale = 0.6f,
                                )
                            }
                            if (index < cities.value.size - 1) {
                                val place2 = cities.value[index + 1]
                                loaded.add(place2)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(0.dp, 120.dp)
                                ) {
                                    MainCard(
                                        infoScale = 0.6f,
                                        destination = place2,
                                        rating = place2.rating,
                                        mainActivity = mainActivity,
                                        icon = FaIcons.Google,
                                        imageMaxHeight = 150f
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(65.dp))
            }


        }

    }

    val scale: Float by animateFloatAsState(
        if (openFilters) 1f else 0f, animationSpec = tween(
            durationMillis = 500,
            easing = LinearOutSlowInEasing
        )
    )
    if (openFilters) {

        Dialog(
            onDismissRequest = {
                openFilters = false
            },

            ) {
            LazyVerticalGrid(
                cells = GridCells.Fixed(5),
                modifier = Modifier
                    .scale(scale)
                    .height(200.dp)
                    .padding(0.dp)
                    .graphicsLayer {
                        shape = RoundedCornerShape(cardRadius)
                        clip = true
                    }
                    .background(colors.background)
                    .fillMaxWidth()
            ) {
                items(filterIcons) { filter ->
                    com.apps.travel_app.ui.components.Button(
                        onClick = {
                            if (filters.contains(filter.name))
                                filters.remove(filter.name)
                            else
                                filters.add(filter.name)
                        },
                        modifier = Modifier.padding(5.dp),
                        background = if (filters.contains(filter.name)) primaryColor else colors.onBackground
                    ) {
                        FaIcon(faIcon = filter.icon, tint = colors.surface)
                    }
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
