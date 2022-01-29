package com.apps.travel_app.data.room.dao

import androidx.room.*
import com.apps.travel_app.data.room.entity.Trip
import com.apps.travel_app.data.room.entity.TripAndDays

@Dao
interface TripDao {
    @Query("SELECT * FROM trip")
    fun getAll(): List<Trip>

    @Transaction
    @Query("SELECT * FROM trip WHERE tid = :id")
    fun getById(id: String): TripAndDays?

    @Query("SELECT * FROM trip WHERE tid IN (:ids)")
    fun loadAllByEntity(ids: Array<String>): List<Trip>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg trips: Trip): List<Long>

    @Delete
    fun delete(trip: Trip)

    @Query("DELETE FROM Trip WHERE tid IN (:ids)")
    fun deleteById(vararg ids: String)
}