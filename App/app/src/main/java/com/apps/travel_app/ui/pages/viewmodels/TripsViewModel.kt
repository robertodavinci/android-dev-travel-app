package com.apps.travel_app.ui.pages.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.apps.travel_app.MainActivity
import com.apps.travel_app.data.room.db.AppDatabase
import com.apps.travel_app.data.room.entity.Location
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.Trip

class TripsViewModel(val db: AppDatabase, val onLoaded: (ArrayList<Destination>, ArrayList<Trip>) -> Unit): ViewModel() {
    var saved =
            ArrayList<Destination>()


    var savedTrips =
            ArrayList<Trip>()

    fun start() {
        Thread {
            val locations = db.locationDao().getAllSaved() as ArrayList<Location>
            val savedLocations = arrayListOf<Destination>()
            locations.forEach {
                val destination = Destination()
                destination.fromLocation(it)
                savedLocations.add(destination)
            }
            val trips =
                db.tripDao().getAll() as ArrayList<com.apps.travel_app.data.room.entity.Trip>
            val finalSavedTrips = arrayListOf<Trip>()
            trips.forEach {
                val trip = Trip()
                trip.fromTripDb(it)
                finalSavedTrips.add(trip)
            }
            saved = savedLocations
            savedTrips = finalSavedTrips
            onLoaded(saved,savedTrips)
        }.start()
    }


    init {
        start()
    }

}