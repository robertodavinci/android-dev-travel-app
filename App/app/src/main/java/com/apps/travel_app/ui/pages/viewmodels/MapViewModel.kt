package com.apps.travel_app.ui.pages.viewmodels

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.lifecycle.ViewModel
import com.apps.travel_app.MainActivity
import com.apps.travel_app.models.Destination
import com.apps.travel_app.ui.utils.*
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MapViewModel : ViewModel() {

    val destinations = HashMap<Int, Destination>()
    var center = LatLng(44.0, 10.0)
    var polygonOpt = PolygonOptions()
    var currentDestination: Destination? by mutableStateOf(null)
    var map: GoogleMap? by mutableStateOf(null)
    var mapLoaded by mutableStateOf(false)
    var destinationSelected by mutableStateOf(false)
    var drawingEnabled by mutableStateOf(false)
    var loadingScreen by mutableStateOf(0)


    fun switchTo3D() {
        if (map != null) {
            val cameraPosition: CameraPosition = CameraPosition.Builder()
                .target(map!!.cameraPosition.target)
                .tilt(if (map!!.cameraPosition.tilt > 0f) 30f else 0f)
                .build()
            map?.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    cameraPosition
                )
            )
        }
    }

    fun toggleDrawing() {
        destinationSelected = false
        drawingEnabled = !drawingEnabled
        map?.uiSettings?.isScrollGesturesEnabled = !drawingEnabled
    }

    fun populateMapDrawing(activity: MainActivity) {
        if (polygonOpt.points.size <= 0)
            return


        val points = line(polygonOpt.points)
        val polygonOpt2 = PolygonOptions()
            .strokeColor(Color.parseColor("#FF0083FF"))
            .fillColor(Color.parseColor("#550083FF"))
        for (point in points) {
            map?.clear()
            polygonOpt2.add(point)
            map?.addPolygon(polygonOpt2)
        }

        points.add(points[0])
        val request = points.joinToString(",", "[", "]") { e ->
            "[${e.latitude},${e.longitude}]"
        }

        Thread {
            try {
                val citiesText = sendPostRequest(request, action = "polygonCities")
                val gson = Gson()
                val itemType = object : TypeToken<List<Destination>>() {}.type
                val cities: List<Destination> = gson.fromJson(citiesText, itemType)
                for (city in cities) {

                    val downloadedImage = getBitmapFromURL(city.thumbnailUrl)
                    var thumbnail: Bitmap? = null
                    if (downloadedImage != null) {
                        val baseImage =
                            cropToSquare(downloadedImage)
                        thumbnail =
                            getCroppedBitmap(baseImage, 100, 100, 5f)

                        city.thumbnail = baseImage.asImageBitmap()
                    }
                    val markerOptions = MarkerOptions()
                        .position(
                            LatLng(
                                city.latitude,
                                city.longitude
                            )
                        )
                        .title(city.name)
                        .zIndex(5f)
                    if (thumbnail != null)
                        markerOptions.icon(
                            BitmapDescriptorFactory.fromBitmap(
                                thumbnail
                            )
                        )
                    activity.runOnUiThread {
                        val marker = map!!.addMarker(markerOptions)
                        destinations[marker.hashCode()] = city
                        markerPopUp(marker)
                    }
                }
            } catch (e: Exception) {
                errorMessage(activity.window.decorView.rootView).show()
            }
        }.start()

    }

    fun mapDrawingReset(position: Offset) {
        map?.clear()
        destinations.clear()
        polygonOpt = PolygonOptions()
        polygonOpt.add(screenCoordinatesToLatLng(position, map))
        polygonOpt
            .strokeColor(Color.parseColor("#FF0083FF"))
            .fillColor(Color.parseColor("#550083FF"))
        map?.addPolygon(polygonOpt)
    }

    fun mapDrawing(motionEvent: PointerInputChange, polygonOpt: PolygonOptions) {

        val latLng = screenCoordinatesToLatLng(motionEvent.position, map) ?: return

        if (!polygonOpt.points.none { point -> point.equals(latLng) })
            return

        map?.clear()
        polygonOpt.add(latLng)

        map?.addPolygon(polygonOpt)
    }

    fun markerClick(marker: Marker): Boolean {
        val destination = destinations[marker.hashCode()]
        if (destination != null) {
            currentDestination = destination
            destinationSelected = true
            return true
        }
        destinationSelected = false
        return false
    }
}