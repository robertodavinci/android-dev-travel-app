package com.apps.travel_app.data.room.entity

import androidx.compose.ui.graphics.ImageBitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.apps.travel_app.data.room.User

@Entity
data class Rating(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "rating") val user: User,
    @ColumnInfo(name = "rating") val rating: Float,
    @ColumnInfo(name = "message") val message: String?,
    @ColumnInfo(name = "entity_id") val entityId: Int?,// what's this used for?
)