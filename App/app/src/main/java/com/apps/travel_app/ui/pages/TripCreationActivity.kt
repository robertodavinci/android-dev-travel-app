package com.apps.travel_app.ui.pages

import FaIcons
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.exifinterface.media.ExifInterface
import androidx.exifinterface.media.ExifInterface.*
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.apps.travel_app.data.room.AppDatabase
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.MediumType
import com.apps.travel_app.models.Trip
import com.apps.travel_app.models.TripDestination
import com.apps.travel_app.ui.components.Button
import com.apps.travel_app.ui.components.FlexibleRow
import com.apps.travel_app.ui.components.Heading
import com.apps.travel_app.ui.components.TripStepCard
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.ui.utils.getRealPathFromURI
import com.apps.travel_app.ui.utils.isOnline
import com.apps.travel_app.ui.utils.sendPostRequest
import com.apps.travel_app.user
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.guru.fontawesomecomposelib.FaIcon
import com.skydoves.landscapist.glide.GlideImage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class TripCreationActivity : ComponentActivity() {

    lateinit var thumbnail: MutableState<Bitmap?>
    lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private val permissionStorage = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    private fun galleryImageSelected(result: ActivityResult) {
        val data: Intent = result.data!!
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, data.data)
        val currentImageFile = getRealPathFromURI(data.data!!, this)
        if (!currentImageFile.isNullOrEmpty()) {
            val exif = ExifInterface(currentImageFile)
            val angle = when (exif.getAttributeInt(TAG_ORIENTATION, 1)) {
                ORIENTATION_ROTATE_90 -> 90
                ORIENTATION_ROTATE_180 -> 180
                ORIENTATION_ROTATE_270 -> 270
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
            thumbnail.value = rotatedBitmap
        } else {
            thumbnail.value = bitmap
        }
    }


    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        val permission =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                permissionStorage,
                1
            )
        }

        val intent = intent

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val systemTheme = sharedPref.getBoolean("darkTheme", true)

        val tripId = intent.getIntExtra("tripId", -1)

        resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                galleryImageSelected(result)            }
        }

        super.onCreate(savedInstanceState)

        requireFullscreenMode(window, this)

        setContent {
            thumbnail = remember { mutableStateOf(null) }
            var thumbnailUrl: String? by remember { mutableStateOf(null) }
            var confirmed by remember { mutableStateOf(false) }
            var description by remember { mutableStateOf("") }
            var name by remember { mutableStateOf("") }
            var tags by remember { mutableStateOf(ArrayList<String>()) }
            var selectedDay by remember { mutableStateOf(0) }
            var locationSelection by remember { mutableStateOf(false) }
            var stepCursor by remember { mutableStateOf(0) }
            val initialDestinations: ArrayList<ArrayList<TripDestination>> =
                arrayListOf(ArrayList())
            var destinations by remember { mutableStateOf(initialDestinations) }
            var startingPoint: Destination? by remember { mutableStateOf(null) }
            var days by remember { mutableStateOf(1) }
            var sharedWith by remember { mutableStateOf(ArrayList<String>()) }
            fun fillUp() {
                Thread {
                    if (isOnline(this)) {
                        val request = tripId.toString()
                        val ratingsText = sendPostRequest(request, action = "trip")
                        val gson = Gson()
                        val itemType = object : TypeToken<Trip>() {}.type
                        runOnUiThread {
                            val trip: Trip = gson.fromJson(ratingsText, itemType)
                            description = trip.description
                            name = trip.name
                            tags = trip.attributes as ArrayList<String>
                            selectedDay = 0
                            destinations = trip.destinationsPerDay
                            startingPoint = trip.startingPoint
                            days = trip.destinationsPerDay.size
                            sharedWith = trip.sharedWith as ArrayList<String>
                        }
                    } else {
                        val db = Room.databaseBuilder(
                            this,
                            AppDatabase::class.java, "database-name"
                        ).build()
                        val tripDb = db.tripDao().getById(tripId)
                        val trip = Trip()
                        if (tripDb != null) {
                            trip.fromTripDb(tripDb)
                            runOnUiThread {
                                description = trip.description
                                name = trip.name
                                tags = trip.attributes as ArrayList<String>
                                selectedDay = 0
                                destinations = trip.destinationsPerDay
                                startingPoint = trip.startingPoint
                                days = trip.destinationsPerDay.size
                                sharedWith = trip.sharedWith as ArrayList<String>
                            }
                        }
                    }
                }.start()
            }
            fun upload(thumbnailUrl: String? = null) {
                confirmed = true
                if (name.isEmpty() || description.isEmpty() || startingPoint == null) {
                    return
                }
                Thread {
                    val gson = Gson()
                    val trip = Trip()
                    trip.id = tripId
                    trip.creatorId = user.id
                    trip.thumbnailUrl = thumbnailUrl ?: ""
                    trip.startingPoint = startingPoint!!
                    trip.name = name
                    trip.description = description
                    trip.attributes = tags
                    val format = SimpleDateFormat("dd/MM/yyy", Locale.ITALIAN)
                    trip.creationDate = format.format(Date())
                    trip.creator = user.email
                    trip.destinationsPerDay = destinations
                    trip.sharedWith = sharedWith
                    val request = gson.toJson(trip)
                    println(request)
                    val id = sendPostRequest(request, action = "saveTrip")
                    trip.id = (id ?: "0").toInt()
                    val db = Room.databaseBuilder(
                        this,
                        AppDatabase::class.java, "database-name"
                    ).build()
                    db.locationDao().insertAll(trip.startingPoint.toLocation())
                    val tripId = db.tripDao()
                        .insertAll(trip.toTripDb(trip.startingPoint.id))[0]

                    val tripDao = db.tripStepDao()
                    trip.getTripStep(tripId.toInt()).forEach {
                        tripDao.insertAll(it)
                    }
                    finish()
                }.start()
            }
            fun save() {
                if (thumbnail.value != null && thumbnailUrl == null) {
                    val bitmap = thumbnail.value
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
            if (tripId > -1) {
                fillUp()
            }
            Travel_AppTheme {

                if (locationSelection) {
                    LocationSelection(this, this, onBack = {
                        locationSelection = false
                    },
                        onAddStep = {
                            if (it != null) {
                                addStep(it)
                            }
                        },
                        onStartingPointSelected = {
                            if (it != null) {
                                startingPoint = it
                            }
                        })
                } else {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(colors.background)) {

                        Column {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        shape = RoundedCornerShape(
                                            bottomStart = cardRadius,
                                            bottomEnd = cardRadius
                                        )
                                        clip = true
                                    }
                                    .background(primaryColor)
                                    .padding(top = cardPadding * 2),verticalAlignment = CenterVertically) {
                                Button(background = Transparent, onClick = {
                                    finish()
                                }) {
                                    FaIcon(
                                        FaIcons.ArrowLeft,
                                        tint = White
                                    )
                                }
                                TextField(
                                    value = name,
                                    onValueChange = {
                                        name = it
                                    },
                                    isError = confirmed && name.isEmpty(),
                                    modifier = Modifier
                                        .weight(1f),
                                    colors = TextFieldDefaults.textFieldColors(
                                        focusedIndicatorColor = Transparent,
                                        disabledIndicatorColor = Transparent,
                                        unfocusedIndicatorColor = Transparent,
                                        backgroundColor = Transparent,
                                    ),
                                    placeholder = {
                                        Text(
                                            "Trip name",
                                            color = White,
                                            fontSize = textHeading,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .alpha(0.5f)
                                                .fillMaxWidth()
                                        )
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = textHeading,
                                        textAlign = TextAlign.Center
                                    ),
                                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                                )
                                Button(background = Transparent, onClick = {
                                    save()
                                }) {
                                    FaIcon(
                                        FaIcons.Check,
                                        tint = White
                                    )
                                }
                            }
                            LazyColumn(
                                modifier = Modifier
                                    .background(colors.background)
                                    .padding(10.dp)
                                    .fillMaxSize(),
                                horizontalAlignment = CenterHorizontally
                            ) {
                                item {
                                    if (thumbnail.value != null || thumbnailUrl != null) {
                                        Card(
                                            elevation = cardElevation,
                                            backgroundColor = colors.onBackground,
                                            shape = RoundedCornerShape(cardRadius),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .wrapContentHeight(),
                                            onClick = {

                                            }
                                        ) {
                                            GlideImage(
                                                imageModel = thumbnail.value ?: thumbnailUrl,
                                                contentDescription = "",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .heightIn(0.dp, 100.dp)
                                            )

                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                val gallery = Intent(
                                                    Intent.ACTION_PICK,
                                                    MediaStore.Images.Media.INTERNAL_CONTENT_URI
                                                )

                                                resultLauncher.launch(gallery)
                                            }
                                        ) {
                                            Row {
                                                FaIcon(
                                                    FaIcons.PhotoVideo,
                                                    tint = colors.surface
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    "Add a thumbnail",
                                                    color = colors.surface,

                                                    )
                                            }
                                        }
                                    }

                                    Heading("Description")

                                    TextField(
                                        value = description,
                                        onValueChange = {
                                            description = it
                                        },
                                        isError = confirmed && description.isEmpty(),
                                        shape = RoundedCornerShape(cardRadius),
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        colors = TextFieldDefaults.textFieldColors(
                                            focusedIndicatorColor = Transparent,
                                            disabledIndicatorColor = Transparent,
                                            unfocusedIndicatorColor = Transparent,
                                            backgroundColor = colors.background,
                                        ),
                                        placeholder = {
                                            Text(
                                                "Trip description",
                                                color = colors.surface,
                                                modifier = Modifier
                                                    .alpha(0.5f)
                                                    .fillMaxWidth()
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                                        textStyle = TextStyle(
                                            color = colors.surface,
                                            fontWeight = FontWeight.Bold
                                        ),
                                    )

                                    Heading("Visibility")

                                    Row {
                                        Button (
                                            background = if(sharedWith.size == 0) primaryColor else colors.onBackground,
                                            onClick = {sharedWith = arrayListOf()}
                                                ) {
                                            Text("World")
                                        }
                                        Button (
                                            background = if(sharedWith.size > 0) primaryColor else colors.onBackground,
                                            onClick = {sharedWith = arrayListOf(user.email)}
                                        ) {
                                            Text("Me")
                                        }
                                    }

                                    Heading("Tags")

                                    Tags(tags)

                                    Heading("Central location")

                                    if (startingPoint == null) {
                                        Button(
                                            onClick = { locationSelection = true },
                                            border = if (confirmed) BorderStroke(
                                                2.dp,
                                                danger
                                            ) else null,
                                            modifier = Modifier.padding(10.dp)
                                        ) {
                                            Row {
                                                FaIcon(
                                                    FaIcons.MapMarkerAlt,
                                                    tint = colors.surface
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    "Central location",
                                                    color = colors.surface

                                                )
                                            }
                                        }
                                    } else {
                                        Row {

                                            GlideImage(
                                                imageModel = startingPoint?.thumbnailUrl,
                                                contentDescription = "",
                                                modifier = Modifier
                                                    .width(50.dp)
                                                    .height(50.dp)
                                                    .border(
                                                        5.dp,
                                                        colors.surface,
                                                        shape = RoundedCornerShape(100)
                                                    )
                                                    .graphicsLayer {
                                                        shape = RoundedCornerShape(100)
                                                        clip = true
                                                    }
                                            )


                                            Text(
                                                startingPoint?.name!!,
                                                color = colors.surface,
                                                modifier = Modifier
                                                    .padding(5.dp)
                                                    .weight(1f),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = textNormal,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Heading("Days", modifier = Modifier.padding(cardPadding))

                                    LazyRow(
                                        horizontalArrangement = Arrangement.SpaceAround,
                                    ) {
                                        items(days) { i ->
                                            val background =
                                                if (i == selectedDay) primaryColor else colors.onBackground
                                            val foreground =
                                                if (i == selectedDay) White else colors.surface
                                            Button(
                                                onClick = { selectedDay = i; stepCursor = 0 },
                                                modifier = Modifier.padding(5.dp),
                                                background = background
                                            ) {
                                                Column(horizontalAlignment = CenterHorizontally) {
                                                    Text(
                                                        (i + 1).toString(),
                                                        color = foreground,
                                                        fontSize = textHeading
                                                    )
                                                    Text(
                                                        "day",
                                                        color = foreground,
                                                        fontSize = textSmall
                                                    )
                                                }
                                            }
                                        }
                                        item {
                                            Button(
                                                onClick = {
                                                    days++
                                                    val destinationsPerDay =
                                                        destinations.clone() as ArrayList<ArrayList<TripDestination>>
                                                    destinationsPerDay.add(ArrayList())
                                                    destinations = destinationsPerDay

                                                    selectedDay = days - 1
                                                    stepCursor = 0
                                                },
                                                modifier = Modifier.padding(5.dp)
                                            ) {
                                                Column(horizontalAlignment = CenterHorizontally) {
                                                    FaIcon(
                                                        FaIcons.Plus,
                                                        tint = colors.surface,
                                                        modifier = Modifier.padding(4.dp)
                                                    )
                                                    Text(
                                                        "day",
                                                        color = colors.surface,
                                                        fontSize = textSmall
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Heading("Steps", modifier = Modifier.padding(cardPadding))

                                    if (destinations[selectedDay].size <= 0) {
                                        Button(
                                            onClick = { locationSelection = true },
                                        ) {
                                            Row {
                                                FaIcon(
                                                    FaIcons.MapMarkerAlt,
                                                    tint = colors.surface
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    "First step",
                                                    color = colors.surface

                                                )
                                            }
                                        }
                                    } else {
                                        destinations[selectedDay].forEachIndexed { index, place ->
                                            Column(horizontalAlignment = Start) {
                                                if (index > 0) {
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(start = 25.dp)
                                                            .width(2.dp)
                                                            .height(25.dp)
                                                            .background(
                                                                colors.surface
                                                            )
                                                    )
                                                }
                                                TripStepCard(place, index, changeable = true)
                                                if (index < destinations[selectedDay].size - 1) {
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(start = 25.dp)
                                                            .width(2.dp)
                                                            .height(25.dp)
                                                            .background(
                                                                colors.surface
                                                            )
                                                    )
                                                    if (place.mediumToNextDestination != null) {
                                                        Row(
                                                            modifier = Modifier
                                                                .padding(
                                                                    start = 20.dp,
                                                                    end = 5.dp,
                                                                    top = 5.dp,
                                                                    bottom = 10.dp
                                                                )
                                                                .fillMaxWidth(),
                                                            horizontalArrangement = SpaceBetween
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.align(
                                                                    CenterVertically
                                                                )
                                                            ) {
                                                                FaIcon(
                                                                    MediumType.mediumTypeToIcon(
                                                                        place.mediumToNextDestination!!
                                                                    ),
                                                                    tint = colors.surface
                                                                )
                                                                Text(
                                                                    "${place.minutesToNextDestination.toInt()} minutes (${place.kmToNextDestination} km)",
                                                                    color = colors.surface,
                                                                    fontSize = textSmall,
                                                                    modifier = Modifier
                                                                        .padding(start = 20.dp)
                                                                        .align(CenterVertically)
                                                                )
                                                            }
                                                            IconButton(
                                                                onClick = {
                                                                    stepCursor = index + 1
                                                                    locationSelection = true
                                                                },
                                                                modifier = Modifier
                                                                    .size(22.dp, 22.dp)
                                                                    .align(CenterVertically)
                                                            ) {
                                                                FaIcon(
                                                                    FaIcons.Plus,
                                                                    size = 18.dp,
                                                                    tint = colors.surface,
                                                                )
                                                            }
                                                        }

                                                    }
                                                }
                                            }
                                        }
                                        Button(
                                            onClick = {
                                                stepCursor = destinations[selectedDay].size
                                                locationSelection = true
                                            },
                                        ) {
                                            Row {
                                                FaIcon(
                                                    FaIcons.Hiking,
                                                    tint = colors.surface
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    "Add step",
                                                    color = colors.surface

                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }


        }

    }
    
    

    @Composable
    fun Tags(_tags: ArrayList<String>) {
        var tags by remember { mutableStateOf(_tags) }
        var newTag by remember { mutableStateOf("") }
        var selectedTag by remember { mutableStateOf("") }

        val infiniteTransition = rememberInfiniteTransition()
        val rotate by infiniteTransition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        FlexibleRow(
            alignment = CenterHorizontally,
            modifier = Modifier
                .scale(0.8f)
        ) {
            tags.forEach {
                val text = it
                Button(
                    onClick = {
                        if (selectedTag == text) {
                            val _tags = tags.clone() as ArrayList<String>
                            _tags.remove(text)
                            tags = _tags
                        } else {
                            selectedTag = text
                        }
                    },
                    modifier = Modifier
                        .padding(5.dp)
                        .rotate(if (selectedTag == text) rotate else 0f),
                    background = if (selectedTag == text) danger else colors.onBackground
                ) {
                    Row {
                        Text(
                            text,
                            color = if (selectedTag == text) White else colors.surface,
                            fontSize = textSmall
                        )
                    }
                }
            }
            TextField(
                value = newTag,
                onValueChange = {
                    newTag = if (it.contains(",")) {
                        tags.add(it.split(",")[0].trim())
                        ""
                    } else {
                        it
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Transparent,
                    disabledIndicatorColor = Transparent,
                    unfocusedIndicatorColor = Transparent,
                    backgroundColor = Transparent,
                ),
                placeholder = {
                    Text(
                        "Tags split by comma",
                        color = colors.surface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(0.5f),
                        fontSize = textSmall
                    )
                },
                singleLine = true,
                textStyle = TextStyle(
                    color = colors.surface,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
        }
    }


}







