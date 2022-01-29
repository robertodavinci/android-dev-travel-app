package com.apps.travel_app.ui.pages

import FaIcons
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.apps.travel_app.R
import com.apps.travel_app.models.MediumType
import com.apps.travel_app.models.TripDestination
import com.apps.travel_app.ui.components.*
import com.apps.travel_app.ui.pages.viewmodels.TripCreationViewModel
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.user
import com.guru.fontawesomecomposelib.FaIcon
import com.skydoves.landscapist.glide.GlideImage
import java.util.*



class TripCreationActivity : ComponentActivity() {
    
    lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private val permissionStorage = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    lateinit var viewModel: TripCreationViewModel

    override fun onBackPressed(){
        if (viewModel.locationSelection) viewModel.locationSelection = false
        else finish()
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
        viewModel =
            TripCreationViewModel(this, tripId)


        resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                viewModel.galleryImageSelected(result)            }
        }

        super.onCreate(savedInstanceState)

        requireFullscreenMode(window, this)

        setContent {

            Travel_AppTheme(systemTheme = systemTheme) {

                if (viewModel.locationSelection) {
                    LocationSelection(this, this, onBack = {
                        viewModel.locationSelection = false
                    },
                        onAddStep = {
                            if (it != null) {
                                viewModel.addStep(it)
                            }
                        },
                        onMainDestinationSelected = {
                            if (it != null) {
                                viewModel.mainDestination = it
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
                                    value = viewModel.name,
                                    onValueChange = {
                                        viewModel.name = it
                                    },
                                    isError = viewModel.confirmed && viewModel.name.isEmpty(),
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
                                            stringResource(R.string.trip_name),
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
                                    viewModel.save()
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
                                    if (viewModel.thumbnail != null || viewModel.thumbnailUrl != null) {
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
                                                imageModel = viewModel.thumbnail ?: viewModel.thumbnailUrl,
                                                contentDescription = "",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .heightIn(0.dp, 100.dp)
                                            )

                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(5.dp))
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
                                                    stringResource(R.string.add_thumbnail),
                                                    color = colors.surface,
                                                    fontSize = textNormal
                                                    )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Heading(stringResource(R.string.description))

                                    TextField(
                                        value = viewModel.description,
                                        onValueChange = {
                                            viewModel.description = it
                                        },
                                        isError = viewModel.confirmed && viewModel.description.isEmpty(),
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
                                                stringResource(R.string.description),
                                                fontSize = textNormal,
                                                color = colors.surface,
                                                modifier = Modifier
                                                    .alpha(0.5f)
                                                    .fillMaxWidth(),
                                                textAlign = TextAlign.Center
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                                        textStyle = TextStyle(
                                            color = colors.surface,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        ),
                                    )

                                    Heading(stringResource(R.string.visibility))

                                    Row {
                                        Button (
                                            background = if(viewModel.sharedWith.size == 0) primaryColor else colors.onBackground,
                                            onClick = {viewModel.sharedWith = arrayListOf()}
                                                ) {
                                            FaIcon(
                                                FaIcons.Globe,
                                                tint = if(viewModel.sharedWith.size == 0) White else colors.surface
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Button (
                                            background = if(viewModel.sharedWith.size > 0) primaryColor else colors.onBackground,
                                            onClick = {viewModel.sharedWith = arrayListOf(user.email)}
                                        ) {
                                            FaIcon(
                                                FaIcons.Lock,
                                                tint = if(viewModel.sharedWith.size > 0) White else colors.surface
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Heading(stringResource(R.string.tags))

                                    Tags(viewModel.tags)

                                    Heading(stringResource(R.string.main_destination))

                                    if (viewModel.mainDestination == null) {
                                        Button(
                                            onClick = { viewModel.locationSelection = true },
                                            border = if (viewModel.confirmed) BorderStroke(
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
                                                    stringResource(R.string.main_destination),
                                                    color = colors.surface,
                                                    fontSize = textNormal
                                                )
                                            }
                                        }
                                    } else {
                                        Row (){
                                            GlideImage(
                                                imageModel = viewModel.mainDestination?.thumbnailUrl,
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
                                                viewModel.mainDestination?.name!!,
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

                                    Heading(stringResource(R.string.days), modifier = Modifier.padding(cardPadding))

                                    LazyRow(
                                        horizontalArrangement = Arrangement.SpaceAround,
                                    ) {
                                        items(viewModel.days) { i ->
                                            val background =
                                                if (i == viewModel.selectedDay) primaryColor else colors.onBackground
                                            val foreground =
                                                if (i == viewModel.selectedDay) White else colors.surface
                                            Button(
                                                onClick = { viewModel.selectedDay = i; viewModel.stepCursor = 0 },
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
                                                        stringResource(R.string.day),
                                                        color = foreground,
                                                        fontSize = textSmall
                                                    )
                                                }
                                            }
                                        }
                                        item {
                                            Button(
                                                onClick = {
                                                    viewModel.days++
                                                    val destinationsPerDay =
                                                        viewModel.destinations.clone() as ArrayList<ArrayList<TripDestination>>
                                                    destinationsPerDay.add(ArrayList())
                                                    viewModel.destinations = destinationsPerDay

                                                    viewModel.selectedDay = viewModel.days - 1
                                                    viewModel.stepCursor = 0
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
                                                        stringResource(R.string.day),
                                                        color = colors.surface,
                                                        fontSize = textSmall
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Heading(stringResource(R.string.steps), modifier = Modifier.padding(cardPadding))

                                    if (viewModel.destinations[viewModel.selectedDay].size <= 0) {
                                        Button(
                                            onClick = { viewModel.locationSelection = true },
                                        ) {
                                            Row {
                                                FaIcon(
                                                    FaIcons.MapMarkerAlt,
                                                    tint = colors.surface
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    stringResource(R.string.first_step),
                                                    color = colors.surface

                                                )
                                            }
                                        }
                                    } else {
                                        viewModel.destinations[viewModel.selectedDay].forEachIndexed { index, place ->
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
                                                if (index < viewModel.destinations[viewModel.selectedDay].size - 1) {
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
                                                                    "${place.minutesToNextDestination.toInt()} ${stringResource(R.string.minutes)} (${place.kmToNextDestination} km)",
                                                                    color = colors.surface,
                                                                    fontSize = textSmall,
                                                                    modifier = Modifier
                                                                        .padding(start = 20.dp)
                                                                        .align(CenterVertically)
                                                                )
                                                            }
                                                            IconButton(
                                                                onClick = {
                                                                    viewModel.stepCursor = index + 1
                                                                    viewModel.locationSelection = true
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
                                                viewModel.stepCursor = viewModel.destinations[viewModel.selectedDay].size
                                                viewModel.locationSelection = true
                                            },
                                        ) {
                                            Row {
                                                FaIcon(
                                                    FaIcons.Hiking,
                                                    tint = colors.surface
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    stringResource(R.string.add_step),
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
                if (viewModel.loading) {
                    val color = colors.background.copy(alpha = 0.8f)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color),
                        contentAlignment = Center

                    ) {
                        Loader()
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
                        stringResource(R.string.tags_comma),
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







