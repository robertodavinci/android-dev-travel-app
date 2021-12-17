package com.apps.travel_app.data.room.dao

import androidx.room.*
import com.apps.travel_app.data.room.entity.Rating

@Dao
interface RatingDao {
    @Query("SELECT * FROM rating")
    fun getAll(): List<Rating>

    @Query("SELECT * FROM rating WHERE entity_id IN (:entityIds)")
    fun loadAllByEntity(entityIds: IntArray): List<Rating>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg ratings: Rating)

    @Delete
    fun delete(rating: Rating)
}
