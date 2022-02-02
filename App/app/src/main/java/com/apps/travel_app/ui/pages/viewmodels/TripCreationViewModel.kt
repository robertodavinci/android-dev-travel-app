package com.apps.travel_app.ui.pages.viewmodels
/**
 * A view model used for fetching and forwarding the data regarding Trip creation.
 * Used in combination with TripCreationActivity. Has functions of creating a new trip,
 * editing its details, saving a trip that is not completed, uploading a completed trip,
 * selecting and adding gallery images, and adding a step to the trip. Maintains constant
 * communication with the database.
 */
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.room.Room
import com.apps.travel_app.R
import com.apps.travel_app.data.room.db.AppDatabase
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.MediumType
import com.apps.travel_app.models.Trip
import com.apps.travel_app.models.TripDestination
import com.apps.travel_app.ui.theme.yellow
import com.apps.travel_app.ui.utils.errorMessage
import com.apps.travel_app.ui.utils.getRealPathFromURI
import com.apps.travel_app.ui.utils.isOnline
import com.apps.travel_app.ui.utils.sendPostRequest
import com.apps.travel_app.user
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class TripCreationViewModel(val mainActivity: Activity, tripId: String, val onLoad: () -> Unit) : ViewModel() {
    var loading by mutableStateOf(false)
    var thumbnailUrl: String? by mutableStateOf(null)
    var confirmed by mutableStateOf(false)
    var description by mutableStateOf("")
    var name by mutableStateOf("")
    var tags by mutableStateOf(ArrayList<String>())
    var selectedDay by mutableStateOf(0)
    var locationSelection by mutableStateOf(false)
    var stepCursor by mutableStateOf(0)
    private val initialDestinations: ArrayList<ArrayList<TripDestination>> =
        arrayListOf(ArrayList())
    var destinations by mutableStateOf(initialDestinations)
    var mainDestination: Destination? by mutableStateOf(null)
    var days by mutableStateOf(1)
    var sharedWith by mutableStateOf(ArrayList<String>())
    var id = tripId
    var thumbnail: Bitmap? by mutableStateOf(null)
    private var continuePresaving = true

    fun stop() {
        continuePresaving = false
    }

    private fun presave() {
        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post(object : Runnable {
            override fun run() {
                if (continuePresaving) {
                    save(true)
                    mainHandler.postDelayed(this, 10000)
                }
            }
        })
    }

    private fun fillUp() {
        Thread {
            if (isOnline(mainActivity) && !id.contains("_")) {
                try {
                    val request = id // NON-NLS
                    val ratingsText = sendPostRequest(request, action = "trip") // NON-NLS
                    val gson = Gson()
                    val itemType = object : TypeToken<Trip>() {}.type
                    mainActivity.runOnUiThread {
                        val trip: Trip = gson.fromJson(ratingsText, itemType)
                        description = trip.description
                        name = trip.name
                        tags = trip.attributes as ArrayList<String>
                        selectedDay = 0
                        destinations = trip.destinationsPerDay
                        mainDestination = trip.mainDestination
                        days = trip.destinationsPerDay.size
                        sharedWith = trip.sharedWith as ArrayList<String>
                        thumbnailUrl = trip.thumbnailUrl
                        onLoad()
                    }
                } catch (e: Exception) {
                    errorMessage(mainActivity.window.decorView.rootView).show()
                }
            } else {
                val db = Room.databaseBuilder(
                    mainActivity,
                    AppDatabase::class.java, AppDatabase.NAME
                ).build()
                val tripDb = db.tripDao().getById(id)
                val trip = Trip()
                if (tripDb != null) {
                    trip.fromTripDb(tripDb)
                    mainActivity.runOnUiThread {
                        description = trip.description
                        name = trip.name
                        tags = ArrayList<String>(trip.attributes)
                        selectedDay = 0
                        destinations = trip.destinationsPerDay
                        mainDestination = trip.mainDestination
                        if (trip.mainDestination.id.isEmpty())
                            mainDestination = null
                        days = trip.destinationsPerDay.size
                        sharedWith = ArrayList<String>(trip.sharedWith)
                        sharedWith.forEachIndexed { i, it -> if(it.isEmpty()) sharedWith.removeAt(i) }
                        thumbnailUrl = trip.thumbnailUrl
                        onLoad()
                    }
                }
            }

        }.start()
    }
    
    fun exit() {
        if (id == Trip.NOTSAVEDATALL || id.contains("_")) {
            Thread {
                val db = Room.databaseBuilder(
                    mainActivity,
                    AppDatabase::class.java, AppDatabase.NAME
                ).build()
                val exTrip = db.tripDao()
                    .getById(id)

                if (exTrip != null)
                    db.tripDao().delete(exTrip.trip)
                mainActivity.runOnUiThread {
                    mainActivity.finish()
                }
            }.start()
        } else {
            mainActivity.finish()
        }
    }

    private fun upload(thumbnailUrl: String? = null, incomplete: Boolean = false) {
        confirmed = true
        if (!incomplete && (name.isEmpty() || description.isEmpty() || mainDestination == null)) {
            val sb = Snackbar.make(
                mainActivity.window.decorView.rootView, mainActivity.resources.getString(R.string.not_enough_info),
                Snackbar.LENGTH_LONG)
            sb.view.contentDescription = "Error"
            sb.view.setBackgroundColor(yellow.toArgb())
            sb.show()
            return
        }
        loading = !incomplete && true
        Thread {
            try {
                val gson = Gson()
                val trip = Trip()
                trip.id = id
                trip.creatorId = user.displayName
                trip.thumbnailUrl = thumbnailUrl ?: ""
                this.thumbnailUrl = thumbnailUrl
                if (mainDestination != null)
                    trip.mainDestination = mainDestination!!
                trip.name = name
                trip.description = description
                trip.attributes = tags
                val format = SimpleDateFormat("dd/MM/yyy", Locale.getDefault())
                trip.creationDate = format.format(Date())
                trip.creator = user.displayName ?: "?"
                trip.destinationsPerDay = destinations
                trip.sharedWith = sharedWith
                if (!incomplete) {


                    val request = gson.toJson(trip) // NON-NLS
                    println(request)
                    val newId = sendPostRequest(request, action = "saveTrip") // NON-NLS
                    mainActivity.runOnUiThread { loading = false }

                    val db = Room.databaseBuilder(
                        mainActivity,
                        AppDatabase::class.java, AppDatabase.NAME
                    ).build()
                    db.locationDao().insertAll(trip.mainDestination.toLocation())
                    val exTrip = db.tripDao()
                        .getById(id)

                    if (exTrip != null)
                        db.tripDao().delete(exTrip.trip)


                    if (newId?.toInt()!! <= 0) {
                        errorMessage(mainActivity.window.decorView.rootView).show()
                    } else {
                        trip.incomplete = false
                        trip.id = newId

                        db.locationDao().insertAll(trip.mainDestination.toLocation())
                        db.tripDao()
                            .insertAll(trip.toTripDb(trip.mainDestination.id))[0]

                        val tripDao = db.tripStepDao()
                        trip.getTripStep(trip.id).forEach {
                            tripDao.insertAll(it)
                        }
                        mainActivity.finish()
                    }
                } else {
                    trip.incomplete = true
                    if (id == Trip.NOTSAVEDATALL) {
                        trip.id = "_" + System.currentTimeMillis()
                        id = trip.id
                    }
                    val db = Room.databaseBuilder(
                        mainActivity,
                        AppDatabase::class.java, AppDatabase.NAME
                    ).build()
                    db.locationDao().insertAll(trip.mainDestination.toLocation())
                    db.tripDao()
                        .insertAll(trip.toTripDb(trip.mainDestination.id))[0]

                    val tripDao = db.tripStepDao()
                    trip.getTripStep(trip.id).forEach {
                        tripDao.insertAll(it)
                    }
                }
                FirebaseMessaging.getInstance().subscribeToTopic("trip" + trip.id)
            } catch (e: Exception) {
                errorMessage(mainActivity.window.decorView.rootView).show()
            }
        }.start()
    }

    fun save(incomplete: Boolean = false) {
        if (thumbnail != null && thumbnailUrl == null) {
            val bitmap = thumbnail
            val baos = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val path = "images/${Date().time}.jpg"
            val mountainImagesRef = storageRef.child(path)

            val uploadTask = mountainImagesRef.putBytes(data)
            uploadTask.addOnFailureListener {
                upload(incomplete = incomplete)
            }.addOnSuccessListener {
                storageRef.child(path).downloadUrl.addOnSuccessListener {
                    upload(it.toString(),incomplete)
                }

            }
        } else {
            upload(thumbnailUrl, incomplete)
        }
    }

    fun addStep(destination: Destination): TripDestination {
        val destinationsPerDay =
            destinations.clone() as ArrayList<ArrayList<TripDestination>>
        val newDestination = (TripDestination)(destination)
        if (destinations[selectedDay].size > 0 && stepCursor > 0) {
            val oldDestination = (destinations[selectedDay])[stepCursor - 1]
            oldDestination.kmToNextDestination = 1f
            oldDestination.minutesToNextDestination = 1f
            oldDestination.mediumToNextDestination = MediumType.Foot
        }
        destinationsPerDay[selectedDay].add(stepCursor++, newDestination)
        destinations = destinationsPerDay
        return newDestination
    }

    fun galleryImageSelected(result: ActivityResult) {
        val data: Intent = result.data!!
        val bitmap = MediaStore.Images.Media.getBitmap(mainActivity.contentResolver, data.data)
        val currentImageFile = getRealPathFromURI(data.data!!, mainActivity)
        if (!currentImageFile.isNullOrEmpty()) {
            val exif = ExifInterface(currentImageFile)
            val angle = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }

            val matrix = Matrix()

            matrix.postRotate(angle.toFloat())

            val rotatedBitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
            thumbnail = rotatedBitmap
        } else {
            thumbnail = bitmap
        }
        thumbnailUrl = null
    }

    init {
        if (tripId != Trip.NOTSAVEDATALL) {
            fillUp()
        }
        presave()
    }
}