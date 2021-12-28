package com.apps.travel_app.ui.pages

import FaIcons
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.apps.travel_app.ui.utils.sendPostRequest
import com.apps.travel_app.user
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.guru.fontawesomecomposelib.FaIcon
import com.skydoves.landscapist.glide.GlideImage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class TripCreationActivity : ComponentActivity() {

    lateinit var thumbnail: MutableState<Bitmap?>


    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val systemTheme = sharedPref.getBoolean("darkTheme", true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }

        setContent {
            thumbnail = remember {
                mutableStateOf(null)
            }
            var confirmed by remember {mutableStateOf(false)}
            var description by remember { mutableStateOf("") }
            var name by remember { mutableStateOf("") }
            val tags by remember { mutableStateOf(ArrayList<String>()) }
            var selectedDay by remember { mutableStateOf(0) }
            var locationSelection by remember { mutableStateOf(false) }
            var stepCursor by remember { mutableStateOf(0) }
            val initialDestinations: ArrayList<ArrayList<TripDestination>> =
                arrayListOf(ArrayList())
            var destinations by remember { mutableStateOf(initialDestinations) }
            var startingPoint: MutableState<Destination?> = remember { mutableStateOf(null) }
            var days by remember { mutableStateOf(1) }




            fun upload(thumbnailUrl: String? = null) {
                confirmed = true
                if (name.isEmpty() || description.isEmpty() || startingPoint.value == null) {
                    return
                }
                Thread {
                    val gson = Gson()
                    val trip = Trip()
                    trip.creatorId = FirebaseAuth.getInstance().currentUser?.uid.toString()
                    trip.thumbnailUrl = thumbnailUrl ?: ""
                    trip.startingPoint = startingPoint.value!!
                    trip.name = name
                    trip.description = description
                    trip.attributes = tags
                    val format = SimpleDateFormat("dd/MM/yyy", Locale.ITALIAN)
                    trip.creationDate = format.format(Date())
                    trip.creator = user.email
                    trip.destinationsPerDay = destinations
                    trip.sharedWith = arrayListOf(user.email)
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


            Travel_AppTheme(systemTheme = systemTheme) {

                if (locationSelection) {
                    LocationSelection(this, this, onBack = {
                        locationSelection = false
                    },
                        onAddStep = {
                            if (it != null) {
                                val destinationsPerDay =
                                    destinations.clone() as ArrayList<ArrayList<TripDestination>>
                                val newDestination = (TripDestination)(it)
                                if (destinations[selectedDay].size > 0 && stepCursor > 0) {
                                    val oldDestination = (destinations[selectedDay])[stepCursor - 1]
                                    oldDestination.kmToNextDestination = 1f
                                    oldDestination.minutesToNextDestination = 1f
                                    oldDestination.mediumToNextDestination = MediumType.Foot
                                }
                                destinationsPerDay[selectedDay].add(stepCursor++, newDestination)
                                destinations = destinationsPerDay
                            }
                        },
                        onStartingPointSelected = {
                            if (it != null) {
                                startingPoint.value = it
                            }
                        })
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {

                        Column {
                            Row (Modifier.fillMaxWidth().graphicsLayer {
                                shape = RoundedCornerShape(bottomStart = cardRadius, bottomEnd = cardRadius)
                                clip = true
                            }.background(primaryColor).padding(top = cardPadding * 2)) {
                                TextField(
                                    value = name,
                                    onValueChange = {
                                        name = it
                                    },
                                    isError = confirmed && name.isEmpty(),
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    colors = TextFieldDefaults.textFieldColors(
                                        focusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        backgroundColor = Color.Transparent,
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
                            }
                            LazyColumn(
                                modifier = Modifier
                                    .background(colors.background)
                                    .padding(10.dp)
                                    .fillMaxSize(),
                                horizontalAlignment = CenterHorizontally
                            ) {
                                item {
                                    if (thumbnail.value != null) {
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
                                                imageModel = thumbnail.value,
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
                                                startActivityForResult(gallery, 1000)

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
                                            focusedIndicatorColor = Color.Transparent,
                                            disabledIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
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

                                    Heading("Tags")

                                    Tags(tags)

                                    Heading("Central location")

                                    if (startingPoint.value == null) {
                                        Button(
                                            onClick = { locationSelection = true },
                                            border = if (confirmed) BorderStroke(2.dp, danger) else null,
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
                                                imageModel = startingPoint.value?.thumbnailUrl,
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
                                                startingPoint.value?.name!!,
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
                                                TripStepCard(place, index,changeable = true)
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
                        FloatingActionButton(
                            onClick = {
                                if (thumbnail.value != null) {
                                    val bitmap = thumbnail.value
                                    val baos = ByteArrayOutputStream()
                                    bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                                    val data = baos.toByteArray()
                                    val storage = FirebaseStorage.getInstance()
                                    val storageRef = storage.reference
                                    val path = "images/${Date().time}.jpg"
                                    val mountainImagesRef = storageRef.child(path)

                                    var uploadTask = mountainImagesRef.putBytes(data)
                                    uploadTask.addOnFailureListener {
                                        upload()
                                    }.addOnSuccessListener { taskSnapshot ->
                                        storageRef.child(path).downloadUrl.addOnSuccessListener {
                                            upload(it.toString())
                                        }

                                    }
                                } else {
                                    upload()
                                }

                            },
                            backgroundColor = primaryColor,
                            modifier = Modifier
                                .align(
                                    BottomEnd
                                )
                                .padding(cardPadding)
                        ) {
                            FaIcon(
                                FaIcons.Save,
                                tint = White
                            )
                        }

                    }
                }
            }


        }

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 1000) {
            thumbnail.value = MediaStore.Images.Media.getBitmap(this.contentResolver, data?.data)
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
                            val tagss = tags.clone() as ArrayList<String>
                            tagss.remove(text)
                            tags = tagss
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
                    focusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    backgroundColor = Color.Transparent,
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







