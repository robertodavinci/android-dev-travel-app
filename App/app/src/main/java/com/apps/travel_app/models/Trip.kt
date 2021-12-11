package com.apps.travel_app.models

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.ui.graphics.ImageBitmap
import com.apps.travel_app.ui.components.login.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class Trip() {
    var id: Int = 0
    var startingPoint: Destination = Destination()
    var days: Int = 1
        set (value) {
            field = if (value <= 0) 1 else value
        }
    var attributes: List<String> = arrayListOf()
    var creator: String = ""
    var thumbnail: ImageBitmap? = null
    var name: String = ""
    var thumbnailUrl: String = ""
    var rating: Float = 0f
    var destinationsPerDay: ArrayList<ArrayList<TripDestination>> = ArrayList()
    var description : String =""
    var season: String = ""
    var creationDate: String = ""
    var mine: Boolean = false
}