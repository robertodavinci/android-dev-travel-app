package com.apps.travel_app.data.room.entity

import androidx.compose.ui.graphics.ImageBitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.apps.travel_app.models.TripDestination

@Entity
data class Trip(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "starting_point") val destination: Destination,
    @ColumnInfo(name = "days") val days: Int,
    @ColumnInfo(name = "attributes") val attributes:List<String>,
    @ColumnInfo(name = "creator") val creator: String,
    @ColumnInfo(name = "thumbnail") val thumbnail: ImageBitmap,
    @ColumnInfo(name = "name") val name: String = "",
    @ColumnInfo(name = "thumbnail_url") val thumbnailurl: String = "",
    @ColumnInfo(name = "rating") val rating: Float,
    @ColumnInfo(name = "destinations") val destinations: ArrayList<TripDestination>,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "season") val season: String,
    @ColumnInfo(name = "days") val creationDate: String,
    @ColumnInfo(name = "mine") val mine: Boolean


)