package com.apps.travel_app.ui.pages.viewmodels
/**
 * A view model used for fetching and forwarding the data regarding Trips.
 * Used in combination with TripsScreen. Fetches locally saved trips and
 * forwards them to the function that then displays it.
 */
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.apps.travel_app.data.room.db.AppDatabase
import com.apps.travel_app.data.room.entity.Location
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.Trip

class TripsViewModel(db: AppDatabase, onLoaded: (ArrayList<Destination>, ArrayList<Trip>) -> Unit): ViewModel() {
    var saved =
            ArrayList<Destination>()


    var savedTrips =
            ArrayList<Trip>()


    init {
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

}