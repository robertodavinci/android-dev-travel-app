package com.apps.travel_app.data.room.dao

import androidx.room.*
import com.apps.travel_app.data.room.entity.Location

@Dao
interface LocationDao {
    @Query("SELECT * FROM location")
    fun getAll(): List<Location>

    @Query("SELECT * FROM location WHERE saved = 1")
    fun getAllSaved(): List<Location>

    @Query("SELECT * FROM location WHERE lid = :id")
    fun getById(id: String): Location?

    @Query("SELECT * FROM location WHERE lid = :id and saved = 1")
    fun getSavedById(id: String): Location?

    @Query("SELECT * FROM location WHERE lid IN (:ids)")
    fun loadAllByEntity(ids: IntArray): List<Location>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg locations: Location)

    @Delete
    fun delete(location: Location)
}