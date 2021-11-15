package com.apps.travel_app.ui.pages

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.apps.travel_app.models.Destination
import com.apps.travel_app.ui.components.DestinationCard
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.ui.utils.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.*
import com.google.maps.android.PolyUtil
import com.google.maps.android.ktx.awaitMap
import com.guru.fontawesomecomposelib.FaIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Math.random

val center = LatLng(44.0, 10.0)
var polygonOpt = PolygonOptions()
var customMarkerImage: Bitmap? = null
var baseImage: Bitmap? = null
var drawing = false
var map: GoogleMap? = null
var destinationSelected: MutableState<Boolean> = mutableStateOf(false)
var currentDestination: Destination = Destination()
var drawingEnabled: MutableState<Boolean> = mutableStateOf(false)

@Composable
fun MapScreen(context: Context) {
    destinationSelected = remember { destinationSelected }
    drawingEnabled = remember { drawingEnabled }

    Thread {

        baseImage =
            cropToSquare(getBitmapFromURL("https://www.veneto.info/wp-content/uploads/sites/114/verona.jpg")!!)
        customMarkerImage =
            getCroppedBitmap(baseImage!!, 100, 100, 5f)

    }.start()

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = textLightColor
    )

    val mapView = rememberMapViewWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .wrapContentSize(Alignment.Center)
        ) {
            AndroidView({ mapView }) { mapView ->
                CoroutineScope(Dispatchers.Main).launch {
                    map = mapView.awaitMap()
                    mapInit(map!!, context)
                }
            }

        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .height(200.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            textLightColor,
                            Transparent
                        )
                    )
                )
        )

        Text(
            text = "\uD83C\uDF0D Spin & pin",
            color = White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            fontSize = textHeading,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(cardPadding)
        )
        if (drawingEnabled.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopCenter)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { position ->
                                if (map != null)
                                    mapDrawingReset(map!!, position)
                            },
                            onDrag = { event, _ ->
                                mapDrawing(
                                    map,
                                    event,
                                    polygonOpt
                                )
                            },
                            onDragEnd = {
                                if (map != null) {
                                    populateMapDrawing(map!!)
                                    toggleDrawing()
                                }
                            }
                        )
                    }
            )
        }

        Column(
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
                        tint = if (drawingEnabled.value) textLightColor else iconLightColor
                    )
                }

                IconButton(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(cardPadding)
                        .width(30.dp)
                        .height(30.dp),
                    onClick = {

                    }) {
                    FaIcon(
                        faIcon = FaIcons.BuildingRegular,
                        tint = iconLightColor
                    )
                }
            }

            if (destinationSelected.value) {
                DestinationCard(
                    destination = currentDestination, modifier = Modifier
                        .fillMaxWidth()
                        .align(
                            Alignment.CenterHorizontally
                        ).heightIn(0.dp,80.dp), true
                )
            }
        }
    }
}

fun toggleDrawing() {
    if (map == null)
        return
    drawingEnabled.value = !drawingEnabled.value
    map!!.uiSettings.isScrollGesturesEnabled = !drawing
}

fun populateMapDrawing(map: GoogleMap) {
    val points = noiseReduction(polygonOpt.points,5)
    val polygonOpt2 = PolygonOptions()
        .strokeColor(Color.parseColor("#FF808ea7"))
        .fillColor(Color.parseColor("#88808ea7"))
        for (point in points) {
            map.clear()
            polygonOpt2.add(point)
            map.addPolygon(polygonOpt2)
        }


    val point = points[0]
    val mapPoints = points.map { m ->
        com.google.android.gms.maps.model.LatLng(
            m.latitude,
            m.longitude
        )
    }
    for (i in 1..5) {
        var location: com.google.android.gms.maps.model.LatLng
        do {
            Log.d("skip","skip")
            location = com.google.android.gms.maps.model.LatLng(
                point.latitude + random() * 5 - 2.5f,
                point.longitude + random() * 5 - 2.5f
            )
        } while (!PolyUtil.containsLocation(
                location,
                mapPoints,
                true))
        val marker = map.addMarker(
            MarkerOptions()
                .position(
                    LatLng(
                        location.latitude,
                        location.longitude
                    )
                )
                .icon(
                    BitmapDescriptorFactory.fromBitmap(
                        customMarkerImage
                    )
                )
                .title("Verona")
                .snippet("Figo!")
                .zIndex(5f)
        )
        markerPopUp(marker)
    }
}

fun mapDrawingReset(map: GoogleMap, position: Offset) {
    map.clear()
    polygonOpt = PolygonOptions()
    polygonOpt.add(screenCoordinatesToLatLng(position, map))
    polygonOpt
        .strokeColor(Color.parseColor("#FF808ea7"))
        .fillColor(Color.parseColor("#88808ea7"))
    map.addPolygon(polygonOpt)
}

fun mapInit(map: GoogleMap, context: Context) {
    map.uiSettings.isZoomControlsEnabled = false

    map.setMapStyle(
        MapStyleOptions.loadRawResourceStyle(
            context,
            com.apps.travel_app.R.raw.style
        )
    )

    map.uiSettings.isMapToolbarEnabled = false

    map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 6f))

    map.setOnMarkerClickListener { marker -> markerClick(marker) }
}

fun mapDrawing(map: GoogleMap?, motionEvent: PointerInputChange, polygonOpt: PolygonOptions) {
    if (map == null)
        return

    val latLng = screenCoordinatesToLatLng(motionEvent.position, map) ?: return

    if (!polygonOpt.points.none { point -> point.equals(latLng) })
        return

    map.clear()
    polygonOpt.add(latLng)

    map.addPolygon(polygonOpt)
}

fun markerClick(marker: Marker): Boolean {
    currentDestination = Destination()
    currentDestination.latitude = marker.position.latitude
    currentDestination.latitude = marker.position.longitude
    currentDestination.name = marker.title
    currentDestination.thumbnail = baseImage?.asImageBitmap()
    destinationSelected.value = true
    return true
}

fun noiseReduction(src: List<LatLng>, severity: Int = 1): List<LatLng>
{
    val newList = ArrayList<LatLng>()
    for (point in src) {
        newList.add(point)
    }
    for (i in src.indices)
    {
        val start = i - severity;
        val end = i + severity;

        var sumLat = 0.0;
        var sumLng = 0.0;

        for (j in start until end)
        {
            sumLat += newList[Math.floorMod(j,src.size)].latitude;
            sumLng += newList[Math.floorMod(j,src.size)].longitude;
        }

        val avgLat = sumLat / (end - start);
        val avgLng = sumLng / (end - start);

        newList[i] = LatLng(avgLat,avgLng)
    }
    return newList
}
