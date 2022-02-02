package com.apps.travel_app.ui.pages
/**
 * Composable function that is shown when one of the locations is selected.
 * It features all of the details about that location, and if it's a Google location,
 * fetches additional places and destinations connected to it via Google Places.
 * Fetches and displays all of the trips in which a certain location has been used.
 * Directly connected to the LocationViewModel.
 */
// Vincenzo Manto + Robert Medvedec
import FaIcons
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.room.Room
import com.apps.travel_app.MainActivity
import com.apps.travel_app.R
import com.apps.travel_app.data.room.db.AppDatabase
import com.apps.travel_app.models.Destination
import com.apps.travel_app.ui.components.*
import com.apps.travel_app.ui.pages.viewmodels.LocationViewModel
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.ui.utils.isOnline
import com.apps.travel_app.ui.utils.rememberMapViewWithLifecycle
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MapStyleOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.guru.fontawesomecomposelib.FaIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocationScreen(
    navController: NavController,
    destination: Destination,
    mainActivity: MainActivity
) {

    val db = Room.databaseBuilder(
        mainActivity,
        AppDatabase::class.java, AppDatabase.NAME
    ).build()

    val viewModel = remember { LocationViewModel(destination, db, mainActivity) }

    fun mapInit() {
        viewModel.map.value!!.uiSettings.isZoomControlsEnabled = false

        viewModel.map.value?.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                mainActivity,
                mapStyle
            )
        )

        viewModel.map.value!!.uiSettings.isMapToolbarEnabled = false

        viewModel.map.value?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    destination.latitude,
                    destination.longitude
                ), 12f
            )
        )
    }

    val activities = arrayListOf(destination.type)

    val scrollState = rememberScrollState()
    val maxScroll = 100
    var percentage = scrollState.value.toFloat() / maxScroll
    percentage = if (percentage > 1) 1f else percentage

    var mapView: MapView? = null
    if (viewModel.loadingScreen.value > 5)
        mapView = rememberMapViewWithLifecycle()

    BoxWithConstraints {
        Box(
            modifier = Modifier
                .background(colors.background)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = cardPadding * 2)
        ) {
            Column {

                Box(
                    modifier = Modifier.heightIn(0.dp, 250.dp)
                ) {
                    MainCard(
                        destination = destination,
                        rating = 3.5f,
                        clickable = false,
                        views = 206,
                        mainActivity = mainActivity,
                        padding = (cardPadding * (1 - percentage)),
                        radius = (cardRadius * (1 - percentage)),
                        depthCards = true
                    )
                }

                Text(
                    destination.description,
                    color = colors.surface,
                    fontSize = textSmall,
                    modifier = Modifier.padding(cardPadding)
                )


                FlexibleRow(
                    alignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(cardPadding / 2)
                        .fillMaxWidth()
                ) {

                    Button(onClick = { viewModel.openMap.value = true }, modifier = Modifier.padding(5.dp)) {
                        FaIcon(
                            FaIcons.LocationArrow,
                            tint = colors.surface
                        )
                    }
                    Button(onClick = {
                        Thread {
                            try {
                                if (!viewModel.isSaved.value) {
                                    val location = destination.toLocation()
                                    location.saved = true
                                    db.locationDao().insertAll(location)

                                } else {
                                    val location = destination.toLocation()
                                    location.saved = false
                                    db.locationDao().delete(location)
                                }
                                viewModel.isSaved.value = !viewModel.isSaved.value
                                if (viewModel.isSaved.value) {
                                    FirebaseMessaging.getInstance().subscribeToTopic("city" + destination.id)
                                } else {
                                    FirebaseMessaging.getInstance().unsubscribeFromTopic("city" + destination.id)
                                }
                            } catch (e: Exception) {

                            }
                        }.start()
                    }, modifier = Modifier.padding(5.dp)) {
                        FaIcon(
                            if (viewModel.isSaved.value) FaIcons.Heart else FaIcons.HeartRegular,
                            tint = if (viewModel.isSaved.value) danger else colors.surface
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .padding(cardPadding)
                        .fillMaxWidth(),
                    backgroundColor = colors.onBackground,
                    elevation = cardElevation,
                    shape = RoundedCornerShape(cardRadius)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(cardPadding)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(cardPadding)
                        ) {
                            FaIcon(
                                FaIcons.Viruses,
                                tint = primaryLightColor,
                                size = 60.dp
                            )
                            Text(
                                stringResource(R.string.covid19),
                                fontSize = textNormal,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                fontWeight = FontWeight.Bold,
                                color = colors.surface
                            )
                        }
                        Text(
                            stringResource(R.string.covid19_plus)
                            ,
                            fontSize = textSmall,
                            color = colors.surface,
                            modifier = Modifier.weight(1f)
                        )

                    }
                }


                if (!viewModel.isSaved.value && !isOnline(mainActivity)) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Center) {
                        NetworkError()
                    }
                } else if(isOnline(mainActivity)) {
                    Heading(stringResource(R.string.todo))
                    Subheading(stringResource(R.string.do_description))

                    AttractionsRow(viewModel.todo, mainActivity)

                    Heading(stringResource(R.string.eat))
                    Subheading(stringResource(R.string.eat_description))

                    AttractionsRow(viewModel.eat, mainActivity)

                    Heading(stringResource(R.string.stay))
                    Subheading(stringResource(R.string.stay_description))

                    AttractionsRow(viewModel.stay, mainActivity)

                    if (viewModel.ratings.value.size > 0) {
                        Heading(
                            stringResource(R.string.ratings)
                        )
                    }

                    Box(
                        modifier = Modifier.padding(bottom = 60.dp)
                    ) {

                        Column(
                            modifier = Modifier
                                .padding(cardPadding)
                        ) {

                            viewModel.ratings.value.forEach { rating ->
                                RatingCard(
                                    rating
                                )
                            }


                        }

                        Box(
                            modifier = Modifier
                                .padding(cardPadding)
                                .fillMaxWidth()
                                .height(30.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            colors.background,
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }
                }
            }
        }

    }
    val scale: Float by animateFloatAsState(
        if (viewModel.openMap.value) 1f else 0f, animationSpec = tween(
            durationMillis = 500,
            easing = LinearOutSlowInEasing
        )
    )
    if (viewModel.openMap.value) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = {
                viewModel.openMap.value = false
            },

            ) {
            Column(
                modifier = Modifier
                    .scale(scale)
                    .height(200.dp)
                    .padding(0.dp)
                    .graphicsLayer {
                        shape = RoundedCornerShape(cardRadius)
                        clip = true
                    }
                    .background(colors.background)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    if (mapView != null) {
                        AndroidView({ mapView }) { mapView ->
                            CoroutineScope(Dispatchers.Main).launch {
                                if (!viewModel.mapLoaded.value) {
                                    mapView.getMapAsync { mMap ->
                                        if (!viewModel.mapLoaded.value) {
                                            viewModel.map.value = mMap
                                            mapInit()
                                            viewModel.mapLoaded.value = true
                                        }
                                    }
                                }
                            }
                        }

                    } else {
                        Text(
                            stringResource(R.string.loading),
                            color = colors.surface,
                            modifier = Modifier.padding(
                                cardPadding
                            )
                        )
                        viewModel.loadingScreen.value++
                    }
                }
            }
        }
    }

}







