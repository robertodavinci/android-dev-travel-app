package com.apps.travel_app.ui.pages

import FaIcons
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.ExtraBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.apps.travel_app.R
import com.apps.travel_app.data.room.AppDatabase
import com.apps.travel_app.models.*
import com.apps.travel_app.ui.components.*
import com.apps.travel_app.ui.pages.viewmodels.TripActivityViewModel
import com.apps.travel_app.ui.pages.viewmodels.TripViewModel
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.ui.utils.markerPopUp
import com.apps.travel_app.ui.utils.numberedMarker
import com.apps.travel_app.ui.utils.rememberMapViewWithLifecycle
import com.apps.travel_app.ui.utils.sendPostRequest
import com.apps.travel_app.user
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.*
import com.google.gson.Gson
import com.guru.fontawesomecomposelib.FaIcon
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TripActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireFullscreenMode(window, this)

        val intent = intent

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val systemTheme = sharedPref.getBoolean("darkTheme", true)

        val tripId = intent.getStringExtra("tripId")

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContent {

            val viewModel = remember { TripActivityViewModel(this, tripId ?: "0") }
            Travel_AppTheme(systemTheme = systemTheme) {

                if (viewModel.trip != null) {

                    if (!viewModel.phase) {
                        TripWallpaper(viewModel.trip!!, next = {
                            viewModel.phase = true
                        })
                    } else {
                        TripScreen(viewModel.trip!!, this)
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
                            stringResource(R.string.more),
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
        trip: Trip,
        context: Context
    ) {

        val ratings = remember {
            mutableStateListOf<Rating>()
        }

        val db = Room.databaseBuilder(
            this,
            AppDatabase::class.java, "database-name"
        ).build()

        val viewModel = remember {
            TripViewModel(
                trip, db, this
            ) {
                ratings.addAll(it)
            }
        }

        val discussion = remember { mutableStateListOf(*trip.discussion.toTypedArray()) }

        var writeRating by remember { mutableStateOf(false) }
        var writeMessage by remember { mutableStateOf(false) }

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
                        text = stringResource(R.string.loading)
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
                            viewModel.save()
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
                                            Text(
                                                stringResource(R.string.duplicate),
                                                fontSize = textNormal,
                                                color = White
                                            )
                                        }
                                    }
                                    Text(
                                        stringResource(R.string.if_customize_copy),
                                        fontSize = textExtraSmall,
                                        textAlign = TextAlign.Center,
                                        color = colors.surface,
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
                                            .align(CenterHorizontally)
                                            .padding(5.dp)
                                    ) {
                                        Row {
                                            FaIcon(FaIcons.Pen, tint = White, size = 18.dp)
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                stringResource(R.string.edit),
                                                color = White,
                                                fontSize = textNormal
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = cardPadding, end = cardPadding),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    (1..3).forEach {
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

                                }

                                Row(
                                    horizontalArrangement = SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = cardPadding, end = cardPadding)
                                ) {
                                    Text(
                                        stringResource(R.string.created_by) + " " + trip.creator + " - " + trip.creationDate,
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
                                                    viewModel.steps =
                                                        trip.destinationsPerDay[viewModel.selectedDay]
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
                                                    stringResource(R.string.day),
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
                                        Heading(stringResource(R.string.steps))
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
                                            TripStepCard(place, index, tripId = trip.id, context = context)
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
                                                                "${place.minutesToNextDestination.toInt()} ${
                                                                    stringResource(
                                                                        R.string.minutes
                                                                    )
                                                                } (${place.kmToNextDestination} km)",
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

                                Heading(stringResource(R.string.ratings))


                            }
                        }
                        items(ratings) {
                            Box(
                                modifier = Modifier.padding(cardPadding / 2)
                            ) {
                                RatingCard(
                                    it
                                )
                            }
                        }
                        item {
                            if (!viewModel.isReviewed) {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Center) {
                                    Button(
                                        onClick = { writeRating = true },
                                        background = primaryColor
                                    ) {
                                        Text(
                                            stringResource(R.string.share_what_you_think),
                                            color = White
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            Heading(stringResource(R.string.discussion))
                        }

                        items(
                            discussion
                        ) { message ->
                            Box(Modifier.padding(cardPadding)) {
                                MessageCard(message, tripId = trip.id)
                            }
                        }
                        item {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Center) {
                                Button(
                                    onClick = { writeMessage = true },
                                    background = primaryColor
                                ) {
                                    Text(stringResource(R.string.new_message), color = White)
                                }
                            }
                            Spacer(Modifier.height(60.dp))
                        }
                    }
                }
            }

        }
        if (writeRating || writeMessage) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = {
                    writeRating = false
                    writeMessage = false
                },
            ) {
                Column(Modifier.padding(cardPadding)) {
                    if (writeMessage) {
                        MessageField(tripId = trip.id) {
                            discussion.add(0, it)
                            writeRating = false
                            writeMessage = false
                        }
                    }
                    if (writeRating) {
                        RatingField {
                            val rating = it
                            rating.entityId = trip.id
                            rating.userId = user.id
                            rating.username = user.displayName ?: ""
                            viewModel.uploadRating(rating) { result ->
                                if (result) {
                                    ratings.add(rating)
                                    writeRating = false
                                    viewModel.isReviewed = true
                                }
                            }
                        }
                    }
                }

            }
        }

    }


}









