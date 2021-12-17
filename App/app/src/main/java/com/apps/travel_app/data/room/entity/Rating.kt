package com.apps.travel_app.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Rating(
    @PrimaryKey val rid: Int,
    @ColumnInfo(name = "user_name") val userName: String,
    @ColumnInfo(name = "user_id") val uid: Int?,
    @ColumnInfo(name = "rating") val rating: Float,
    @ColumnInfo(name = "message") val message: String?,
    @ColumnInfo(name = "entity_id") val entity: Int?
)
