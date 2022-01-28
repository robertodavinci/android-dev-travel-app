package com.apps.travel_app.ui.pages

import FaIcons
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.apps.travel_app.MainActivity
import com.apps.travel_app.R
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.GooglePlace
import com.apps.travel_app.ui.components.*
import com.apps.travel_app.ui.pages.viewmodels.GooglePlaceViewModel
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.ui.utils.rememberMapViewWithLifecycle
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MapStyleOptions
import com.guru.fontawesomecomposelib.FaIcon
import com.skydoves.landscapist.CircularReveal
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GooglePlaceScreen(
    navController: NavController,
    destination: Destination,
    mainActivity: MainActivity
) {

    val viewModel = remember { GooglePlaceViewModel(destination, mainActivity) }

    fun dayOfWeek(i: Int): String {
        return when (i) {
            0 -> "Mon"
            1 -> "Tue"
            2 -> "Wed"
            3 -> "Thu"
            4 -> "Fri"
            5 -> "Sat"
            6 -> "Sun"
            else -> ""
        }
    }

    fun mapInit() {
        viewModel.map!!.uiSettings.isZoomControlsEnabled = false

        viewModel.map?.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                mainActivity,
                mapStyle
            )
        )

        viewModel.map!!.uiSettings.isMapToolbarEnabled = false

        viewModel.map?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    destination.latitude,
                    destination.longitude
                ), 12f
            )
        )

        //viewModel.map?.setOnMarkerClickListener { marker -> markerClick(marker) }
    }


    val scrollState = rememberScrollState()
    val maxScroll = 100
    var percentage = scrollState.value.toFloat() / maxScroll
    percentage = if (percentage > 1) 1f else percentage

    var mapView: MapView? = null
    if (viewModel.loadingScreen > 5)
        mapView = rememberMapViewWithLifecycle()

    BoxWithConstraints {
        Box(
            modifier = Modifier
                .background(colors.background)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Column {

                Box(
                    modifier = Modifier.heightIn(0.dp, 250.dp)
                ) {
                    MainCard(
                        destination = destination,
                        rating = 3.5f,
                        views = 206,
                        mainActivity = mainActivity,
                        padding = (cardPadding * (1 - percentage)),
                        radius = (cardRadius * (1 - percentage)),
                        icon = FaIcons.Google,
                        clickable = false
                    )
                }

                viewModel.googlePlace?.let { PhotosRow(it.photos) }

                FlexibleRow(
                    alignment = CenterHorizontally,
                    modifier = Modifier
                        .padding(cardPadding / 2)
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = { }, modifier = Modifier
                            .padding(5.dp)
                    ) {
                        Row {
                            FaIcon(
                                FaIcons.AddressCardRegular,
                                tint = colors.surface,
                                modifier = Modifier.align(CenterVertically)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                viewModel.googlePlace?.address ?: "",
                                color = colors.surface,
                                modifier = Modifier.align(CenterVertically)
                            )
                        }
                    }
                    Button(
                        onClick = { }, modifier = Modifier
                            .padding(5.dp)
                    ) {
                        Row {
                            FaIcon(
                                FaIcons.PhoneAlt,
                                tint = colors.surface
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(viewModel.googlePlace?.phoneNumber ?: "", color = colors.surface)
                        }
                    }
                    Button(
                        onClick = { viewModel.openMap = true },
                        modifier = Modifier.padding(5.dp)
                    ) {
                        FaIcon(
                            FaIcons.LocationArrow,
                            tint = colors.surface
                        )
                    }
                    Button(onClick = { }, modifier = Modifier.padding(5.dp)) {
                        Row {
                            for (i in 1..(viewModel.googlePlace?.priceLevel?.toInt() ?: 1)) {
                                FaIcon(
                                    FaIcons.EuroSign,
                                    tint = colors.surface
                                )
                            }
                        }
                    }
                }

                FlexibleRow(
                    alignment = CenterHorizontally,
                    modifier = Modifier
                        .padding(cardPadding / 2)
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = { }, modifier = Modifier
                            .padding(5.dp),

                        background = if (viewModel.googlePlace?.isOpen == true) success else danger
                    ) {
                        Text(
                            if (viewModel.googlePlace?.isOpen == true) stringResource(R.string.open) else stringResource(R.string.closed),
                            color = Color.White
                        )
                    }
                    viewModel.googlePlace?.openingHours?.forEach {
                        val day = it.open.dayOfWeek
                        Button(onClick = { }, modifier = Modifier.padding(5.dp)) {
                            Row {
                                Text(
                                    dayOfWeek(day) + " ",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    it.open.hour + " - "
                                )
                                if (day != it.close.dayOfWeek) {
                                    Text(
                                        dayOfWeek(it.close.dayOfWeek),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    it.close.hour
                                )
                            }
                        }
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
                        verticalAlignment = CenterVertically,
                        modifier = Modifier.padding(cardPadding)
                    ) {
                        Column(
                            horizontalAlignment = CenterHorizontally,
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

                Heading(stringResource(R.string.todo))
                Subheading(stringResource(R.string.do_description))

                AttractionsRow(viewModel.todo, mainActivity)

                Heading(stringResource(R.string.eat))
                Subheading(stringResource(R.string.eat_description))

                AttractionsRow(viewModel.eat, mainActivity)

                Heading(stringResource(R.string.stay))
                Subheading(stringResource(R.string.stay_description))

                AttractionsRow(viewModel.stay, mainActivity)

                Heading(
                    stringResource(R.string.top_ratings)
                )

                Box(
                    modifier = Modifier.padding(bottom = 60.dp)
                ) {
                    if (viewModel.googlePlace?.reviews?.isEmpty() == true) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .alpha(0.5f)
                                .padding(50.dp)
                        ) {
                            Loader()
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .padding(cardPadding)
                        ) {

                            viewModel.googlePlace?.reviews?.forEach { rating ->
                                RatingCard(
                                    rating
                                )
                            }


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
    val scale: Float by animateFloatAsState(
        if (viewModel.openMap) 1f else 0f, animationSpec = tween(
            durationMillis = 500,
            easing = LinearOutSlowInEasing
        )
    )
    if (viewModel.openMap) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = {
                viewModel.openMap = false
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
                                if (!viewModel.mapLoaded) {
                                    mapView.getMapAsync { mMap ->
                                        if (!viewModel.mapLoaded) {
                                            viewModel.map = mMap
                                            mapInit()
                                            viewModel.mapLoaded = true
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
                        viewModel.loadingScreen++
                    }
                }
            }
        }
    }


}


@Composable
private fun PhotosRow(attractions: List<String>) {
    var selectedImage by remember { mutableStateOf("") }
    LazyRow(
        modifier = Modifier.padding(cardPadding)
    ) {
        items(attractions.size) { i ->
            val image = attractions[i]
            GlideImage(
                imageModel = image,
                modifier = Modifier
                    .size(100.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                selectedImage = image
                            }
                        )

                    }
                    .padding(10.dp)
                    .graphicsLayer {
                        shape = RoundedCornerShape(20)
                        clip = true
                    }
            )

        }
    }

    val scale: Float by animateFloatAsState(
        if (selectedImage.isNotEmpty()) 1f else 0f, animationSpec = tween(
            durationMillis = 500,
            easing = LinearOutSlowInEasing
        )
    )
    if (selectedImage.isNotEmpty()) {

        androidx.compose.ui.window.Dialog(
            onDismissRequest = {
                selectedImage = String()
            },

            ) {
            Column(
                modifier = Modifier
                    .scale(scale)
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
                    GlideImage(
                        imageModel = selectedImage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        circularReveal = CircularReveal(duration = 700),

                        )

                }
            }
        }
    }

}

@Composable
private fun AttractionsRow(attractions: ArrayList<GooglePlace>, activity: MainActivity) {
    LazyRow(
        modifier = Modifier.padding(cardPadding)
    ) {
        items(attractions.size) { i ->
            val attraction = attractions[i]

            MainCard(
                destination = attraction,
                rating = attraction.rating,
                padding = 5.dp,
                shadow = 10.dp,
                imageMaxHeight = 200f,
                mainActivity = activity,
                infoScale = 0.8f,
                icon = FaIcons.Google,
                isGooglePlace = true
            )

        }
    }

}






