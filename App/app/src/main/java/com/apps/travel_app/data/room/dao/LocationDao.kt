package com.apps.travel_app.data.room.dao

import androidx.room.*
import com.apps.travel_app.data.room.entity.Location

@Dao
interface LocationDao {
    @Query("SELECT * FROM location")
    fun getAll(): List<Location>

    @Query("SELECT * FROM location WHERE lid = :id")
    fun getById(id: Int): Location?

    @Query("SELECT * FROM location WHERE lid IN (:ids)")
    fun loadAllByEntity(ids: IntArray): List<Location>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg locations: Location): List<Long>

    @Delete
    fun delete(location: Location)
}