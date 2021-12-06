package com.apps.travel_app.data.room.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.apps.travel_app.data.room.UserPreferences
import com.apps.travel_app.data.room.dao.UserPreferencesDao


@Database(entities = [UserPreferences::class], version = 1)
abstract class DB: RoomDatabase() {
    abstract fun userPreferencesDao(): UserPreferencesDao
}