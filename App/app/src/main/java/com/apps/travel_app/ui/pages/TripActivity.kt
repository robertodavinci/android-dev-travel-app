package com.apps.travel_app.ui.pages

import FaIcons
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.ExtraBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.apps.travel_app.data.room.AppDatabase
import com.apps.travel_app.models.MediumType
import com.apps.travel_app.models.Trip
import com.apps.travel_app.models.TripDestination
import com.apps.travel_app.ui.components.*
import com.apps.travel_app.ui.pages.viewmodels.TripActivityViewModel
import com.apps.travel_app.ui.pages.viewmodels.TripViewModel
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.ui.utils.markerPopUp
import com.apps.travel_app.ui.utils.numberedMarker
import com.apps.travel_app.ui.utils.rememberMapViewWithLifecycle
import com.apps.travel_app.user
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.*
import com.guru.fontawesomecomposelib.FaIcon
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TripActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireFullscreenMode(window, this)

        val intent = intent

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val systemTheme = sharedPref.getBoolean("darkTheme", true)

        val tripId = intent.getIntExtra("tripId", 0)


        setContent {

            val viewModel = remember { TripActivityViewModel(this, tripId) }
            Travel_AppTheme(systemTheme = systemTheme) {

                if (viewModel.trip != null) {
                    if (!viewModel.phase) {
                        TripWallpaper(viewModel.trip!!, next = {
                            viewModel.phase = true
                        })
                    } else {
                        TripScreen(viewModel.trip!!)
                    }
                }
            }
        }
    }

    @OptIn(
        ExperimentalFoundationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class
    )
    @Composable
    fun TripWallpaper(
        trip: Trip,
        next: () -> Unit
    ) {
        Box(Modifier.fillMaxSize()) {
            GlideImage(imageModel = trip.thumbnailUrl)
            Button(
                onClick = {
                    finish()
                },
                background = Transparent,
                modifier = Modifier.padding(cardPadding * 2)
            ) {
                FaIcon(
                    FaIcons.ArrowLeft,
                    tint = White
                )
            }
            Column(
                Modifier
                    .align(BottomStart)
                    .padding(cardPadding * 2)
            ) {
                Text(
                    trip.name,
                    color = White,
                    fontSize = textNormal,
                    fontWeight = ExtraBold
                )
                Text(
                    trip.description,
                    color = White,
                    fontSize = textNormal
                )
                Button(
                    onClick = next
                ) {
                    Row {
                        Text(
                            "More",
                            fontSize = textNormal
                        )
                        Spacer(Modifier.size(10.dp))
                        FaIcon(
                            FaIcons.ArrowRight,
                            tint = textLightColor
                        )
                    }

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

        val viewModel = remember { TripViewModel(
             trip,  db, this
        ) }

        fun mapInit() {
            viewModel.map!!.uiSettings.isZoomControlsEnabled = false

            viewModel.map?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    mapStyle
                )
            )

            viewModel.map!!.uiSettings.isMapToolbarEnabled = false

            viewModel.map!!.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        trip.mainDestination.latitude,
                        trip.mainDestination.longitude
                    ), 6f
                )
            )

            viewModel.map!!.clear()
            val pattern: List<PatternItem> =
                arrayListOf(Dot(), Gap(15f))
            val polyline = PolylineOptions()
                .color(primaryColor.toArgb())
                .width(8f)
                .pattern(pattern)

            for ((index, step) in trip.destinationsPerDay[viewModel.selectedDay].withIndex()) {
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


                val marker = viewModel.map!!.addMarker(markerOptions)

                markerPopUp(marker)
            }
            viewModel.map!!.addPolyline(polyline)
        }


        var mapView: MapView? = null
        if (viewModel.loadingScreen > 5)
            mapView = rememberMapViewWithLifecycle()

        BoxWithConstraints {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
            ) {

                if (mapView != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.55f)
                            .background(colors.background)
                            .wrapContentSize(Center)
                    ) {
                        AndroidView({ mapView }) { mapView ->
                            CoroutineScope(Dispatchers.Main).launch {
                                if (!viewModel.mapLoaded) {
                                    mapView.getMapAsync { mMap ->
                                        if (!viewModel.mapLoaded) {
                                            viewModel.map = mMap
                                            mapInit()
                                            viewModel.mapLoaded = true
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
                if (!viewModel.mapLoaded) {
                    Text(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.5f)
                            .padding(50.dp),
                        textAlign = TextAlign.Center,
                        color = White,
                        fontSize = textNormal,
                        text = "Loading..."
                    )
                    viewModel.loadingScreen++
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(TopCenter)
                        .height(200.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    colors.background,
                                    Transparent
                                )
                            )
                        )
                ) {
                    Text(
                        text = "\uD83C\uDF0D ${trip.name}",
                        color = colors.surface,
                        fontWeight = Bold,
                        textAlign = TextAlign.Center,
                        fontSize = textHeading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(TopCenter)
                            .padding(vertical = cardPadding * 2, horizontal = cardPadding)
                    )
                }
                FullHeightBottomSheet(button = {
                    Button(
                        onClick = {
                            Thread {
                                try {
                                    if (!viewModel.isSaved) {
                                        db.locationDao()
                                            .insertAll(trip.mainDestination.toLocation())
                                        val tripId = db.tripDao()
                                            .insertAll(trip.toTripDb(trip.mainDestination.id))[0]

                                        val tripDao = db.tripStepDao()
                                        trip.getTripStep(tripId.toInt()).forEach {
                                            tripDao.insertAll(it)
                                        }

                                    } else {
                                        db.locationDao()
                                            .delete(trip.mainDestination.toLocation())
                                        trip.getTripStep(trip.id).forEach {
                                            db.tripStepDao().delete(it)
                                        }

                                        db.tripDao().deleteById(trip.id)
                                    }
                                    viewModel.isSaved = !viewModel.isSaved
                                } catch (e: Exception) {
                                    Log.e("ERROR", e.localizedMessage)
                                }
                            }.start()
                        },
                        modifier = Modifier
                            .size(40.dp),
                        contentPadding = PaddingValues(
                            start = 2.dp,
                            top = 2.dp,
                            end = 2.dp,
                            bottom = 2.dp
                        )
                    ) {
                        FaIcon(
                            if (viewModel.isSaved) FaIcons.Heart else FaIcons.HeartRegular,
                            tint = if (viewModel.isSaved) danger else colors.surface
                        )
                    }
                }) {

                    LazyColumn(
                        modifier = Modifier
                            .align(TopStart)
                            .heightIn(0.dp, 1000.dp)
                            .fillMaxWidth()
                    ) {
                        item {
                            Column {
                                if (trip.creatorId != user.id) {
                                    Button(
                                        onClick = {}, background = primaryColor, modifier = Modifier
                                            .align(
                                                CenterHorizontally
                                            )
                                            .padding(5.dp)
                                    ) {
                                        Row {
                                            FaIcon(FaIcons.CopyRegular, tint = White, size = 18.dp)
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text("Duplicate", fontSize = textNormal, color = White)
                                        }
                                    }
                                    Text(
                                        "If you want to customize this trip, copy it",
                                        fontSize = textExtraSmall,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    Button(
                                        onClick = {
                                            val intent = Intent(
                                                baseContext,
                                                TripCreationActivity::class.java
                                            )
                                            intent.putExtra("tripId", trip.id)
                                            finish()
                                            startActivity(intent)
                                        }, background = primaryColor, modifier = Modifier
                                            .align(
                                                CenterHorizontally
                                            )
                                            .padding(5.dp)
                                    ) {
                                        Row {
                                            FaIcon(FaIcons.Pen, tint = White, size = 18.dp)
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text("Edit", color = White, fontSize = textNormal)
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = cardPadding, end = cardPadding),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    GlideImage(
                                        imageModel = trip.thumbnailUrl,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .padding(10.dp)
                                            .graphicsLayer {
                                                shape = RoundedCornerShape(20)
                                                clip = true
                                            }
                                    )
                                    GlideImage(
                                        imageModel = trip.thumbnailUrl,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .padding(10.dp)
                                            .graphicsLayer {
                                                shape = RoundedCornerShape(20)
                                                clip = true
                                            }
                                    )
                                    GlideImage(
                                        imageModel = trip.thumbnailUrl,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .padding(10.dp)
                                            .graphicsLayer {
                                                shape = RoundedCornerShape(20)
                                                clip = true
                                            }
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

                                Row(
                                    modifier = Modifier.padding(cardPadding / 3)
                                ) {

                                    FlexibleRow(
                                        alignment = CenterHorizontally,
                                        modifier = Modifier
                                            .align(CenterVertically)
                                            .scale(0.8f)
                                            .weight(1f)
                                    ) {
                                        Button(
                                            onClick = {},
                                            modifier = Modifier.padding(5.dp)
                                        ) {
                                            Text(
                                                "${trip.destinationsPerDay.size} Day${if (trip.destinationsPerDay.size > 1) "s" else ""}",
                                                fontSize = textNormal
                                            )
                                        }
                                        trip.attributes.forEach { activity ->
                                            Button(
                                                onClick = {},
                                                modifier = Modifier.padding(5.dp)
                                            ) {
                                                Text(activity, fontSize = textNormal)
                                            }
                                        }
                                    }
                                }


                                LazyRow(
                                    modifier = Modifier.align(CenterHorizontally),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                ) {
                                    items(trip.destinationsPerDay.size) { i ->
                                        val background =
                                            if (i == viewModel.selectedDay) primaryColor else colors.onBackground
                                        val foreground =
                                            if (i == viewModel.selectedDay) White else colors.surface
                                        Button(
                                            onClick = {
                                                viewModel.selectedDay = i
                                                if (viewModel.selectedDay < trip.destinationsPerDay.size)
                                                    viewModel.steps = trip.destinationsPerDay[viewModel.selectedDay]
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
                                        .background(colors.onBackground)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp)
                                    ) {
                                        Heading("Steps")
                                        viewModel.steps.forEachIndexed { index, place ->
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
                                            TripStepCard(place, index, tripId = trip.id)
                                            if (index < viewModel.steps.size - 1) {
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
                                                                MediumType.mediumTypeToIcon(
                                                                    place.mediumToNextDestination!!
                                                                ),
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
                                                        if (trip.sharedWith.contains(user.email)) {
                                                            IconButton(
                                                                onClick = {
                                                                    val _steps =
                                                                        viewModel.steps.clone() as ArrayList<TripDestination>
                                                                    _steps.add(
                                                                        index + 1,
                                                                        TripDestination()
                                                                    )
                                                                    viewModel.steps = _steps
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

                                Heading("Ratings")

                                Box(
                                    modifier = Modifier.padding(bottom = 60.dp)
                                ) {
                                    if (viewModel.ratings.size <= 0) {
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

                                            viewModel.ratings.forEach { rating ->
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
                }
            }

        }

    }


}









