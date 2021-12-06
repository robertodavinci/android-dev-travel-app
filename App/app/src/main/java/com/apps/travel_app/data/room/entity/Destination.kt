package com.apps.travel_app.data.room.entity

import androidx.compose.ui.graphics.ImageBitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
open class Destination {
    @PrimaryKey(autoGenerate = true)
    val userID: Int = 0 // we can either do @AutoGenerate = true or get the one directly from Firebase
    @ColumnInfo(name = "latitude")
    val latitude: Double? = 0.0
    @ColumnInfo(name = "longitude")
    val longitude: Double? = 0.0
    @ColumnInfo(name = "type")
    val type: String? = ""
    @ColumnInfo(name = "country")
    val country: Country? = null
    @ColumnInfo(name = "thumbnail")
    val thumbnail: ImageBitmap? = null
    @ColumnInfo(name = "name")
    val name: String? = ""
    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String? = ""
    @ColumnInfo(name = "rating")
    val rating: Float? = 0f
    @ColumnInfo(name = "description")
    val description: String? = ""
    @ColumnInfo(name = "price_level")
    val priceLevel: Float? = 0f
    @ColumnInfo(name = "open_closed")
    val isOpen: Boolean? = null
}