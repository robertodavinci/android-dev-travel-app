package com.apps.travel_app.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.apps.travel_app.data.room.dao.*
import com.apps.travel_app.data.room.entity.*

@Database(entities = [User::class, Location::class, Rating::class, Trip::class, TripStep::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun locationDao(): LocationDao
    abstract fun ratingDao(): RatingDao
    abstract fun tripDao(): TripDao
    abstract fun tripStepDao(): TripStepDao
}
