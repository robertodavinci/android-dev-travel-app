package com.apps.travel_app.models

import androidx.compose.ui.graphics.ImageBitmap
import com.apps.travel_app.data.room.entity.Trip
import com.apps.travel_app.data.room.entity.TripAndDays
import com.apps.travel_app.data.room.entity.TripStep
import com.google.gson.Gson


class Trip() {
    companion object {
        const val NOTSAVEDATALL = "-1"
    }
    var id: String = ""
    var mainDestination: Destination = Destination()
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
    var discussion: ArrayList<Message> = arrayListOf()
    var incomplete: Boolean = false

    fun getTripStep(tripId: String): List<TripStep> {
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
            creator_id = creatorId,
            incomplete = incomplete
        )
    }

    fun fromTripDb(trip: com.apps.travel_app.data.room.entity.Trip) {
        id = trip.tid
        attributes = trip.attributes.split("|").toList()
        sharedWith = trip.sharedWith.split("|").toList()
        creator = trip.creator
        season = trip.season
        thumbnailUrl = trip.thumbnailUrl
        rating = trip.rating
        description = trip.description
        creationDate = trip.creationDate ?: ""
        name = trip.name
        creatorId = trip.creator_id
        incomplete = trip.incomplete
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
        mainDestination = Destination()
        creatorId = trip.trip.creator_id
        mainDestination.fromLocation(trip.mainDestination)
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
        incomplete = trip.trip.incomplete
    }

    fun clone(): com.apps.travel_app.models.Trip
    {
        val stringAnimal = Gson().toJson(this, com.apps.travel_app.models.Trip::class.java)
        return Gson().fromJson(stringAnimal, com.apps.travel_app.models.Trip::class.java)
    }
}