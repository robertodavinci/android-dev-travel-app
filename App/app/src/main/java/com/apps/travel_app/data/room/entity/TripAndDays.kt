package com.apps.travel_app.data.room.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TripAndDays(
    @Embedded val trip: Trip,
    @Relation(
        parentColumn = "tid",
        entityColumn = "trip"
    )
    val days: List<TripStep>,

    @Relation(
        parentColumn = "starting_location",
        entityColumn = "lid"
    )
    val startingPoint: Location
)