package com.apps.travel_app.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val userID: String, // we can either do @AutoGenerate = true or get the one directly from Firebase
    @ColumnInfo(name = "display_name") val displayName: String?
)