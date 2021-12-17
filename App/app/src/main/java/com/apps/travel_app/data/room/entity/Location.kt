package com.apps.travel_app.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Location(
    @PrimaryKey val lid: Int,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "latitude") var latitude: Double,
    @ColumnInfo(name = "longitude") var longitude: Double,
    @ColumnInfo(name = "type") var type: String,
    @ColumnInfo(name = "thumbnail_url") var thumbnailUrl: String,
    @ColumnInfo(name = "rating") var rating: Float,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "address") var address: String?,
    @ColumnInfo(name = "phone_number") var phone_number: String?
)