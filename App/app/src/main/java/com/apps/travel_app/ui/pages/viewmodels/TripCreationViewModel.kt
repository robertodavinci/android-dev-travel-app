package com.apps.travel_app.ui.pages.viewmodels

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.room.Room
import com.apps.travel_app.data.room.AppDatabase
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.MediumType
import com.apps.travel_app.models.Trip
import com.apps.travel_app.models.TripDestination
import com.apps.travel_app.ui.utils.errorMessage
import com.apps.travel_app.ui.utils.getRealPathFromURI
import com.apps.travel_app.ui.utils.isOnline
import com.apps.travel_app.ui.utils.sendPostRequest
import com.apps.travel_app.user
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class TripCreationViewModel(activity: Activity, tripId: Int) : ViewModel() {
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
    val mainActivity = activity
    val id = tripId
    var thumbnail: Bitmap? by mutableStateOf(null)


    fun fillUp() {
        Thread {
            if (isOnline(mainActivity)) {
                try {
                    val request = id.toString() // NON-NLS
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
                    }
                } catch (e: Exception) {
                    errorMessage(mainActivity.window.decorView.rootView).show()
                }
            } else {
                val db = Room.databaseBuilder(
                    mainActivity,
                    AppDatabase::class.java, "database-name"
                ).build()
                val tripDb = db.tripDao().getById(id)
                val trip = Trip()
                if (tripDb != null) {
                    trip.fromTripDb(tripDb)
                    mainActivity.runOnUiThread {
                        description = trip.description
                        name = trip.name
                        tags = trip.attributes as ArrayList<String>
                        selectedDay = 0
                        destinations = trip.destinationsPerDay
                        mainDestination = trip.mainDestination
                        days = trip.destinationsPerDay.size
                        sharedWith = trip.sharedWith as ArrayList<String>
                    }
                }
            }
        }.start()
    }

    fun upload(thumbnailUrl: String? = null) {
        confirmed = true
        if (name.isEmpty() || description.isEmpty() || mainDestination == null) {
            return
        }
        loading = true
        Thread {
            try {
                val gson = Gson()
                val trip = Trip()
                trip.id = id
                trip.creatorId = user.id
                trip.thumbnailUrl = thumbnailUrl ?: ""
                trip.mainDestination = mainDestination!!
                trip.name = name
                trip.description = description
                trip.attributes = tags
                val format = SimpleDateFormat("dd/MM/yyy", Locale.ITALIAN)
                trip.creationDate = format.format(Date())
                trip.creator = user.email
                trip.destinationsPerDay = destinations
                trip.sharedWith = sharedWith
                val request = gson.toJson(trip) // NON-NLS
                println(request)
                val id = sendPostRequest(request, action = "saveTrip") // NON-NLS
                mainActivity.runOnUiThread { loading = false }

                if (id?.toInt()!! <= 0) {
                    errorMessage(mainActivity.window.decorView.rootView).show()
                } else {
                    trip.id = id.toInt()
                    val db = Room.databaseBuilder(
                        mainActivity,
                        AppDatabase::class.java, "database-name"
                    ).build()
                    db.locationDao().insertAll(trip.mainDestination.toLocation())
                    val tripId = db.tripDao()
                        .insertAll(trip.toTripDb(trip.mainDestination.id))[0]

                    val tripDao = db.tripStepDao()
                    trip.getTripStep(tripId.toInt()).forEach {
                        tripDao.insertAll(it)
                    }
                    mainActivity.finish()
                }
            } catch (e: Exception) {
                errorMessage(mainActivity.window.decorView.rootView).show()
            }
        }.start()
    }

    fun save() {
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
                upload()
            }.addOnSuccessListener {
                storageRef.child(path).downloadUrl.addOnSuccessListener {
                    upload(it.toString())
                }

            }
        } else {
            upload(thumbnailUrl)
        }
    }

    fun addStep(destination: Destination) {
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
    }

    init {
        if (tripId > -1) {
            fillUp()
        }
    }
}