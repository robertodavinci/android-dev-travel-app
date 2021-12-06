package com.apps.travel_app.data.room.dao

import androidx.room.*
import com.apps.travel_app.data.room.User
import com.apps.travel_app.data.room.UserPreferences


// https://gist.github.com/florina-muntenescu/1c78858f286d196d545c038a71a3e864
interface BaseDao<T>{
    @Insert
    fun insert(obj: T)
    @Insert
    fun insert(vararg obj: T)
    @Update
    fun update(obj: T)
    @Delete
    fun delete(obj: T)
}


// DAOs support inheritance
@Dao
abstract class UserPreferencesDao : BaseDao<UserPreferences> {
   /* @Query("SELECT * FROM users")
    abstract fun getAll(): List<User>

    @Insert
    abstract fun insertAll(vararg users:User)

    @Query("DELETE FROM users")
    abstract fun deleteAll(vararg user: User)*/
}