package com.apps.travel_app.ui.pages

import FaIcons
import android.app.Activity
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.apps.travel_app.models.Destination
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.ui.utils.markerPopUp
import com.apps.travel_app.ui.utils.numberedMarker
import com.apps.travel_app.ui.utils.rememberMapViewWithLifecycle
import com.apps.travel_app.ui.utils.sendPostRequest
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MapStyleOptions
import com.google.android.libraries.maps.model.Marker
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.guru.fontawesomecomposelib.FaIcon
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val destinations = HashMap<Int, Destination>()

@OptIn(ExperimentalMaterialApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.ui.ExperimentalComposeUiApi::class
)
@Composable
fun LocationSelection(
    context: Context,
    activity: Activity,
    onBack: () -> Unit,
    onAddStep: (Destination?) -> Unit,
    onStartingPointSelected: (Destination?) -> Unit
) {
    val center = LatLng(44.0, 10.0)
    val currentDestination: MutableState<Destination?> = remember { mutableStateOf(null) }
    val map: MutableState<GoogleMap?> = remember { mutableStateOf(null) }
    val mapView: MutableState<MapView?> = remember { mutableStateOf(null) }
    val mapLoaded = remember { mutableStateOf(false) }
    val destinationSelected = remember { mutableStateOf(false) }
    var startingPointSelected by remember { mutableStateOf(false) }
    var stepAdded by remember { mutableStateOf(false) }

    var searchTerm by remember { mutableStateOf("") }

    val cities = remember {
        mutableStateOf(ArrayList<Destination>())
    }
    val places = remember {
        mutableStateOf(ArrayList<Destination>())
    }

    fun addMarker(position: LatLng, index: Int, name: String, destination: Destination) {
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
        destinations[marker.hashCode()] = destination

        markerPopUp(marker)
    }

    fun Search(text: String) {
        val region = map.value?.projection?.visibleRegion ?: return

        Thread {
            val points = arrayListOf(
                region.nearLeft,
                region.farLeft,
                region.farRight,
                region.nearRight,
                region.nearLeft
            )
            val request = "{\"area\":" + points.joinToString(",", "[", "]") { e ->
                "[${e.latitude},${e.longitude}]"
            } + ", \"text\": \"$text\"}"
            println(request)
            val resultText = sendPostRequest(request, action = "search")
            if (!resultText.isNullOrEmpty()) {
                val gson = Gson()
                val itemType = object : TypeToken<Response>() {}.type
                val response: Response = gson.fromJson(resultText, itemType)
                cities.value = response.cities
                places.value = response.places

                var index = 0

                activity.runOnUiThread {
                    if (map.value != null) {
                        map.value!!.clear()
                        cities.value.forEach {
                            addMarker(
                                LatLng(it.latitude, it.longitude),
                                index++,
                                it.name,
                                it
                            )
                        }
                        places.value.forEach {
                            addMarker(
                                LatLng(it.latitude, it.longitude),
                                index++,
                                it.name,
                                it
                            )
                        }
                    }
                }
            }
        }.start()
    }


    fun markerClick(marker: Marker): Boolean {
        startingPointSelected = false
        stepAdded = false
        val destination = destinations[marker.hashCode()]
        if (destination != null) {
            currentDestination.value = destination
            destinationSelected.value = true
            return true
        }
        destinationSelected.value = false

        return false
    }


    fun mapInit(context: Context) {
        map.value!!.uiSettings.isZoomControlsEnabled = false

        map.value?.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                context,
                mapStyle
            )
        )

        map.value!!.uiSettings.isMapToolbarEnabled = false

        map.value?.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 6f))

        map.value?.setOnMarkerClickListener { marker -> markerClick(marker) }
    }

/*
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = colors.background
    )*/


    mapView.value = rememberMapViewWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {

        //if (mapView.value != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .wrapContentSize(Alignment.Center)
        ) {
            AndroidView({ mapView.value!! }) { mapView ->
                CoroutineScope(Dispatchers.Main).launch {
                    if (!mapLoaded.value) {
                        mapView.getMapAsync { mMap ->
                            if (!mapLoaded.value) {
                                map.value = mMap
                                mapInit(context)
                                mapLoaded.value = true
                            }
                        }
                    }
                }
            }

        }
        //}

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .height(200.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colors.background,
                            Transparent
                        )
                    )
                )
        )
        val keyboardController = LocalSoftwareKeyboardController.current
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(vertical = cardPadding * 2, horizontal = cardPadding)
        ) {
            Row {
                IconButton(onClick = { onBack() }) {
                    FaIcon(FaIcons.ArrowLeft, tint = colors.surface)
                }
                TextField(
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onDone = {Search(searchTerm);keyboardController?.hide()}),
                    value = searchTerm, onValueChange = { searchTerm = it },
                    shape = RoundedCornerShape(cardRadius),
                    modifier = Modifier
                        .weight(1f),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Transparent,
                        disabledIndicatorColor = Transparent,
                        unfocusedIndicatorColor = Transparent,
                        backgroundColor = colors.onBackground,
                    ),
                    placeholder = {
                        Text(
                            "Search",
                            color = colors.surface,
                            modifier = Modifier.alpha(0.5f)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            Search(
                                searchTerm
                            )
                            keyboardController?.hide()
                        }) {
                            FaIcon(FaIcons.Search, tint = colors.surface)
                        }
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = colors.surface,
                        fontWeight = FontWeight.Bold
                    ),
                )
            }
            Row {

            }
        }

        /*Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(vertical = 70.dp)
                .fillMaxWidth()
        ) {

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {

                IconButton(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(cardPadding)
                        .width(30.dp)
                        .height(30.dp),
                    onClick = {
                        toggleDrawing()
                    }) {
                    FaIcon(
                        faIcon = FaIcons.HandPointUpRegular,
                        tint = if (drawingEnabled.value) MaterialTheme.colors.surface else iconLightColor
                    )
                }

                IconButton(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(cardPadding)
                        .width(30.dp)
                        .height(30.dp),
                    onClick = {
                        switchTo3D()
                    }) {
                    FaIcon(
                        faIcon = FaIcons.BuildingRegular,
                        tint = MaterialTheme.colors.surface
                    )
                }
            }*/

        if (currentDestination.value != null) {
            Column(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Card(
                    elevation = cardElevation,
                    backgroundColor = colors.onBackground,
                    shape = RoundedCornerShape(cardRadius),
                    modifier = Modifier
                        .heightIn(0.dp, 100.dp)
                        .wrapContentSize()
                        .padding(cardPadding),
                    onClick = {

                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        GlideImage(
                            imageModel = currentDestination.value?.thumbnailUrl,
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxHeight()
                                .widthIn(0.dp, 100.dp)
                                .align(Alignment.CenterVertically)
                        )

                        Column(
                            modifier = Modifier.padding(cardPadding)
                        ) {
                            Text(
                                text = currentDestination.value?.name ?: "",
                                color = colors.surface,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Start,
                                fontSize = textNormal,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Start)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(cardPadding),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    com.apps.travel_app.ui.components.Button(onClick = {
                        onStartingPointSelected(currentDestination.value)
                        startingPointSelected = true
                    },background = if (startingPointSelected) success else primaryColor) {
                        Text("Set as starting point", color = White)
                    }
                    Spacer(modifier = Modifier.padding(5.dp))
                    com.apps.travel_app.ui.components.Button(onClick = {
                        onAddStep(currentDestination.value)
                        stepAdded = true
                    },background = if (stepAdded) success else primaryColor) {
                        Text("Add as step", color = White)
                    }
                }
            }
        }

        //}

    }

}



