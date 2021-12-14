package com.apps.travel_app.ui.pages

import FaIcons
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.room.Room
import com.apps.travel_app.data.rooms.AppDatabase
import com.apps.travel_app.models.MediumType
import com.apps.travel_app.models.Rating
import com.apps.travel_app.models.Trip
import com.apps.travel_app.models.TripDestination
import com.apps.travel_app.ui.components.*
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.ui.utils.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.guru.fontawesomecomposelib.FaIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.Math.random

class TripActivity : ComponentActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val systemTheme = sharedPref.getBoolean("darkTheme", true)

        val tripId = intent.getIntExtra("tripId", 0)


        setContent {
            var trip: Trip? by remember { mutableStateOf(null) }
            Thread {
                if (isOnline(this)) {
                    val request = tripId.toString()
                    val ratingsText = sendPostRequest(request, action = "trip")
                    val gson = Gson()
                    val itemType = object : TypeToken<Trip>() {}.type
                    runOnUiThread {
                        trip = gson.fromJson(ratingsText, itemType)
                    }
                } else {
                    val db = Room.databaseBuilder(
                        this,
                        AppDatabase::class.java, "database-name"
                    ).build()
                    val tripDb = db.tripDao().getById(tripId)
                    val newTrip = Trip()
                    if (tripDb != null) {
                        newTrip.fromTripDb(tripDb)
                        runOnUiThread {
                            trip = newTrip
                        }
                    }
                }
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

        val db = Room.databaseBuilder(
            this,
            AppDatabase::class.java, "database-name"
        ).build()

        val systemUiController = rememberSystemUiController()
        systemUiController.setSystemBarsColor(
            color = textLightColor
        )

        val open: MutableState<Boolean> = remember { mutableStateOf(false) }
        val loaded: MutableState<Boolean> = remember { mutableStateOf(false) }
        val ratings: MutableState<ArrayList<Rating>> = remember { mutableStateOf(ArrayList()) }
        val loadingScreen = remember { mutableStateOf(0) }
        val map: MutableState<GoogleMap?> = remember { mutableStateOf(null) }
        val mapLoaded = remember { mutableStateOf(false) }
        var selectedDay by remember { mutableStateOf(0) }
        var isSaved by remember { mutableStateOf(false) }

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
                        trip.startingPoint.latitude,
                        trip.startingPoint.longitude
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

        fun getRatings(trip: Trip) {
            loaded.value = true

            if (ratings.value.size <= 0) {
                Thread {

                    val result = ArrayList<Rating>()

                    val request = "${trip.id}"
                    val ratingsText = sendPostRequest(request, action = "ratings")
                    val gson = Gson()
                    val itemType = object : TypeToken<List<Rating>>() {}.type
                    val _ratings: List<Rating> = gson.fromJson(ratingsText, itemType)
                    for (rating in _ratings) {
                        rating.rating = random().toFloat() * 5f
                        result.add(rating)
                    }
                    ratings.value = result
                }.start()
            }
        }

        if (!loaded.value) {
            if (isOnline(this)) {
                getRatings(trip)
            }
            Thread {
                try {
                    isSaved = db.tripDao().getById(trip.id) != null
                } catch (e: Exception) {

                }
            }.start()
        }


        var mapView: MapView? = null
        if (loadingScreen.value > 5)
            mapView = rememberMapViewWithLifecycle()

        BoxWithConstraints {
            Box(modifier = Modifier.fillMaxSize()) {

                if (mapView != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.55f)
                            .background(MaterialTheme.colors.background)
                            .wrapContentSize(Center)
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
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.5f)
                            .padding(50.dp),
                        textAlign = TextAlign.Center,
                        color = White,
                        text = "Loading..."
                    )
                    loadingScreen.value++
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(TopCenter)
                        .height(200.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    textLightColor,
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
                            .align(TopCenter)
                            .padding(cardPadding)
                    )
                }
                FullHeightBottomSheet(body = { _ ->

                    val maxHeight: Float by animateFloatAsState(
                        if (open.value) 250f else 100f, animationSpec = tween(
                            durationMillis = 500
                        )
                    )

                    LazyColumn(
                        modifier = Modifier
                            .align(TopStart)
                            .heightIn(0.dp, 1000.dp)
                            .fillMaxWidth()
                    ) {
                        item {
                            Column {
                                Button(
                                    onClick = {}, background = primaryColor, modifier = Modifier
                                        .align(
                                            CenterHorizontally
                                        )
                                        .padding(5.dp)
                                ) {
                                    Row {
                                        FaIcon(FaIcons.Play, tint = White, size = 18.dp)
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text("Start", color = White)

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
                                        radius = cardRadius
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
                                        color = MaterialTheme.colors.surface,
                                        fontSize = textExtraSmall
                                    )
                                    Text(
                                        "Perfect during ${trip.season}",
                                        color = MaterialTheme.colors.surface,
                                        fontSize = textExtraSmall
                                    )
                                }

                                Text(
                                    trip.description,
                                    color = MaterialTheme.colors.surface,
                                    fontSize = textSmall,
                                    fontWeight = Bold,
                                    modifier = Modifier.padding(cardPadding)
                                )

                                Row(
                                    modifier = Modifier.padding(cardPadding / 3)
                                ) {
                                    Button(
                                        onClick = {
                                            Thread {
                                                try {
                                                    if (!isSaved) {
                                                        val startingPointId = db.locationDao()
                                                            .insertAll(trip.startingPoint.toLocation())[0]
                                                        val tripId = db.tripDao()
                                                            .insertAll(trip.toTripDb(startingPointId.toInt()))[0]

                                                        val tripDao = db.tripStepDao()
                                                        trip.getTripStep(tripId.toInt()).forEach {
                                                            tripDao.insertAll(it)
                                                        }

                                                    } else {
                                                        db.locationDao()
                                                            .delete(trip.startingPoint.toLocation())
                                                        trip.getTripStep(trip.id).forEach {
                                                            db.tripStepDao().delete(it)
                                                        }

                                                        db.tripDao().deleteById(trip.id)
                                                    }
                                                    isSaved = !isSaved
                                                } catch (e: Exception) {
                                                    Log.e("ERROR", e.localizedMessage)
                                                }
                                            }.start()
                                        },
                                        modifier = Modifier
                                            .align(CenterVertically)
                                            .size(40.dp),
                                        contentPadding = PaddingValues(
                                            start = 2.dp,
                                            top = 2.dp,
                                            end = 2.dp,
                                            bottom = 2.dp
                                        )
                                    ) {
                                        FaIcon(
                                            if (isSaved) FaIcons.Heart else FaIcons.HeartRegular,
                                            tint = if (isSaved) danger else MaterialTheme.colors.surface
                                        )
                                    }
                                    FlexibleRow(
                                        alignment = CenterHorizontally,
                                        modifier = Modifier
                                            .align(CenterVertically)
                                            .scale(0.8f)
                                            .weight(1f)
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
                                    Spacer(
                                        modifier = Modifier.size(40.dp)
                                    )
                                }


                                LazyRow(
                                    modifier = Modifier.align(CenterHorizontally),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                ) {
                                    items(trip.destinationsPerDay.size) { i ->
                                        val background =
                                            if (i == selectedDay) primaryColor else MaterialTheme.colors.onBackground
                                        val foreground =
                                            if (i == selectedDay) White else MaterialTheme.colors.surface
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
                                        .padding(cardPadding)
                                        .graphicsLayer {
                                            shape = RoundedCornerShape(cardRadius)
                                            clip = true
                                        }
                                        .background(MaterialTheme.colors.onBackground)
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
                                                            MaterialTheme.colors.surface
                                                        )
                                                )
                                            }
                                            TripStepCard(place, index)
                                            if (index < steps.size - 1) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(start = 25.dp)
                                                        .width(2.dp)
                                                        .height(25.dp)
                                                        .background(
                                                            MaterialTheme.colors.surface
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
                                                                tint = MaterialTheme.colors.surface
                                                            )
                                                            Text(
                                                                "${place.minutesToNextDestination.toInt()} minutes (${place.kmToNextDestination} km)",
                                                                color = MaterialTheme.colors.surface,
                                                                fontSize = textSmall,
                                                                modifier = Modifier
                                                                    .padding(start = 20.dp)
                                                                    .align(CenterVertically)
                                                            )
                                                        }
                                                        if (trip.mine) {
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
                                                                    tint = MaterialTheme.colors.surface,
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                    }

                                }

                                Heading("Ratings")

                                Box(
                                    modifier = Modifier.padding(bottom = 60.dp)
                                ) {
                                    if (ratings.value.size <= 0) {
                                        Box(
                                            modifier = Modifier
                                                .align(Center)
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
                                }
                            }
                        }
                    }
                })

            }

        }


    }

}







