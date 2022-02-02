package com.apps.travel_app.ui.pages.viewmodels

/**
 * A view model used for fetching and forwarding the data regarding selecting
 * and adding a new location to the map. This function can only be accessed when
 * adding an additional step to the trip, and when Google search doesn't provide
 * us with a satisfying location.
 * Handles the map initialization, user inputs, marker adding, location saving, and
 * location uploading to the server.
 */

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.provider.MediaStore
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.*
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.room.Room
import com.apps.travel_app.data.room.db.AppDatabase
import com.apps.travel_app.models.Destination
import com.apps.travel_app.ui.theme.mapStyle
import com.apps.travel_app.ui.utils.*
import com.google.android.libraries.maps.CameraUpdate
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.*
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import java.util.*

class LocationSelectionViewModel(activity: Activity, val onAddStep: (Destination) -> Unit,val onMainDestinationSelected: (Destination) -> Unit): ViewModel() {
    private val destinations = HashMap<Int, Destination>()

    var center = LatLng(44.0, 10.0)
    var map: GoogleMap? by  mutableStateOf(null)
    var mapView: MapView? by  mutableStateOf(null)
    var mapLoaded by  mutableStateOf(false)
    var destinationSelected by  mutableStateOf(false)
    var mainDestinationSelected by  mutableStateOf(false)
    var stepAdded by mutableStateOf(false)
    val mainActivity = activity
    var userIsAddingAPlace by mutableStateOf(false)
    var addedMarker: Marker? by mutableStateOf(null)
    var newDestination by mutableStateOf(Destination())
    var newDestinationThumbnail by mutableStateOf<Bitmap?>(null)
    var error by mutableStateOf("")
    var newDestinationName by mutableStateOf("")
    var newDestinationDesc by mutableStateOf("")
    var newDestinationType by mutableStateOf("")
    var openCards by  mutableStateOf(true)
    var searchTerm by  mutableStateOf("") 

    var cities by
        mutableStateOf(ArrayList<Destination>())

    fun mapInit(context: Context) {
        map!!.uiSettings.isZoomControlsEnabled = false

        map?.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                context,
                mapStyle
            )
        )

        map!!.uiSettings.isMapToolbarEnabled = false

        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 6f))

        map?.setOnMarkerClickListener { marker -> markerClick(marker) }

        map?.setOnMapClickListener { i ->
            if (!mapClick(i))
                userIsAddingAPlace = false
        }
    }

    fun moveTo(index: Int) {
        if (index >= 0 && index < cities.size && map != null) {
            val cameraPosition = CameraPosition.Builder()
                .target(LatLng(cities[index].latitude,cities[index].longitude))
                .zoom(6f)
                .build()
            val cu: CameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
            map!!.animateCamera(cu)
        }
    }

    fun mapClick(pos: LatLng): Boolean {
        if (map == null || !userIsAddingAPlace)
            return false
        val markerOptions = MarkerOptions()
            .position(
                pos
            )
            .icon(numberedMarker(0))
            .title("")
            .zIndex(5f)
        addedMarker = map!!.addMarker(markerOptions)

        markerPopUp(addedMarker!!)
        newDestination = Destination()
        return true
    }

    fun removeMarker(marker: Marker) {
        marker.remove()
    }

    private fun addMarker(position: LatLng, index: Int, name: String, destination: Destination) {
        if (map == null)
            return
        val markerOptions = MarkerOptions()
            .position(
                position
            )
            .icon(numberedMarker(index + 1))
            .title(name)
            .zIndex(5f)
        val marker = map!!.addMarker(markerOptions)
        destinations[marker.hashCode()] = destination

        markerPopUp(marker)
    }

    fun search(text: String) {
        val region = map?.projection?.visibleRegion ?: return

        Thread {
            val points = arrayListOf(
                region.nearLeft,
                region.farLeft,
                region.farRight,
                region.nearRight,
                region.nearLeft
            )
            val request = "{\"area\":" + points.joinToString(",", "[", "]") { e -> // NON-NLS
                "[${e.latitude},${e.longitude}]"
            } + ", \"text\": \"$text\"}"
            println(request)
            val resultText = sendPostRequest(request, action = "search") // NON-NLS
            if (!resultText.isNullOrEmpty()) {
                try {
                val gson = Gson()
                val itemType = object : TypeToken<Response>() {}.type
                val response: Response = gson.fromJson(resultText, itemType)
                cities = response.cities
                cities.addAll(response.places)

                var index = 0

                mainActivity.runOnUiThread {
                    if (map != null) {
                        map!!.clear()
                        cities.forEach {
                            addMarker(
                                LatLng(it.latitude, it.longitude),
                                index++,
                                it.name,
                                it
                            )
                        }
                        openCards = true
                    }
                }
                } catch (e: Exception) {
                    errorMessage(mainActivity.window.decorView.rootView).show()
                }
            }
        }.start()
    }


    fun markerClick(marker: Marker): Boolean {
        mainDestinationSelected = false
        stepAdded = false
        val destination = destinations[marker.hashCode()]
        if (destination != null) {
            //currentDestination = destination
            destinationSelected = true
            return true
        }
        destinationSelected = false

        return false
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
            newDestinationThumbnail = rotatedBitmap
        } else {
            newDestinationThumbnail = bitmap
        }
    }

    fun saveNewDestination() {
        if (newDestinationThumbnail != null) {
            val bitmap = newDestinationThumbnail
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
            upload("")
        }
    }

    fun upload(thumbnailUrl: String? = null) {
        newDestination.description = newDestinationDesc
        newDestination.name = newDestinationName
        newDestination.type = newDestinationType
        if (newDestination.name.isEmpty() || newDestination.description.isEmpty()) {
            return
        }

        try {
            var request = "{\"name\":\"${newDestination.name}\",\"pos\":[${newDestination.latitude},${newDestination.longitude}]}"
            val names = sendPostRequest(request, action = "similarCities") // NON-NLS
            if (!names.isNullOrEmpty()) {
                error = "There exist other locations with similar name. Your submission will be evaluated."
            }
            Thread {

                val gson = Gson()
                newDestination.thumbnailUrl = thumbnailUrl ?: ""

                request = gson.toJson(newDestination)
                println(request)
                val id = sendPostRequest(request, action = "saveCity") // NON-NLS

                if (id.isNullOrEmpty() || id.toInt() <= 0) {
                    errorMessage(mainActivity.window.decorView.rootView).show()
                } else {
                    newDestination.id = id
                    val db = Room.databaseBuilder(
                        mainActivity,
                        AppDatabase::class.java, AppDatabase.NAME
                    ).build()
                    db.locationDao().insertAll(newDestination.toLocation())
                    /*onAddStep(newDestination)
                    onMainDestinationSelected(newDestination)*/
                    newDestination = Destination()
                    stepAdded = true
                    addedMarker = null
                    userIsAddingAPlace = false
                }

            }.start()
        } catch (e: Exception) {
            errorMessage(mainActivity.window.decorView.rootView).show()
        }
    }

    fun closeNewDestination() {
        newDestination = Destination()
        stepAdded = true
        addedMarker = null
        userIsAddingAPlace = false
        addedMarker?.remove()
        addedMarker = null
    }


}