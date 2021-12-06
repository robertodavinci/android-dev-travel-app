package com.apps.travel_app.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserPreferences(
    @PrimaryKey val userID: String,
    @ColumnInfo(name = "colour_mode") val darkMode: Boolean?,
    @ColumnInfo(name = "economy_level") val ecoLevel: Int?,
    @ColumnInfo(name = "real_name") val realName: String?,
    @ColumnInfo(name = "real_surname") val realSurname: String?,
)