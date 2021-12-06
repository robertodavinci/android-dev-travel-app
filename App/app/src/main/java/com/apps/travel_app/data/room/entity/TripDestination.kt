package com.apps.travel_app.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.apps.travel_app.models.MediumType

@Entity // we can optionall put (ignoredColumns = [""]) to avoid some columns
data class TripDestination(
    //@PrimaryKey val name: String, // we can either do @AutoGenerate = true or get the one directly from Firebase
    @ColumnInfo(name = "km_to_next") val kmToNextDestination: Float?,
    @ColumnInfo(name = "min_to_next") val minutesToNextDestination: Float?,
    @ColumnInfo(name = "medium_to_next") val mediumToNextDestination: MediumType?, // UREDI OVO
    @ColumnInfo(name = "hour") val hour: String?,
    @ColumnInfo(name = "minutes") val minutes: Int,
    @ColumnInfo(name = "visited") val visited: Boolean?
) : Destination()