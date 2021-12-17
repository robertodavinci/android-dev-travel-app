package com.apps.travel_app.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Trip(
    @PrimaryKey val tid: Int,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "attributes") var attributes: String,
    @ColumnInfo(name = "creator") var creator: String,
    @ColumnInfo(name = "thumbnail_url") var thumbnailUrl: String,
    @ColumnInfo(name = "rating") var rating: Float,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "season") var season: String,
    @ColumnInfo(name = "creation_date") var creationDate: String,
    @ColumnInfo(name = "starting_location") var starting_location: Int,
)
