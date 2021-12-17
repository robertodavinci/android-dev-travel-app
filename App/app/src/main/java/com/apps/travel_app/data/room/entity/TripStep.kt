package com.apps.travel_app.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.apps.travel_app.models.MediumType

@Entity
data class TripStep(
    @PrimaryKey val tsid: Int,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "latitude") var latitude: Double,
    @ColumnInfo(name = "longitude") var longitude: Double,
    @ColumnInfo(name = "type") var type: String,
    @ColumnInfo(name = "thumbnail_url") var thumbnailUrl: String,
    @ColumnInfo(name = "rating") var rating: Float,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "address") var address: String?,
    @ColumnInfo(name = "phone_number") var phone_number: String?,
    @ColumnInfo(name = "km") val kmToNextDestination: Float,
    @ColumnInfo(name = "minutes") var minutesToNextDestination: Float,
    @ColumnInfo(name = "medium") var mediumToNextDestination: MediumType? = MediumType.Foot,
    @ColumnInfo(name = "time_start") var hour: String,
    @ColumnInfo(name = "duration_min") var minutes: Int,
    @ColumnInfo(name = "visited") var visited: Boolean,
    @ColumnInfo(name = "notes") var notes: String,
    @ColumnInfo(name = "images") var images: String,
    @ColumnInfo(name = "trip") var trip: Int,
    @ColumnInfo(name = "day") var day: Int
)