package com.apps.travel_app.ui.pages.viewmodels
/**
 * A view model used for fetching and forwarding the data regarding Trips.
 * Used in combination with TripsScreen. Fetches locally saved trips and
 * forwards them to the function that then displays it.
 */
import android.app.Activity
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.room.Room
import com.apps.travel_app.R
import com.apps.travel_app.data.room.db.AppDatabase
import com.apps.travel_app.models.Rating
import com.apps.travel_app.models.Trip
import com.apps.travel_app.ui.theme.success
import com.apps.travel_app.ui.theme.yellow
import com.apps.travel_app.ui.utils.errorMessage
import com.apps.travel_app.ui.utils.isOnline
import com.apps.travel_app.ui.utils.sendPostRequest
import com.apps.travel_app.user
import com.google.android.libraries.maps.GoogleMap
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TripViewModel(val trip: Trip, val db: AppDatabase, val activity: Activity, val onRating: (List<Rating>) -> Unit) : ViewModel() {
    var open by mutableStateOf(false)
    var loaded by mutableStateOf(false)
    var ratings by mutableStateOf<ArrayList<Rating>?>(null)
    var loadingScreen by mutableStateOf(0)
    var map: GoogleMap? by mutableStateOf(null)
    var mapLoaded by mutableStateOf(false)
    var selectedDay by mutableStateOf(0)
    var isSaved by mutableStateOf(false)
    var isReviewed by mutableStateOf(false)
    var copied by mutableStateOf(false)


    var steps by mutableStateOf(if (selectedDay < trip.destinationsPerDay.size) trip.destinationsPerDay[selectedDay] else ArrayList())

    private fun getRatings(trip: Trip) {
        loaded = true

        if (ratings == null) {
            Thread {
                try {
                    val result = ArrayList<Rating>()

                    val request = "${trip.id}" // NON-NLS
                    val ratingsText = sendPostRequest(request, action = "ratings") // NON-NLS
                    val gson = Gson()
                    val itemType = object : TypeToken<List<Rating>>() {}.type
                    val _ratings: List<Rating> = gson.fromJson(ratingsText, itemType)
                    for (rating in _ratings) {
                        isReviewed = isReviewed || rating.userId == user.id
                        result.add(rating)
                    }
                    ratings = result
                    this.onRating(result)
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

    fun uploadRating(rating: Rating, callback: (Boolean) -> Unit) {
        Thread {
            val gson = Gson()
            val request = gson.toJson(rating) // NON-NLS
            val tripText = sendPostRequest(request, action = "newRating") // NON-NLS
            if (!tripText.isNullOrEmpty())
                callback(true)
            else
                callback(false)
        }.start()
    }

    fun save() {
        Thread {
            try {
                if (!isSaved) {
                    db.locationDao()
                        .insertAll(trip.mainDestination.toLocation())
                    db.tripDao()
                        .insertAll(trip.toTripDb(trip.mainDestination.id))[0]

                    val tripDao = db.tripStepDao()
                    trip.getTripStep(trip.id).forEach {
                        tripDao.insertAll(it)
                    }

                } else {
                    db.locationDao()
                        .delete(trip.mainDestination.toLocation())
                    trip.getTripStep(trip.id).forEach {
                        db.tripStepDao().delete(it)
                    }

                    db.tripDao().deleteById(trip.id)
                }
                isSaved = !isSaved
            } catch (e: Exception) {
                Log.e("ERROR", e.localizedMessage)
            }
        }.start()
    }

    fun copy() {
        try {
            val newTrip = trip.clone()
            newTrip.name += " copy"
            newTrip.creator = user.displayName
            newTrip.creatorId = user.id
            newTrip.id = "_" + System.currentTimeMillis()
            newTrip.incomplete = true
            Thread {

                db.locationDao()
                    .insertAll(newTrip.mainDestination.toLocation())
                db.tripDao()
                    .insertAll(newTrip.toTripDb(newTrip.mainDestination.id))[0]

                val tripDao = db.tripStepDao()
                trip.getTripStep(newTrip.id).forEach {
                    tripDao.insertAll(it)
                }
            }.start()

            val sb = Snackbar.make(
                activity.window.decorView.rootView, activity.resources.getString(R.string.saved),
                Snackbar.LENGTH_LONG
            )
            sb.view.setBackgroundColor(success.toArgb())
            sb.show()
            copied = true
        } catch (e: Exception) {
            errorMessage(activity.window.decorView.rootView)
        }

    }
}

class TripActivityViewModel(activity: Activity, tripId: String) : ViewModel() {
    var trip: Trip? by mutableStateOf(null)
    var phase by mutableStateOf(false)




    init {
        Thread {
            if (isOnline(activity)) {
                try {
                    val request = tripId.toString() // NON-NLS
                    val ratingsText = sendPostRequest(request, action = "trip") // NON-NLS
                    val gson = Gson()
                    val itemType = object : TypeToken<Trip>() {}.type
                    trip = gson.fromJson(ratingsText, itemType)

                } catch (e: Exception) {
                    errorMessage(activity.window.decorView.rootView).show()
                }
            } else {
                val db = Room.databaseBuilder(
                    activity,
                    AppDatabase::class.java, AppDatabase.NAME
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