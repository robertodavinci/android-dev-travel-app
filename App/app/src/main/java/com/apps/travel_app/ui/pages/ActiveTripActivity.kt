package com.apps.travel_app.ui.pages

import FaIcons
import android.os.Bundle
import androidx.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.apps.travel_app.R
import com.apps.travel_app.models.MediumType
import com.apps.travel_app.models.Rating
import com.apps.travel_app.models.Trip
import com.apps.travel_app.models.TripDestination
import com.apps.travel_app.ui.components.*
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.ui.utils.markerPopUp
import com.apps.travel_app.ui.utils.numberedMarker
import com.apps.travel_app.ui.utils.rememberMapViewWithLifecycle
import com.apps.travel_app.ui.utils.sendPostRequest
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.guru.fontawesomecomposelib.FaIcon
import com.guru.fontawesomecomposelib.FaIconType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActiveTripActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val systemTheme = sharedPref.getBoolean("darkTheme", true)

        val tripId = intent.getIntExtra("tripId", 0)

        setContent {
            var trip: Trip? by remember { mutableStateOf(null) }
            Thread {
                val request = tripId.toString() // NON-NLS
                val ratingsText = sendPostRequest(request, action = "trip") // NON-NLS
                val gson = Gson()
                val itemType = object : TypeToken<Trip>() {}.type
                trip = gson.fromJson(ratingsText, itemType)
            }.start()
            Travel_AppTheme(systemTheme = systemTheme) {

                if (trip != null) {
                    TripScreen(trip!!)
                }
            }
        }
    }


    @OptIn(
        ExperimentalFoundationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class
    )
    @Composable
    fun TripScreen(
        trip: Trip
    ) {

       /* val systemUiController = rememberSystemUiController()
        systemUiController.setSystemBarsColor(
            color = colors.background
        )*/

        var isCompleted by remember { mutableStateOf(false) }
        val open: MutableState<Boolean> = remember { mutableStateOf(false) }
        var reviewed by remember { mutableStateOf(false) }
        val loadingScreen = remember { mutableStateOf(0) }
        val map: MutableState<GoogleMap?> = remember { mutableStateOf(null) }
        val mapLoaded = remember { mutableStateOf(false) }
        var selectedDay by remember { mutableStateOf(0) }

        var icon: FaIconType? = null
        weatherChip(
            latitude = trip.mainDestination.latitude,
            longitude = trip.mainDestination.longitude
        ) {
            icon = it
        }

        var steps by remember { mutableStateOf(trip.destinationsPerDay[selectedDay]) }

        fun mapInit() {
            map.value!!.uiSettings.isZoomControlsEnabled = false

            map.value?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    mapStyle
                )
            )

            map.value!!.uiSettings.isMapToolbarEnabled = false

            map.value!!.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        trip.mainDestination.latitude,
                        trip.mainDestination.longitude
                    ), 6f
                )
            )

            map.value!!.clear()
            val pattern: List<PatternItem> =
                arrayListOf(Dot(), Gap(15f))
            val polyline = PolylineOptions()
                .color(primaryColor.toArgb())
                .width(8f)
                .pattern(pattern)

            for ((index, step) in trip.destinationsPerDay[selectedDay].withIndex()) {
                val point = LatLng(
                    step.latitude,
                    step.longitude
                )
                polyline.add(point)
                val markerOptions = MarkerOptions()
                    .position(point)
                    .icon(numberedMarker(index + 1))
                    .title(step.name)
                    .zIndex(5f)


                val marker = map.value!!.addMarker(markerOptions)

                markerPopUp(marker)
            }
            map.value!!.addPolyline(polyline)
        }

        var mapView: MapView? = null
        if (loadingScreen.value > 5)
            mapView = rememberMapViewWithLifecycle()

        BoxWithConstraints {
            Box(modifier = Modifier.fillMaxSize().background(colors.background)) {

                if (mapView != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.55f)
                            .background(colors.background)
                            .wrapContentSize(Alignment.Center)
                    ) {
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

                    }
                }
                if (!mapLoaded.value) {
                    Text(
                        fontSize = textNormal,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.5f)
                            .padding(50.dp),
                        textAlign = TextAlign.Center,
                        color = White,
                        text = stringResource(R.string.loading)
                    )
                    loadingScreen.value++
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .height(200.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    colors.background,
                                    Color.Transparent
                                )
                            )
                        )
                ) {
                    Text(
                        text = "\uD83C\uDF0D ${trip.name}",
                        color = White,
                        fontWeight = Bold,
                        textAlign = TextAlign.Center,
                        fontSize = textHeading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(cardPadding)
                    )
                }
                FullHeightBottomSheet {

                    val maxHeight: Float by animateFloatAsState(
                        if (open.value) 250f else 100f, animationSpec = tween(
                            durationMillis = 500
                        )
                    )

                    LazyColumn(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .heightIn(0.dp, 1000.dp)
                            .fillMaxWidth()
                    ) {
                        item {
                            Column {
                                Button(
                                    onClick = {}, background = danger, modifier = Modifier
                                        .align(CenterHorizontally)
                                        .padding(5.dp)
                                ) {
                                    Row {
                                        FaIcon(FaIcons.Stop, tint = White, size = 18.dp)
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text("Quit the game", color = White)

                                    }
                                }
                                LazyRow(
                                    modifier = Modifier.align(CenterHorizontally),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                ) {
                                    items(trip.destinationsPerDay.size) { i ->
                                        val background =
                                            if (i == selectedDay) primaryColor else colors.onBackground
                                        val foreground =
                                            if (i == selectedDay) White else colors.surface
                                        Button(
                                            onClick = {
                                                selectedDay = i
                                                if (selectedDay < trip.destinationsPerDay.size)
                                                    steps = trip.destinationsPerDay[selectedDay]
                                            },
                                            modifier = Modifier.padding(5.dp),
                                            background = background
                                        ) {
                                            Column(horizontalAlignment = CenterHorizontally) {
                                                Text(
                                                    (i + 1).toString(),
                                                    color = foreground,
                                                    fontSize = textHeading
                                                )
                                                Text(
                                                    "day",
                                                    color = foreground,
                                                    fontSize = textSmall
                                                )
                                            }
                                        }
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .heightIn(0.dp, maxHeight.dp)
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onTap = { open.value = !open.value }
                                            )
                                        }
                                ) {
                                    TripCard(
                                        trip = trip,
                                        rating = 3.5f,
                                        padding = cardPadding,
                                        radius = cardRadius,
                                        icon = icon
                                    )
                                }

                                Row(
                                    horizontalArrangement = SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = cardPadding, end = cardPadding)
                                ) {
                                    Text(
                                        "Created by ${trip.creator} on ${trip.creationDate}",
                                        color = colors.surface,
                                        fontSize = textExtraSmall
                                    )
                                    Text(
                                        "Perfect during ${trip.season}",
                                        color = colors.surface,
                                        fontSize = textExtraSmall
                                    )
                                }

                                Text(
                                    trip.description,
                                    color = colors.surface,
                                    fontSize = textSmall,
                                    fontWeight = Bold,
                                    modifier = Modifier.padding(cardPadding)
                                )

                                FlexibleRow(
                                    alignment = CenterHorizontally,
                                    modifier = Modifier
                                        .scale(0.8f)
                                        .weight(1f)
                                        .padding(cardPadding / 3)
                                ) {
                                    Button(onClick = {}, modifier = Modifier.padding(5.dp)) {
                                        Text("${trip.destinationsPerDay.size} Day${if (trip.destinationsPerDay.size > 1) "s" else ""}")
                                    }
                                    trip.attributes.forEach { activity ->
                                        Button(
                                            onClick = {},
                                            modifier = Modifier.padding(5.dp)
                                        ) {
                                            Text(activity)
                                        }
                                    }

                                }


                                Box(
                                    modifier = Modifier
                                        .padding(cardPadding)
                                        .graphicsLayer {
                                            shape = RoundedCornerShape(cardRadius)
                                            clip = true
                                        }
                                        .background(colors.onBackground)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp)
                                    ) {
                                        Heading("Steps")
                                        steps.forEachIndexed { index, place ->
                                            if (index > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(start = 25.dp)
                                                        .width(2.dp)
                                                        .height(25.dp)
                                                        .background(
                                                            colors.surface
                                                        )
                                                )
                                            }
                                            TripStepCard(place, index, onComplete = {
                                                isCompleted = completed(steps)
                                            })
                                            if (index < steps.size - 1) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(start = 25.dp)
                                                        .width(2.dp)
                                                        .height(25.dp)
                                                        .background(
                                                            colors.surface
                                                        )
                                                )
                                                if (place.mediumToNextDestination != null) {
                                                    Row(
                                                        modifier = Modifier
                                                            .padding(
                                                                start = 20.dp,
                                                                end = 5.dp,
                                                                top = 5.dp,
                                                                bottom = 10.dp
                                                            )
                                                            .fillMaxWidth(),
                                                        horizontalArrangement = SpaceBetween
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.align(
                                                                CenterVertically
                                                            )
                                                        ) {
                                                            FaIcon(
                                                                MediumType.mediumTypeToIcon(place.mediumToNextDestination!!),
                                                                tint = colors.surface
                                                            )
                                                            Text(
                                                                "${place.minutesToNextDestination.toInt()} minutes (${place.kmToNextDestination} km)",
                                                                color = colors.surface,
                                                                fontSize = textSmall,
                                                                modifier = Modifier
                                                                    .padding(start = 20.dp)
                                                                    .align(CenterVertically)
                                                            )
                                                        }
                                                        if (!isCompleted) {
                                                            IconButton(
                                                                onClick = {
                                                                    val _steps =
                                                                        steps.clone() as ArrayList<TripDestination>
                                                                    _steps.add(
                                                                        index + 1,
                                                                        TripDestination()
                                                                    )
                                                                    steps = _steps
                                                                },
                                                                modifier = Modifier
                                                                    .size(22.dp, 22.dp)
                                                                    .align(CenterVertically)
                                                            ) {
                                                                FaIcon(
                                                                    FaIcons.Plus,
                                                                    size = 18.dp,
                                                                    tint = colors.surface,
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }


                                    }


                                }

                                if (isCompleted && !reviewed) {
                                    Heading("Aware other astronauts")
                                    RatingField {
                                        val rating = it
                                        rating.entityId = trip.id
                                        rating.username =
                                            Firebase.auth.currentUser?.displayName.toString()
                                        uploadRating(rating) { result ->
                                            if (result) {
                                                reviewed = true
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }

        }


    }


    private fun completed(destinations: List<TripDestination>): Boolean {
        for (destination in destinations) {
            if (!destination.visited) {
                return false
            }
        }
        return true
    }

    private fun uploadRating(rating: Rating, callback: (Boolean) -> Unit) {
        Thread {
            val gson = Gson()
            val request = gson.toJson(rating) // NON-NLS
            val tripText = sendPostRequest(request, action = "newRating") // NON-NLS
            if (!tripText.isNullOrEmpty())
                callback(true)
            else
                callback(false)
        }.start()
    }


}


