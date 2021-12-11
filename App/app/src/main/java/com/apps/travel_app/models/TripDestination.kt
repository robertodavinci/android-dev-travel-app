package com.apps.travel_app.models

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.ui.graphics.ImageBitmap
import com.guru.fontawesomecomposelib.FaIconType

class TripDestination : Destination() {
    var kmToNextDestination: Float = 0f
    var minutesToNextDestination: Float = 0f
    var mediumToNextDestination: MediumType? = MediumType.Foot
    var hour: String = ""
    var minutes: Int = 0
    var visited: Boolean = false
    var notes: ArrayList<String> = arrayListOf()
    var images: ArrayList<String> = arrayListOf()



    companion object {

        operator fun invoke(it: Destination): TripDestination {
            val trip = TripDestination()
            trip.id = it.id
            trip.latitude = it.latitude
            trip.longitude = it.longitude
            trip.type = it.type
            trip.thumbnail = it.thumbnail
            trip.name = it.name
            trip.thumbnailUrl = it.thumbnailUrl
            trip.rating = it.rating
            trip.description = it.description
            trip.priceLevel = it.priceLevel
            trip.isOpen = it.isOpen
            trip.address = it.address
            return trip
        }
    }
}

enum class MediumType {
    Car,
    Foot,
    Bike,
    Plane,
    Ferry,
    Bus,
    Tram,
    Train,
    Chairlift,
    Ski;

    companion object {

        fun mediumTypeToIcon(type: MediumType): FaIconType {

            return when (type) {
                Car -> FaIcons.Car
                Foot -> FaIcons.Walking
                Bike -> FaIcons.Biking
                Ferry -> FaIcons.Ship
                Plane -> FaIcons.Plane
                Bus -> FaIcons.Bus
                Tram -> FaIcons.Tram
                Train -> FaIcons.Train
                Chairlift -> FaIcons.Chair
                Ski -> FaIcons.Skiing
            }
        }
    }
}