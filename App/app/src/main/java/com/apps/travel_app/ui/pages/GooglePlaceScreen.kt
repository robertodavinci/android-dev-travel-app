package com.apps.travel_app.ui.pages

import FaIcons
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.apps.travel_app.MainActivity
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.GooglePlace
import com.apps.travel_app.ui.components.*
import com.apps.travel_app.ui.theme.*
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


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GooglePlaceScreen(
    navController: NavController,
    destination: Destination,
    mainActivity: MainActivity
) {

    val loaded: MutableState<Boolean> = remember { mutableStateOf(false) }
    val googlePlace: MutableState<GooglePlace?> = remember { mutableStateOf(null) }
    val loadingScreen = remember { mutableStateOf(0) }
    val map: MutableState<GoogleMap?> = remember { mutableStateOf(null) }
    val mapLoaded = remember { mutableStateOf(false) }
    val openMap = remember { mutableStateOf(false) }
    val id = remember { mutableStateOf("") }

    fun dayOfWeek(i: Int): String {
        return when (i) {
            0 -> "Mon"
            1 -> "Tue"
            2 -> "Wed"
            3 -> "Thu"
            4 -> "Fri"
            5 -> "Sat"
            6 -> "Sun"
            else -> ""
        }
    }

    fun getMoreInfo() {
        loaded.value = true

        //if (googlePlace.value == null) {
            Thread {
                val request = "{\"id\":\"${destination.id}\"}"
                val text = sendPostRequest(request, action = "placeDetails")
                val gson = Gson()
                val itemType = object : TypeToken<GooglePlace>() {}.type
                val _googlePlace: GooglePlace = gson.fromJson(text, itemType)
                googlePlace.value = _googlePlace
            }.start()
        //}
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
                    destination.latitude,
                    destination.longitude
                ), 12f
            )
        )

        //map.value?.setOnMarkerClickListener { marker -> markerClick(marker) }
    }


    if (!loaded.value || destination.id != id.value) {
        id.value = destination.id
        getMoreInfo()

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
                .background(colors.background)
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
                        views = 206,
                        mainActivity = mainActivity,
                        padding = (cardPadding * (1 - percentage)),
                        radius = (cardRadius * (1 - percentage)),
                        icon = FaIcons.Google,
                        clickable = false
                    )
                }

                FlexibleRow(
                    alignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(cardPadding / 2)
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = { }, modifier = Modifier
                            .padding(5.dp)
                    ) {
                        Row {
                            FaIcon(
                                FaIcons.AddressCardRegular,
                                tint = colors.surface,
                                modifier = Modifier.align(CenterVertically)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                googlePlace.value?.address ?: "",
                                color = colors.surface,
                                modifier = Modifier.align(CenterVertically)
                            )
                        }
                    }
                    Button(
                        onClick = { }, modifier = Modifier
                            .padding(5.dp)
                    ) {
                        Row {
                            FaIcon(
                                FaIcons.PhoneAlt,
                                tint = colors.surface
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(googlePlace.value?.phoneNumber ?: "", color = colors.surface)
                        }
                    }
                    Button(onClick = { openMap.value = true }, modifier = Modifier.padding(5.dp)) {
                        FaIcon(
                            FaIcons.LocationArrow,
                            tint = colors.surface
                        )
                    }
                    Button(onClick = {  }, modifier = Modifier.padding(5.dp)) {
                        Row {
                            for (i in 1..(googlePlace.value?.priceLevel?.toInt() ?: 1)) {
                                FaIcon(
                                    FaIcons.EuroSign,
                                    tint = colors.surface
                                )
                            }
                        }
                    }
                }

                FlexibleRow(
                    alignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(cardPadding / 2)
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = { }, modifier = Modifier
                            .padding(5.dp),

                        background = if (googlePlace.value?.isOpen == true) success else danger
                    ) {
                        Text(
                            if (googlePlace.value?.isOpen == true) "Open" else "Close",
                            color = Color.White
                        )
                    }
                    googlePlace.value?.openingHours?.forEach {
                        val day = it.open.dayOfWeek
                        Button(onClick = { }, modifier = Modifier.padding(5.dp)) {
                            Row {
                                Text(
                                    dayOfWeek(day) + " ",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    it.open.hour + " - "
                                )
                                if (day != it.close.dayOfWeek) {
                                    Text(
                                        dayOfWeek(it.close.dayOfWeek),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    it.close.hour
                                )
                            }
                        }
                    }
                }

                Heading(
                    "Top ratings"
                )

                Box(
                    modifier = Modifier.padding(bottom = 60.dp)
                ) {
                    if (googlePlace.value?.reviews?.isEmpty() == true) {
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

                            googlePlace.value?.reviews?.forEach { rating ->
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
                                        colors.background,
                                        Color.Transparent
                                    )
                                )
                            )
                    )
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
                            color = colors.surface,
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







