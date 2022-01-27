package com.apps.travel_app.ui.pages.viewmodels

import android.app.Activity
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.apps.travel_app.models.Destination
import com.apps.travel_app.ui.utils.Response
import com.apps.travel_app.ui.utils.markerPopUp
import com.apps.travel_app.ui.utils.numberedMarker
import com.apps.travel_app.ui.utils.sendPostRequest
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.Marker
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocationSelectionViewModel(activity: Activity): ViewModel() {
    private val destinations = HashMap<Int, Destination>()

    var center = LatLng(44.0, 10.0)
    var currentDestination: Destination? by  mutableStateOf(null)
    var map: GoogleMap? by  mutableStateOf(null)
    var mapView: MapView? by  mutableStateOf(null)
    var mapLoaded by  mutableStateOf(false)
    var destinationSelected by  mutableStateOf(false)
    var startingPointSelected by  mutableStateOf(false)
    var stepAdded by mutableStateOf(false)
    val mainActivity = activity

    var searchTerm by  mutableStateOf("") 

    private var cities by
        mutableStateOf(ArrayList<Destination>())
    private var places by
        mutableStateOf(ArrayList<Destination>())


    fun addMarker(position: LatLng, index: Int, name: String, destination: Destination) {
        if (map == null)
            return
        val markerOptions = MarkerOptions()
            .position(
                position
            )
            .icon(numberedMarker(index + 1))
            .title(name)
            .zIndex(5f)
        val marker = map!!.addMarker(markerOptions)
        destinations[marker.hashCode()] = destination

        markerPopUp(marker)
    }

    fun search(text: String) {
        val region = map?.projection?.visibleRegion ?: return

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
                try {
                val gson = Gson()
                val itemType = object : TypeToken<Response>() {}.type
                val response: Response = gson.fromJson(resultText, itemType)
                cities = response.cities
                places = response.places

                var index = 0

                mainActivity.runOnUiThread {
                    if (map != null) {
                        map!!.clear()
                        cities.forEach {
                            addMarker(
                                LatLng(it.latitude, it.longitude),
                                index++,
                                it.name,
                                it
                            )
                        }
                        places.forEach {
                            addMarker(
                                LatLng(it.latitude, it.longitude),
                                index++,
                                it.name,
                                it
                            )
                        }
                    }
                }
                } catch (e: Exception) {
                    mainActivity.currentFocus?.let {
                        Snackbar.make(
                            it, "Ops, there is a connectivity problem",
                            Snackbar.LENGTH_LONG).show()
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
            currentDestination = destination
            destinationSelected = true
            return true
        }
        destinationSelected = false

        return false
    }

}