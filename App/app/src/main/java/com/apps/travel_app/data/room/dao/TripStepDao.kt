package com.apps.travel_app.data.room.dao

import androidx.room.*
import com.apps.travel_app.data.room.entity.TripStep

@Dao
interface TripStepDao {
    @Query("SELECT * FROM TripStep")
    fun getAll(): List<TripStep>

    @Transaction
    @Query("SELECT * FROM TripStep WHERE tsid = :id")
    fun getById(id: Int): TripStep?

    @Query("SELECT * FROM TripStep WHERE tsid IN (:ids)")
    fun loadAllByEntity(ids: IntArray): List<TripStep>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg trips: TripStep)

    @Delete
    fun delete(trip: TripStep)
}
