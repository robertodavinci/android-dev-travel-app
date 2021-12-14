package com.apps.travel_app.data.rooms

import androidx.compose.ui.graphics.ImageBitmap
import androidx.room.*
import com.apps.travel_app.models.MediumType
import com.apps.travel_app.models.OpeningHour
import com.apps.travel_app.models.TripDestination

@Entity
data class User(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "display_name") val displayName: String,
)

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<User>

    @Query("SELECT * FROM user WHERE display_name LIKE :name LIMIT 1")
    fun findByName(name: String): User

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg users: User)

    @Delete
    fun delete(user: User)
}


@Entity
data class Rating(
    @PrimaryKey val rid: Int,
    @ColumnInfo(name = "user_name") val userName: String,
    @ColumnInfo(name = "user_id") val uid: Int?,
    @ColumnInfo(name = "rating") val rating: Float,
    @ColumnInfo(name = "message") val message: String?,
    @ColumnInfo(name = "entity_id") val entity: Int?
)

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


@Entity
data class Location(
    @PrimaryKey val lid: Int,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "latitude") var latitude: Double,
    @ColumnInfo(name = "longitude") var longitude: Double,
    @ColumnInfo(name = "type") var type: String,
    @ColumnInfo(name = "thumbnail_url") var thumbnailUrl: String,
    @ColumnInfo(name = "rating") var rating: Float,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "address") var address: String?,
    @ColumnInfo(name = "phone_number") var phone_number: String?
)

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

@Entity
data class Trip(
    @PrimaryKey val tid: Int,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "attributes") var attributes: String,
    @ColumnInfo(name = "creator") var creator: String,
    @ColumnInfo(name = "thumbnail_url") var thumbnailUrl: String,
    @ColumnInfo(name = "rating") var rating: Float,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "season") var season: String,
    @ColumnInfo(name = "creation_date") var creationDate: String,
    @ColumnInfo(name = "starting_location") var starting_location: Int,
)

@Dao
interface TripDao {
    @Query("SELECT * FROM trip")
    fun getAll(): List<Trip>

    @Transaction
    @Query("SELECT * FROM trip WHERE tid = :id")
    fun getById(id: Int): TripAndDays?

    @Query("SELECT * FROM trip WHERE tid IN (:ids)")
    fun loadAllByEntity(ids: IntArray): List<Trip>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg trips: Trip): List<Long>

    @Delete
    fun delete(trip: Trip)

    @Query("DELETE FROM Trip WHERE tid IN (:ids)")
    fun deleteById(vararg ids: Int)
}


@Entity
data class TripStep(
    @PrimaryKey val tsid: Int,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "latitude") var latitude: Double,
    @ColumnInfo(name = "longitude") var longitude: Double,
    @ColumnInfo(name = "type") var type: String,
    @ColumnInfo(name = "thumbnail_url") var thumbnailUrl: String,
    @ColumnInfo(name = "rating") var rating: Float,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "address") var address: String?,
    @ColumnInfo(name = "phone_number") var phone_number: String?,
    @ColumnInfo(name = "km") val kmToNextDestination: Float,
    @ColumnInfo(name = "minutes") var minutesToNextDestination: Float,
    @ColumnInfo(name = "medium") var mediumToNextDestination: MediumType? = MediumType.Foot,
    @ColumnInfo(name = "time_start") var hour: String,
    @ColumnInfo(name = "duration_min") var minutes: Int,
    @ColumnInfo(name = "visited") var visited: Boolean,
    @ColumnInfo(name = "notes") var notes: String,
    @ColumnInfo(name = "images") var images: String,
    @ColumnInfo(name = "trip") var trip: Int,
    @ColumnInfo(name = "day") var day: Int
)

data class TripAndDays(
    @Embedded val trip: Trip,
    @Relation(
        parentColumn = "tid",
        entityColumn = "trip"
    )
    val days: List<TripStep>,

    @Relation(
        parentColumn = "starting_location",
        entityColumn = "lid"
    )
    val startingPoint: Location
)

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


@Database(entities = [User::class, Location::class, Rating::class, Trip::class, TripStep::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun locationDao(): LocationDao
    abstract fun ratingDao(): RatingDao
    abstract fun tripDao(): TripDao
    abstract fun tripStepDao(): TripStepDao
}
