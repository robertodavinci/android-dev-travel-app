package com.apps.travel_app.models

import androidx.compose.ui.graphics.ImageBitmap
import com.apps.travel_app.data.room.entity.Trip
import com.apps.travel_app.data.room.entity.TripAndDays
import com.apps.travel_app.data.room.entity.TripStep


class Trip() {
    var id: Int = 0
    var startingPoint: Destination = Destination()
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
    var sharedWith: List<String> = arrayListOf()
    var creatorId: String = ""

    fun getTripStep(tripId: Int): List<TripStep> {
        val steps = arrayListOf<TripStep>()
        destinationsPerDay.forEachIndexed { index, day ->
            day.forEach { step ->
                val _step = step.toTripStep(tripId, index)
                steps.add(_step)
            }
        }
        return steps
    }

    fun toTripDb(startingtripId: String): Trip {
        return com.apps.travel_app.data.room.entity.Trip(
            tid = id,
            attributes = attributes.joinToString("|"),
            creator = creator,
            thumbnailUrl = thumbnailUrl,
            rating = rating,
            description = description,
            season = season,
            creationDate = creationDate,
            name = name,
            starting_location = startingtripId,
            sharedWith = sharedWith.joinToString("|"),
            creator_id = creatorId
        )
    }

    fun fromTripDb(trip: com.apps.travel_app.data.room.entity.Trip) {
        id = trip.tid
        attributes = trip.attributes.split("|")
        sharedWith = trip.sharedWith.split("|")
        creator = trip.creator
        season = trip.season
        thumbnailUrl = trip.thumbnailUrl
        rating = trip.rating
        description = trip.description
        creationDate = trip.creationDate ?: ""
        name = trip.name
        creatorId = trip.creator_id
    }

    fun fromTripDb(trip: TripAndDays) {
        id = trip.trip.tid
        attributes = trip.trip.attributes.split("|")
        sharedWith = trip.trip.sharedWith.split("|")
        creator = trip.trip.creator
        season = trip.trip.season
        thumbnailUrl = trip.trip.thumbnailUrl
        rating = trip.trip.rating
        description = trip.trip.description
        creationDate = trip.trip.creationDate ?: ""
        name = trip.trip.name
        startingPoint = Destination()
        creatorId = trip.trip.creator_id
        startingPoint.fromLocation(trip.startingPoint)
        val steps = arrayListOf(ArrayList<TripDestination>())
        trip.days.forEach {
            if (it.day >= steps.size) {
                steps.add(it.day, ArrayList<TripDestination>())
            }
            val step = TripDestination()
            step.fromLocation(it)
            steps[it.day].add(step)
        }
        destinationsPerDay = steps
    }
}