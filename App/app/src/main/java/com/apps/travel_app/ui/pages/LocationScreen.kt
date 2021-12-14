package com.apps.travel_app.ui.pages

import FaIcons
import android.util.Log
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.room.Room
import com.apps.travel_app.MainActivity
import com.apps.travel_app.data.rooms.AppDatabase
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.Rating
import com.apps.travel_app.models.Trip
import com.apps.travel_app.ui.components.*
import com.apps.travel_app.ui.theme.cardPadding
import com.apps.travel_app.ui.theme.cardRadius
import com.apps.travel_app.ui.theme.danger
import com.apps.travel_app.ui.theme.mapStyle
import com.apps.travel_app.ui.utils.isOnline
import com.apps.travel_app.ui.utils.rememberMapViewWithLifecycle
import com.apps.travel_app.ui.utils.sendPostRequest
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MapStyleOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.guru.fontawesomecomposelib.FaIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.Math.random


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocationScreen(
    navController: NavController,
    destination: Destination,
    mainActivity: MainActivity
) {

    val db = Room.databaseBuilder(
        mainActivity,
        AppDatabase::class.java, "database-name"
    ).build()

    val loaded: MutableState<Boolean> = remember { mutableStateOf(false) }
    val ratings: MutableState<ArrayList<Rating>> = remember { mutableStateOf(ArrayList()) }
    val facilities: MutableState<ArrayList<Destination>> = remember { mutableStateOf(ArrayList()) }
    val trips: MutableState<ArrayList<Trip>> = remember { mutableStateOf(ArrayList()) }
    val loadingScreen = remember { mutableStateOf(0) }
    val map: MutableState<GoogleMap?> = remember { mutableStateOf(null) }
    val mapLoaded = remember { mutableStateOf(false) }
    val openMap = remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }


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
                    destination.latitude,
                    destination.longitude
                ), 12f
            )
        )

        //map.value?.setOnMarkerClickListener { marker -> markerClick(marker) }
    }

    fun getRatings(destionation: Destination) {
        loaded.value = true

        if (ratings.value.size <= 0) {
            Thread {
                try {
                    val result = ArrayList<Rating>()

                    val request = "${destionation.latitude},${destionation.longitude}"
                    val ratingsText = sendPostRequest(request, action = "ratings")
                    val gson = Gson()
                    val itemType = object : TypeToken<List<Rating>>() {}.type
                    val _ratings: List<Rating> = gson.fromJson(ratingsText, itemType)
                    for (rating in _ratings) {
                        rating.rating = random().toFloat() * 5f
                        result.add(rating)
                    }
                    ratings.value = result
                } catch (e: Exception) {

                }
            }.start()
        }
    }

    fun getFacilities(destionation: Destination) {
        loaded.value = true

        if (facilities.value.size <= 0) {
            Thread {

                try {
                    val request =
                        "{\"lat\":${destionation.latitude},\"lng\":${destionation.longitude},\"type\":\"restaurant\"}"
                    val results = sendPostRequest(request, action = "nearby")
                    val gson = Gson()
                    val itemType = object : TypeToken<List<Destination>>() {}.type
                    val result: ArrayList<Destination> = gson.fromJson(results, itemType)
                    facilities.value = result
                } catch (e: Exception) {

                }
            }.start()
        }
    }

    fun getTrips(destionation: Destination) {
        loaded.value = true

        if (trips.value.size <= 0) {
            Thread {
                try {
                    val request =
                        "[[${destionation.latitude - 1},${destionation.longitude - 1}],[${destionation.latitude - 1},${destionation.longitude + 1}],[${destionation.latitude + 1},${destionation.longitude + 1}],[${destionation.latitude + 1},${destionation.longitude - 1}]]"
                    val results = sendPostRequest(request, action = "polygonTrips")
                    val gson = Gson()
                    val itemType = object : TypeToken<List<Trip>>() {}.type
                    val result: ArrayList<Trip> = gson.fromJson(results, itemType)
                    trips.value = result
                } catch (e: Exception) {

                }
            }.start()
        }
    }

    val activities = arrayListOf(
        "What to do",
        "Where to stay",
        "Where to eat",
        "Crowd-less",
        "1-day trip",
        "Nearby"
    ).toList()


    if (!loaded.value) {
        getRatings(destination)
        getFacilities(destination)
        getTrips(destination)
        Thread {
            try {
                isSaved = db.locationDao().getById(destination.id.toInt()) != null
            } catch (e: Exception) {
                Log.e("ERROR", e.localizedMessage)
            }
        }.start()

    }


    val scrollState = rememberScrollState()
    val maxScroll = 100
    var percentage = scrollState.value.toFloat() / maxScroll
    percentage = if (percentage > 1) 1f else percentage

    var mapView: MapView? = null
    if (loadingScreen.value > 5)
        mapView = rememberMapViewWithLifecycle()

    BoxWithConstraints {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colors.background)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Column {

                Box(
                    modifier = Modifier.heightIn(0.dp, 250.dp)
                ) {
                    MainCard(
                        destination = destination,
                        rating = 3.5f,
                        clickable = false,
                        views = 206,
                        mainActivity = mainActivity,
                        padding = (cardPadding * (1 - percentage)),
                        radius = (cardRadius * (1 - percentage))
                    )
                }


                FlexibleRow(
                    alignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(cardPadding / 2)
                        .fillMaxWidth()
                ) {
                    activities.forEach { activity ->
                        Button(onClick = {}, modifier = Modifier.padding(5.dp)) {
                            Text(activity)
                        }
                    }
                    Button(onClick = { openMap.value = true }, modifier = Modifier.padding(5.dp)) {
                        FaIcon(
                            FaIcons.LocationArrow,
                            tint = MaterialTheme.colors.surface
                        )
                    }
                    Button(onClick = {
                        Thread {
                            try {
                                if (!isSaved)
                                    db.locationDao().insertAll(destination.toLocation())
                                else
                                    db.locationDao().delete(destination.toLocation())
                                isSaved = !isSaved
                            } catch (e: Exception) {

                            }
                        }.start()
                    }, modifier = Modifier.padding(5.dp)) {
                        FaIcon(
                            if (isSaved) FaIcons.Heart else FaIcons.HeartRegular,
                            tint = if (isSaved) danger else MaterialTheme.colors.surface
                        )
                    }
                }

                Heading(
                    "Top trips"
                )

                LazyRow(
                    modifier = Modifier.padding(cardPadding)
                ) {
                    items(trips.value.size) { i ->
                        val trip = trips.value[i]
                        Box(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            TripCard(
                                trip = trip,
                                rating = 4.5f,
                                padding = 5.dp,
                                shadow = 10.dp,
                                badges = arrayListOf("Youth", "Nature", "Crowd-less"),
                                mainActivity = mainActivity,
                                infoScale = 0.8f,
                                imageMaxHeight = 130f
                            )
                        }
                    }
                }

                if (!isSaved && !isOnline(mainActivity)) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Center) {
                            NetworkError()
                        }
                    }else {
                    Heading(
                        "Facilities"
                    )

                    LazyRow(
                        modifier = Modifier.padding(cardPadding)
                    ) {
                        items(facilities.value.size) { i ->

                            val facility = facilities.value[i]
                            val badges = arrayListOf("€".repeat(facility.priceLevel.toInt() + 1))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                MainCard(
                                    destination = facility,
                                    rating = facility.rating,
                                    padding = 5.dp,
                                    shadow = 10.dp,
                                    imageMaxHeight = 100f,
                                    mainActivity = mainActivity,
                                    infoScale = 0.8f,
                                    icon = FaIcons.Google,
                                    badges = badges,
                                    isGooglePlace = true
                                )
                            }
                        }
                    }

                    Heading(
                        "Top ratings"
                    )

                    Box(
                        modifier = Modifier.padding(bottom = 60.dp)
                    ) {
                        if (ratings.value.size <= 0) {
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

                                ratings.value.forEach { rating ->
                                    RatingCard(
                                        rating
                                    )
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
        }

    }
    val scale: Float by animateFloatAsState(
        if (openMap.value) 1f else 0f, animationSpec = tween(
            durationMillis = 500,
            easing = LinearOutSlowInEasing
        )
    )
    if (openMap.value) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = {
                openMap.value = false
            },

            ) {
            Column(
                modifier = Modifier
                    .scale(scale)
                    .height(200.dp)
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
                    if (mapView != null) {
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

                    } else {
                        Text(
                            "Loading...",
                            color = MaterialTheme.colors.surface,
                            modifier = Modifier.padding(
                                cardPadding
                            )
                        )
                        loadingScreen.value++
                    }
                }
            }
        }
    }

}







