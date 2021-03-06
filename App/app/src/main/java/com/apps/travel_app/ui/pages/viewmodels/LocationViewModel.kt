package com.apps.travel_app.ui.pages.viewmodels
/**
 * A view model used for fetching and forwarding the data regarding Locations.
 * Used in combination with LocationScreen. Has functions of fetching everything
 * regarding specific location, including Trips that feature that location,
 * facilities that are located there via Google Places, and finally, ratings
 * that are concerned to this location.
 */
import android.app.Activity
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.apps.travel_app.data.room.db.AppDatabase
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.GooglePlace
import com.apps.travel_app.models.Rating
import com.apps.travel_app.models.Trip
import com.apps.travel_app.ui.utils.errorMessage
import com.apps.travel_app.ui.utils.sendPostRequest
import com.google.android.libraries.maps.GoogleMap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocationViewModel(destination: Destination, db: AppDatabase, val activity: Activity) :
    ViewModel() {

    private val loaded: MutableState<Boolean> = mutableStateOf(false)
    val ratings: MutableState<ArrayList<Rating>> = mutableStateOf(ArrayList())
    val facilities: MutableState<ArrayList<Destination>> = mutableStateOf(ArrayList())
    val trips: MutableState<ArrayList<Trip>> = mutableStateOf(ArrayList())
    val loadingScreen = mutableStateOf(0)
    val map: MutableState<GoogleMap?> = mutableStateOf(null)
    val mapLoaded = mutableStateOf(false)
    val openMap = mutableStateOf(false)
    val isSaved = mutableStateOf(false)
    var todo by mutableStateOf(arrayListOf<GooglePlace>())
    var eat by mutableStateOf(arrayListOf<GooglePlace>())
    var stay by mutableStateOf(arrayListOf<GooglePlace>())


    init {
        getRatings(destination)
        getFacilities(destination)
        getTrips(destination)
        Thread {
            try {
                isSaved.value = db.locationDao().getSavedById(destination.id) != null
            } catch (e: Exception) {
            }
        }.start()
    }

    private fun getRatings(destination: Destination) {
        loaded.value = true

        if (ratings.value.size <= 0) {
            Thread {
                try {
                    val result = ArrayList<Rating>()

                    val request = "${destination.latitude},${destination.longitude}" // NON-NLS
                    val ratingsText = sendPostRequest(request, action = "ratings") // NON-NLS
                    val gson = Gson()
                    val itemType = object : TypeToken<List<Rating>>() {}.type
                    val _ratings: List<Rating> = gson.fromJson(ratingsText, itemType)
                    for (rating in _ratings) {
                        rating.rating = Math.random().toFloat() * 5f
                        result.add(rating)
                    }
                    ratings.value = result
                } catch (e: Exception) {
                    errorMessage(activity.window.decorView.rootView).show()
                }
            }.start()
        }
    }

    private fun getFacilities(destination: Destination) {
        loaded.value = true

        Thread {

            try {
                val request =
                    "{\"lat\":${destination.latitude},\"lng\":${destination.longitude}}" // NON-NLS
                val results = sendPostRequest(request, action = "nearby") // NON-NLS
                val gson = Gson()
                val itemType = object : TypeToken<LocationResponse>() {}.type
                val result:  LocationResponse = gson.fromJson(results, itemType)
                todo = result.todo
                eat = result.eat
                stay = result.stay
            } catch (e: Exception) {

                errorMessage(activity.window.decorView.rootView).show()

            }
        }.start()

    }

    private fun getTrips(destination: Destination) {
        loaded.value = true

        if (trips.value.size <= 0) {
            Thread {
                try {
                    val request =
                        "[[${destination.latitude - 1},${destination.longitude - 1}],[${destination.latitude - 1},${destination.longitude + 1}],[${destination.latitude + 1},${destination.longitude + 1}],[${destination.latitude + 1},${destination.longitude - 1}]]"
                    val results = sendPostRequest(request, action = "polygonTrips") // NON-NLS
                    val gson = Gson()
                    val itemType = object : TypeToken<List<Trip>>() {}.type
                    val result: ArrayList<Trip> = gson.fromJson(results, itemType)
                    trips.value = result
                } catch (e: Exception) {
                    errorMessage(activity.window.decorView.rootView).show()
                }
            }.start()
        }
    }

    private class LocationResponse {
        var todo: ArrayList<GooglePlace> = arrayListOf()
        var eat: ArrayList<GooglePlace> = arrayListOf()
        var stay: ArrayList<GooglePlace> = arrayListOf()
    }
}