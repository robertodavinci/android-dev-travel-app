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
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.ui.utils.getBitmapFromURL
import com.apps.travel_app.ui.utils.rememberMapViewWithLifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.*
//import com.google.maps.android.PolyUtil
import com.google.maps.android.ktx.awaitMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.*


@Composable
fun MapScreen(context: Context) {

    /*val customMarkerImage =
        getBitmapFromURL("https://www.veneto.info/wp-content/uploads/sites/114/verona.jpg")*/

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
                                polygonOpt
                                    .strokeColor(Color.parseColor("#FF808ea7"))
                                    .fillColor(Color.parseColor("#88808ea7"))
                                map!!.addPolygon(polygonOpt)
                            }
                        },
                        onDrag = { event, _ -> mapDrag(map, event, polygonOpt) },
                        onDragEnd = {
                            /*if (map != null) {
                                for (i in 1..5) {
                                    var location: com.google.android.gms.maps.model.LatLng
                                    do {
                                        location = com.google.android.gms.maps.model.LatLng(
                                            -37.813,
                                            144.962
                                        )
                                        map!!.addMarker(
                                            MarkerOptions()
                                                .position(
                                                    LatLng(
                                                        location.latitude,
                                                        location.longitude
                                                    )
                                                )
                                                .title("Melbourne")
                                                .snippet("Population: 4,137,400")
                                                .icon(
                                                    BitmapDescriptorFactory.fromBitmap(
                                                        customMarkerImage
                                                    )
                                                )
                                        )
                                    } while (PolyUtil.containsLocation(
                                            location,
                                            polygonOpt.points.map { marker ->
                                                com.google.android.gms.maps.model.LatLng(
                                                    marker.latitude,
                                                    marker.longitude
                                                )
                                            },
                                            true
                                        )
                                    )
                                }
                            }
                            if (map == null) {
                                val points = polygonOpt.points
                                var x = 0.0
                                var y = 0.0
                                for (point in points) {
                                    x += point.latitude
                                    y += point.longitude
                                }
                                x /= points.size
                                y /= points.size
                                val circleCenter = LatLng(x, y)
                                val circle = CircleOptions()
                                    .strokeColor(Color.parseColor("#FF808ea7"))
                                    .fillColor(Color.parseColor("#88808ea7"))
                                circle.center(circleCenter)
                                circle.radius(
                                    getDistanceFromLatLonInKm(circleCenter, points[0])
                                )
                                map!!.clear()
                                map!!.addCircle(circle)
                            }*/
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
                            com.apps.travel_app.R.raw.style
                        )
                    )

                    completeMap.uiSettings.isMapToolbarEnabled = false

                    completeMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 6f))

                    completeMap.addMarker(
                        MarkerOptions()
                            .position(
                                LatLng(
                                    45.0,
                                    10.0
                                )
                            )
                            .title("Verona")
                            .snippet("Qualcosa")
                            /*.icon(
                                BitmapDescriptorFactory.fromBitmap(
                                    customMarkerImage
                                )
                            )*/
                    )

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

fun getDistanceFromLatLonInKm(point1: LatLng, point2: LatLng): Double {
    val R = 6371 // Radius of the earth in km
    val dLat = deg2rad(point2.latitude - point1.latitude)  // deg2rad below
    val dLon = deg2rad(point2.longitude - point1.longitude)
    val a =
        sin(dLat / 2) * sin(dLat / 2) +
                cos(deg2rad(point1.latitude)) * cos(deg2rad(point2.latitude)) *
                sin(dLon / 2) * sin(dLon / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    val d = R * c // Distance in km
    return d
}

fun deg2rad(deg: Double): Double {
    return deg * (Math.PI / 180)
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
