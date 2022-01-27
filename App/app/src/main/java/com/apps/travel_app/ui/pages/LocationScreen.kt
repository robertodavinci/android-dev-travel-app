package com.apps.travel_app.ui.pages

import FaIcons
import androidx.activity.viewModels
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
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.room.Room
import com.apps.travel_app.MainActivity
import com.apps.travel_app.data.room.AppDatabase
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.Rating
import com.apps.travel_app.models.Trip
import com.apps.travel_app.ui.components.*
import com.apps.travel_app.ui.components.login.LoginViewModel
import com.apps.travel_app.ui.pages.viewmodels.LocationViewModel
import com.apps.travel_app.ui.theme.*
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

    val viewModel = remember { LocationViewModel(destination, db, mainActivity) }

    fun mapInit() {
        viewModel.map.value!!.uiSettings.isZoomControlsEnabled = false

        viewModel.map.value?.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                mainActivity,
                mapStyle
            )
        )

        viewModel.map.value!!.uiSettings.isMapToolbarEnabled = false

        viewModel.map.value?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    destination.latitude,
                    destination.longitude
                ), 12f
            )
        )
    }

    val activities = arrayListOf(destination.type)

    val scrollState = rememberScrollState()
    val maxScroll = 100
    var percentage = scrollState.value.toFloat() / maxScroll
    percentage = if (percentage > 1) 1f else percentage

    var mapView: MapView? = null
    if (viewModel.loadingScreen.value > 5)
        mapView = rememberMapViewWithLifecycle()

    BoxWithConstraints {
        Box(
            modifier = Modifier
                .background(colors.background)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = cardPadding * 2)
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
                        radius = (cardRadius * (1 - percentage)),
                        depthCards = true
                    )
                }

                Text(
                    destination.description,
                    color = colors.surface,
                    fontSize = textSmall,
                    modifier = Modifier.padding(cardPadding)
                )


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
                    Button(onClick = { viewModel.openMap.value = true }, modifier = Modifier.padding(5.dp)) {
                        FaIcon(
                            FaIcons.LocationArrow,
                            tint = colors.surface
                        )
                    }
                    Button(onClick = {
                        Thread {
                            try {
                                if (!viewModel.isSaved.value) {
                                    val location = destination.toLocation()
                                    location.saved = true
                                    db.locationDao().insertAll(location)

                                } else {
                                    val location = destination.toLocation()
                                    location.saved = false
                                    db.locationDao().delete(location)
                                }
                                viewModel.isSaved.value = !viewModel.isSaved.value
                            } catch (e: Exception) {

                            }
                        }.start()
                    }, modifier = Modifier.padding(5.dp)) {
                        FaIcon(
                            if (viewModel.isSaved.value) FaIcons.Heart else FaIcons.HeartRegular,
                            tint = if (viewModel.isSaved.value) danger else colors.surface
                        )
                    }
                }
                if (viewModel.trips.value.size > 0) {
                    Heading(
                        "Trips"
                    )
                }

                LazyRow(
                    modifier = Modifier.padding(cardPadding)
                ) {
                    items(viewModel.trips.value.size) { i ->
                        val trip = viewModel.trips.value[i]
                        Box(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            TripCard(
                                trip = trip,
                                rating = 4.5f,
                                padding = 5.dp,
                                shadow = 10.dp,
                                badges = trip.attributes.subList(
                                    0,
                                    if (trip.attributes.size > 3) 3 else trip.attributes.size
                                ),
                                mainActivity = mainActivity,
                                infoScale = 0.8f,
                                imageMaxHeight = 200f
                            )
                        }
                    }
                }

                if (!viewModel.isSaved.value && !isOnline(mainActivity)) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Center) {
                        NetworkError()
                    }
                } else {
                    if (viewModel.facilities.value.size > 0) {
                        Heading(
                            "Attractions"
                        )
                    }

                    LazyRow(
                        modifier = Modifier.padding(cardPadding)
                    ) {
                        items(viewModel.facilities.value.size) { i ->

                            val facility = viewModel.facilities.value[i]
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
                                    imageMaxHeight = 200f,
                                    mainActivity = mainActivity,
                                    infoScale = 0.8f,
                                    icon = FaIcons.Google,
                                    //badges = badges,
                                    isGooglePlace = true
                                )
                            }
                        }
                    }
                    if (viewModel.ratings.value.size > 0) {
                        Heading(
                            "Ratings"
                        )
                    }

                    Box(
                        modifier = Modifier.padding(bottom = 60.dp)
                    ) {

                        Column(
                            modifier = Modifier
                                .padding(cardPadding)
                        ) {

                            viewModel.ratings.value.forEach { rating ->
                                RatingCard(
                                    rating
                                )
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
        if (viewModel.openMap.value) 1f else 0f, animationSpec = tween(
            durationMillis = 500,
            easing = LinearOutSlowInEasing
        )
    )
    if (viewModel.openMap.value) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = {
                viewModel.openMap.value = false
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
                    .background(colors.background)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    if (mapView != null) {
                        AndroidView({ mapView }) { mapView ->
                            CoroutineScope(Dispatchers.Main).launch {
                                if (!viewModel.mapLoaded.value) {
                                    mapView.getMapAsync { mMap ->
                                        if (!viewModel.mapLoaded.value) {
                                            viewModel.map.value = mMap
                                            mapInit()
                                            viewModel.mapLoaded.value = true
                                        }
                                    }
                                }
                            }
                        }

                    } else {
                        Text(
                            "Loading...",
                            color = colors.surface,
                            modifier = Modifier.padding(
                                cardPadding
                            )
                        )
                        viewModel.loadingScreen.value++
                    }
                }
            }
        }
    }

}







