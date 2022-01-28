package com.apps.travel_app.ui.pages

import FaIcons
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.apps.travel_app.MainActivity
import com.apps.travel_app.R
import com.apps.travel_app.ui.components.Button
import com.apps.travel_app.ui.components.DestinationCard
import com.apps.travel_app.ui.pages.viewmodels.MapViewModel
import com.apps.travel_app.ui.theme.cardPadding
import com.apps.travel_app.ui.theme.mapStyle
import com.apps.travel_app.ui.theme.primaryColor
import com.apps.travel_app.ui.theme.textHeading
import com.apps.travel_app.ui.utils.rememberMapViewWithLifecycle
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.MapStyleOptions
import com.guru.fontawesomecomposelib.FaIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MapScreen {

    @Composable
    fun MapScreen(context: Context, activity: MainActivity) {

        val viewModel = remember { MapViewModel() }

        fun mapInit(context: Context) {
            viewModel.map!!.uiSettings.isZoomControlsEnabled = false

            viewModel.map?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    mapStyle
                )
            )

            viewModel.map!!.uiSettings.isMapToolbarEnabled = false

            viewModel.map?.moveCamera(CameraUpdateFactory.newLatLngZoom(viewModel.center, 6f))

            viewModel.map?.setOnMarkerClickListener { marker -> viewModel.markerClick(marker) }
        }


        var mapView: MapView? = null
        if (viewModel.loadingScreen > 5)
            mapView = rememberMapViewWithLifecycle()

        Box(modifier = Modifier.fillMaxSize()) {

            if (mapView != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(White)
                        .wrapContentSize(Alignment.Center)
                ) {
                    AndroidView({ mapView }) { mapView ->
                        CoroutineScope(Dispatchers.Main).launch {
                            if (!viewModel.mapLoaded) {
                                mapView.getMapAsync { mMap ->
                                    if (!viewModel.mapLoaded) {
                                        viewModel.map = mMap
                                        mapInit(context)
                                        viewModel.mapLoaded = true
                                    }
                                }
                            }
                        }
                    }

                }
            }

            Text(
                text = "\uD83C\uDF0D Spin & pin",
                color = colors.surface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = textHeading,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(cardPadding * 2)
            )
            if (viewModel.drawingEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.TopCenter)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { position ->
                                    viewModel.mapDrawingReset(position)
                                },
                                onDrag = { event, _ ->
                                    viewModel.mapDrawing(
                                        event,
                                        viewModel.polygonOpt
                                    )
                                },
                                onDragEnd = {
                                    viewModel.populateMapDrawing(activity)
                                    viewModel.toggleDrawing()
                                }
                            )
                        }
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 60.dp)
                    .fillMaxWidth()
            ) {

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Button(
                        background = colors.background,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(cardPadding),
                        onClick = {
                            viewModel.toggleDrawing()
                        }) {
                        FaIcon(
                            faIcon = FaIcons.HandPointUpRegular,
                            tint = if (viewModel.drawingEnabled) primaryColor else colors.surface
                        )
                    }

                    Button(
                        background = colors.background,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(cardPadding),
                        onClick = {
                            viewModel.switchTo3D()
                        }) {
                        FaIcon(
                            faIcon = FaIcons.BuildingRegular,
                            tint = colors.surface
                        )
                    }
                }

                DestinationCard(
                    destination = viewModel.currentDestination,
                    open = viewModel.destinationSelected && !viewModel.drawingEnabled,
                    activity = activity
                )

            }

            if (!viewModel.mapLoaded) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(White)
                        .wrapContentSize(Alignment.Center)
                ) {
                    Text(
                        text = stringResource(R.string.loading),
                        fontWeight = FontWeight.Bold,
                        color = colors.surface,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        textAlign = TextAlign.Center,
                        fontSize = textHeading
                    )
                    viewModel.loadingScreen++
                }
            }
        }
    }
}
