package com.apps.travel_app.ui.pages

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import com.apps.travel_app.R
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.ui.utils.rememberMapViewWithLifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.*
import com.google.maps.android.ktx.awaitMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt


@Composable
fun MapScreen(context: Context) {

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = textLightColor
    )

    val mapView = rememberMapViewWithLifecycle()
    val center = LatLng(44.0, 10.0)
    var polygonOpt = PolygonOptions()
    var map: GoogleMap? = null

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .wrapContentSize(Alignment.Center)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { position ->
                            if (map != null) {
                                map!!.clear()
                                polygonOpt = PolygonOptions()
                                polygonOpt.add(screenCoordinatesToLatLng(position, map))
                                polygonOpt.strokeColor(Color.BLUE)
                                map!!.addPolygon(polygonOpt)
                            }
                        },
                        onDrag = { event, _ -> mapDrag(map, event, polygonOpt) },
                        onDragEnd = {
                            if (map != null) {
                                val points = polygonOpt.points
                                var x = 0.0
                                var y = 0.0
                                val pointCount: Int = points.size
                                for (i in 0 until pointCount - 1) {
                                    val point = points[i]
                                    x += point.latitude
                                    y += point.longitude
                                }
                                x /= pointCount
                                y /= pointCount
                                val circle = CircleOptions()
                                circle.strokeColor(Color.BLUE)
                                circle.center(center)
                                circle.radius(
                                    sqrt(
                                        (points[0].latitude - x).pow(2.0) + (points[0].longitude - y).pow(
                                            2.0
                                        )
                                    )
                                )
                                map!!.clear()
                                map!!.addCircle(circle)
                            }
                        }
                    )
                },
        ) {
            AndroidView({ mapView }) { mapView ->
                CoroutineScope(Dispatchers.Main).launch {
                    map = mapView.awaitMap()

                    val completeMap = map!!
                    completeMap.uiSettings.isScrollGesturesEnabled = false
                    completeMap.uiSettings.isZoomControlsEnabled = false

                    completeMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                            context,
                            R.raw.mapstyle
                        )
                    )

                    completeMap.uiSettings.isMapToolbarEnabled = false;

                    completeMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 6f))

                    val markerOptionscenter = MarkerOptions()
                        .title("Restaurant Hubert")
                        .position(center)
                    completeMap.addMarker(markerOptionscenter)

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
    }
}

fun screenCoordinatesToLatLng(position: Offset, map: GoogleMap?): LatLng? {
    if (map == null)
        return null
    val x = position.x.roundToInt() // get screen x position or coordinate
    val y = position.y.roundToInt() // get screen y position or coordinate
    val point = Point(x, y) // accept int x,y value
    return map.projection.fromScreenLocation(point) // convert x,y to LatLng
}

fun mapDrag(map: GoogleMap?, motionEvent: PointerInputChange, polygonOpt: PolygonOptions) {
    if (map == null)
        return

    val latLng = screenCoordinatesToLatLng(motionEvent.position, map) ?: return

    if (!polygonOpt.points.none { point -> point.equals(latLng) })
        return

    map.clear()
    polygonOpt.add(latLng)

    map.addPolygon(polygonOpt)
}
