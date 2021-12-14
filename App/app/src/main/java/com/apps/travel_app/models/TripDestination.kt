package com.apps.travel_app.models


import com.apps.travel_app.data.rooms.TripStep
import com.guru.fontawesomecomposelib.FaIconType
import java.lang.Exception

class TripDestination : Destination() {
    var kmToNextDestination: Float = 0f
    var minutesToNextDestination: Float = 0f
    var mediumToNextDestination: MediumType? = MediumType.Foot
    var hour: String = ""
    var minutes: Int = 0
    var visited: Boolean = false
    var notes: List<String> = arrayListOf()
    var images: List<String> = arrayListOf()

    fun fromLocation(tripStep: TripStep) {
        id = tripStep.tsid.toString()
        latitude = tripStep.latitude
        longitude = tripStep.longitude
        type = tripStep.type
        thumbnailUrl = tripStep.thumbnailUrl
        rating = tripStep.rating
        description = tripStep.description
        address = tripStep.address ?: ""
        name = tripStep.name
        kmToNextDestination = tripStep.kmToNextDestination
        minutesToNextDestination = tripStep.minutesToNextDestination
        mediumToNextDestination = tripStep.mediumToNextDestination
        hour = tripStep.hour
        minutes = tripStep.minutes
        visited = tripStep.visited
        notes = tripStep.notes.split("|")
        images = tripStep.images.split("|")
    }


    fun toTripStep(tripId: Int, day: Int): TripStep {
        var lid = 0
        try {
            lid = id.toInt()
        } catch (e: Exception) {

        }
        return TripStep(
            tsid = lid,
            latitude = latitude,
            longitude = longitude,
            type = type,
            thumbnailUrl = thumbnailUrl,
            rating = rating,
            description = description,
            address = address,
            name = name,
            phone_number = null,
            kmToNextDestination = kmToNextDestination,
            minutesToNextDestination = minutesToNextDestination,
            mediumToNextDestination = mediumToNextDestination,
            hour = hour,
            minutes = minutes,
            visited = visited,
            notes = notes.joinToString { "|" },
            images = images.joinToString { "|" },
            trip = tripId,
            day = day
        )
    }

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