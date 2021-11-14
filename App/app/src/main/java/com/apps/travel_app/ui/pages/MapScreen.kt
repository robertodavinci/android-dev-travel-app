package com.apps.travel_app.ui.pages

import android.content.Context
import android.graphics.Bitmap
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
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.ui.utils.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.*
import com.google.maps.android.PolyUtil
//import com.google.maps.android.PolyUtil
import com.google.maps.android.ktx.awaitMap
import com.guru.fontawesomecomposelib.FaIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Math.random
import kotlin.math.*

val center = LatLng(44.0, 10.0)
var polygonOpt = PolygonOptions()
var customMarkerImage: Bitmap? = null
var drawing = false
var map: GoogleMap? = null

@Composable
fun MapScreen(context: Context) {

    val drawingObserver = remember { mutableStateOf(false) }

    Thread {
        customMarkerImage =
            getCroppedBitmap(
                getBitmapFromURL("https://www.veneto.info/wp-content/uploads/sites/114/verona.jpg")!!,
                100,
                100,
                5f
            )

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
            text = "Spin & pin",
            color = White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            fontSize = textHeading,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(cardPadding)
        )
        if (drawingObserver.value) {
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
                                    drawingObserver.value = drawing
                                }
                            }
                        )
                    }
            )
        }

        IconButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(vertical = 70.dp, horizontal = cardPadding)
                .width(30.dp)
                .height(30.dp),
            onClick = {
                toggleDrawing()
                drawingObserver.value = drawing
            }) {
            FaIcon(
                faIcon = FaIcons.HandPointUpRegular,
                tint = if (drawingObserver.value) textLightColor else iconLightColor
            )
        }

        IconButton(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(vertical = 70.dp, horizontal = cardPadding)
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
}

fun toggleDrawing() {
    if (map == null)
        return
    drawing = !drawing
    map!!.uiSettings.isScrollGesturesEnabled = !drawing
}

fun populateMapDrawing(map: GoogleMap) {
    val point = polygonOpt.points[0]
    for (i in 1..5) {
        var location: com.google.android.gms.maps.model.LatLng
        do {
            location = com.google.android.gms.maps.model.LatLng(
                point.latitude + random() * 5 - 2.5f,
                point.longitude + random() * 5 - 2.5f
            )
        } while (!PolyUtil.containsLocation(
                location,
                polygonOpt.points.map { m ->
                    com.google.android.gms.maps.model.LatLng(
                        m.latitude,
                        m.longitude
                    )
                },
                false
            )
        )
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
    Log.d("c", "prova")
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
