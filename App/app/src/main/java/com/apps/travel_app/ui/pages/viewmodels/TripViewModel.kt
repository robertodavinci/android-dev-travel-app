package com.apps.travel_app.ui.pages.viewmodels

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.room.Room
import com.apps.travel_app.data.room.AppDatabase
import com.apps.travel_app.models.Rating
import com.apps.travel_app.models.Trip
import com.apps.travel_app.ui.utils.errorMessage
import com.apps.travel_app.ui.utils.isOnline
import com.apps.travel_app.ui.utils.sendPostRequest
import com.google.android.libraries.maps.GoogleMap
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TripViewModel(trip: Trip, db: AppDatabase, val activity: Activity) : ViewModel() {
    var open by mutableStateOf(false)
    var loaded by mutableStateOf(false)
    var ratings by mutableStateOf(ArrayList<Rating>())
    var loadingScreen by mutableStateOf(0)
    var map: GoogleMap? by mutableStateOf(null)
    var mapLoaded by mutableStateOf(false)
    var selectedDay by mutableStateOf(0)
    var isSaved by mutableStateOf(false)

    var steps by mutableStateOf(if (selectedDay < trip.destinationsPerDay.size) trip.destinationsPerDay[selectedDay] else ArrayList())

    private fun getRatings(trip: Trip) {
        loaded = true

        if (ratings.size <= 0) {
            Thread {
                try {
                    val result = ArrayList<Rating>()

                    val request = "${trip.id}"
                    val ratingsText = sendPostRequest(request, action = "ratings")
                    val gson = Gson()
                    val itemType = object : TypeToken<List<Rating>>() {}.type
                    val _ratings: List<Rating> = gson.fromJson(ratingsText, itemType)
                    for (rating in _ratings) {
                        rating.rating = Math.random().toFloat() * 5f
                        result.add(rating)
                    }
                    ratings = result
                } catch (e: Exception) {
                    errorMessage(activity.window.decorView.rootView).show()
                }
            }.start()
        }
    }

    init {
        if (isOnline(activity)) {
            getRatings(trip)
        }
        Thread {
            try {
                isSaved = db.tripDao().getById(trip.id) != null
            } catch (e: Exception) {

            }
        }.start()
    }
}

class TripActivityViewModel(activity: Activity, tripId: Int) : ViewModel() {
    var trip: Trip? by mutableStateOf(null)
    var phase by mutableStateOf(false)

    init {
        Thread {
            if (isOnline(activity)) {
                try {
                    val request = tripId.toString()
                    val ratingsText = sendPostRequest(request, action = "trip")
                    val gson = Gson()
                    val itemType = object : TypeToken<Trip>() {}.type
                    activity.runOnUiThread {
                        trip = gson.fromJson(ratingsText, itemType)
                    }
                } catch (e: Exception) {
                    errorMessage(activity.window.decorView.rootView).show()
                }
            } else {
                val db = Room.databaseBuilder(
                    activity,
                    AppDatabase::class.java, "database-name"
                ).build()
                val tripDb = db.tripDao().getById(tripId)
                val newTrip = Trip()
                if (tripDb != null) {
                    newTrip.fromTripDb(tripDb)
                    activity.runOnUiThread {
                        trip = newTrip
                    }
                }
            }
        }.start()
    }
}