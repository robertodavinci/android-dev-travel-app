package com.apps.travel_app.ui.utils

import android.graphics.Interpolator
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.view.animation.BounceInterpolator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.Marker
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
            marker.setAnchor(0.5f, 3 * t)
            if (t > 0.0) {
                handler.postDelayed(this, 16)
            }
        }
    })

    return false
}