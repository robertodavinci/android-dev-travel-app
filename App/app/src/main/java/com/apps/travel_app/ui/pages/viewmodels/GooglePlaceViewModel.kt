package com.apps.travel_app.ui.pages.viewmodels
/**
 * A view model used for fetching and forwarding the data regarding GooglePlace.
 * Used in combination with GooglePlaceScreen. Has functions of fetching the items.
 * todos, eat and stay places, that are directly connected to that Google Place.
 */
import android.app.Activity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.GooglePlace
import com.apps.travel_app.ui.utils.errorMessage
import com.apps.travel_app.ui.utils.sendPostRequest
import com.google.android.libraries.maps.GoogleMap
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GooglePlaceViewModel(destination: Destination, activity: Activity) : ViewModel() {

    var loaded by mutableStateOf(false)
    var googlePlace: GooglePlace? by mutableStateOf(null)
    var loadingScreen by mutableStateOf(0)
    var map: GoogleMap? by mutableStateOf(null)
    var mapLoaded by mutableStateOf(false)
    var openMap by mutableStateOf(false)
    var id by mutableStateOf("")
    var todo by mutableStateOf(arrayListOf<GooglePlace>())
    var eat by mutableStateOf(arrayListOf<GooglePlace>())
    var stay by mutableStateOf(arrayListOf<GooglePlace>())

    init {

        loaded = true

        Thread {
            val request = "{\"id\":\"${destination.id}\"}" // NON-NLS
            println(request)
            try {
                val text = sendPostRequest(request, action = "placeDetails") // NON-NLS
                val gson = Gson()
                val itemType = object : TypeToken<GooglePlaceResponse>() {}.type
                val response: GooglePlaceResponse = gson.fromJson(text, itemType)
                googlePlace = response.item
                todo = response.todo
                eat = response.eat
                stay = response.stay
            } catch (e: Exception) {
                errorMessage(activity.window.decorView.rootView).show()
            }
        }.start()
        if (destination.id != id) {
            id = destination.id
        }
    }

    private class GooglePlaceResponse {
        var item: GooglePlace = GooglePlace()
        var todo: ArrayList<GooglePlace> = arrayListOf()
        var eat: ArrayList<GooglePlace> = arrayListOf()
        var stay: ArrayList<GooglePlace> = arrayListOf()
    }

}