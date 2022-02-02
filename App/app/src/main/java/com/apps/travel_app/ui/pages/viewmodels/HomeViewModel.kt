package com.apps.travel_app.ui.pages.viewmodels

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.Trip
import com.apps.travel_app.ui.theme.danger
import com.apps.travel_app.ui.utils.errorMessage
import com.apps.travel_app.ui.utils.sendPostRequest
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HomeViewModel(activity: Activity): ViewModel() {

    var trips by mutableStateOf(ArrayList<Trip>())
    var adventures by  mutableStateOf(ArrayList<Trip>())
    var cities by mutableStateOf(ArrayList<Destination>())

    init {

        if (trips.size <= 0 && adventures.size <= 0 && cities.size <= 0) {
            Thread {

                val citiesText = sendPostRequest("", action = "home") // NON-NLS
                if (!citiesText.isNullOrEmpty()) {
                    try {
                        val gson = Gson()
                        val itemType = object : TypeToken<HomeResponse>() {}.type
                        val output: HomeResponse = gson.fromJson(citiesText, itemType)

                        activity.runOnUiThread {
                            trips = output.trips
                            cities = output.cities
                            adventures = output.adventures
                        }
                    } catch (e: Exception) {

                        errorMessage(activity.window.decorView.rootView).show()

                    }
                }
            }.start()
        }
    }

    class HomeResponse {
        var adventures: ArrayList<Trip> = arrayListOf()
        var cities: ArrayList<Destination> = arrayListOf()
        var trips: ArrayList<Trip> = arrayListOf()
    }

}