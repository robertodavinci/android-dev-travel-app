package com.apps.travel_app.ui.utils

/**
 * Map view element used for searching places, destinations, and trips.
 * User in several different screens - when searching for places and when
 * adding destinations to the trip. Also features additional details like
 * markers and lining on the map.
 */

import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.animation.BounceInterpolator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.apps.travel_app.ui.theme.primaryColor
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.*
import kotlin.math.*


@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context)
    }

    val lifecycleObserver = rememberMapLifecycleObserver(mapView)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

@Composable
fun rememberMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    remember(mapView) {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> throw IllegalStateException()
            }
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

fun markerPopUp(marker: Marker): Boolean {
    val handler = Handler()
    val start = SystemClock.uptimeMillis()
    val duration: Long = 1000
    val interpolator = BounceInterpolator()
    handler.post(object : Runnable {
        override fun run() {
            val elapsed = SystemClock.uptimeMillis() - start
            val t = max(
                1 - interpolator.getInterpolation(
                    elapsed.toFloat()
                            / duration
                ), 0f
            )

            marker.alpha = 1 - t
            marker.setAnchor(0.5f,1 + 3 * t)
            if (t > 0.0) {
                handler.postDelayed(this, 16)
            }
        }
    })

    return false
}


fun noiseReduction(src: List<LatLng>, severity: Int = 1): ArrayList<LatLng>
{
    val newList = ArrayList<LatLng>()
    for (point in src) {
        newList.add(point)
    }
    for (i in src.indices)
    {
        val start = i - severity
        val end = i + severity

        var sumLat = 0.0
        var sumLng = 0.0

        for (j in start until end)
        {
            sumLat += newList[Math.floorMod(j,src.size)].latitude
            sumLng += newList[Math.floorMod(j,src.size)].longitude
        }

        val avgLat = sumLat / (end - start)
        val avgLng = sumLng / (end - start)

        newList[i] = LatLng(avgLat,avgLng)
    }
    return newList
}

fun line(points: List<LatLng>): ArrayList<LatLng> {
    val smoothedLine = ArrayList<LatLng>()
    smoothedLine.add(points[0])
    var newPoint = points[1]
    for (i in 2 until points.size - 2) {
        newPoint = smoothPoint(points.subList(i - 2, i + 3))
        smoothedLine.add(LatLng(newPoint.latitude,newPoint.longitude))
    }
    return smoothedLine
}

fun smoothPoint(points: List<LatLng>): LatLng {
    var avgX = 0.0
    var avgY = 0.0
    for (point in points) {
        avgX += point.latitude
        avgY += point.longitude
    }
    avgX /= points.size
    avgY /= points.size
    val newPoint = LatLng(avgX, avgY)
    val oldPoint = points[points.size / 2]
    val newX = (5 * newPoint.latitude + oldPoint.latitude) / 6
    val newY = (5 * newPoint.longitude + oldPoint.longitude) / 6
    return LatLng(newX, newY)
}

fun numberedMarker(number: Int): BitmapDescriptor {
    val conf = Bitmap.Config.ARGB_8888
    val bmp = Bitmap.createBitmap(80, 80, conf)
    val canvas = Canvas(bmp)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = Color.WHITE
    paint.textAlign = Paint.Align.CENTER
    paint.textSize = 35f

    val xPos = canvas.width / 2
    val yPos = (canvas.height / 2 - (paint.descent() + paint.ascent()) / 2)
    val paintCircle = Paint(Paint.ANTI_ALIAS_FLAG)
    paintCircle.color = primaryColor.toArgb()

    canvas.drawCircle(40f, 40f, 40f, paintCircle)

    val paintStroke = Paint(Paint.ANTI_ALIAS_FLAG)
    paintStroke.style = Paint.Style.STROKE
    paintStroke.strokeWidth = 5f
    paintStroke.color = Color.WHITE

    canvas.drawCircle(40f, 40f, 38f, paintStroke)
    canvas.drawText(number.toString(), xPos.toFloat(), yPos, paint)

    return BitmapDescriptorFactory.fromBitmap(bmp)
}