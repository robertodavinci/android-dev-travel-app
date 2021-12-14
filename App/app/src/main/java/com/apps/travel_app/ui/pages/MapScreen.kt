package com.apps.travel_app.ui.pages

import FaIcons
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.apps.travel_app.MainActivity
import com.apps.travel_app.models.Destination
import com.apps.travel_app.ui.components.DestinationCard
import androidx.compose.material.MaterialTheme
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.ui.utils.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.guru.fontawesomecomposelib.FaIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MapScreen {
    val destinations = HashMap<Int, Destination>()
    @Composable
    fun MapScreen(context: Context, activity: MainActivity) {
        val center = LatLng(44.0, 10.0)
        var polygonOpt = PolygonOptions()
        val currentDestination: MutableState<Destination?> = remember { mutableStateOf(null) }
        val map: MutableState<GoogleMap?> = remember { mutableStateOf(null) }
        val mapLoaded = remember { mutableStateOf(false) }
        val destinationSelected = remember { mutableStateOf(false) }
        val drawingEnabled = remember { mutableStateOf(false) }
        val loadingScreen = remember { mutableStateOf(0) }

        fun switchTo3D() {
            if (map.value != null) {
                val cameraPosition: CameraPosition = CameraPosition.Builder()
                    .target(map.value!!.cameraPosition.target)
                    .tilt(if (map.value!!.cameraPosition.tilt > 0f) 30f else 0f)
                    .build()
                map.value?.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                        cameraPosition
                    )
                )
            }
        }

        fun toggleDrawing() {
            destinationSelected.value = false
            drawingEnabled.value = !drawingEnabled.value
            map.value?.uiSettings?.isScrollGesturesEnabled = !drawingEnabled.value
        }

        fun populateMapDrawing(activity: MainActivity) {
            if (polygonOpt.points.size <= 0)
                return


            val points = line(polygonOpt.points)
            val polygonOpt2 = PolygonOptions()
                .strokeColor(Color.parseColor("#FF808ea7"))
                .fillColor(Color.parseColor("#88808ea7"))
            for (point in points) {
                map.value?.clear()
                polygonOpt2.add(point)
                map.value?.addPolygon(polygonOpt2)
            }

            points.add(points[0])
            val request = points.joinToString(",", "[", "]") { e ->
                "[${e.latitude},${e.longitude}]"
            }

            Thread {
                val citiesText = sendPostRequest(request, action = "polygonCities")
                val gson = Gson()
                val itemType = object : TypeToken<List<Destination>>() {}.type
                val cities: List<Destination> = gson.fromJson(citiesText, itemType)
                for (city in cities) {

                    val downloadedImage = getBitmapFromURL(city.thumbnailUrl)
                    var thumbnail: Bitmap? = null
                    if (downloadedImage != null) {
                        val baseImage =
                            cropToSquare(downloadedImage)
                        thumbnail =
                            getCroppedBitmap(baseImage, 100, 100, 5f)

                        city.thumbnail = baseImage.asImageBitmap()
                    }
                    val markerOptions = MarkerOptions()
                        .position(
                            LatLng(
                                city.latitude,
                                city.longitude
                            )
                        )
                        .title(city.name)
                        .zIndex(5f)
                    if (thumbnail != null)
                        markerOptions.icon(
                            BitmapDescriptorFactory.fromBitmap(
                                thumbnail
                            )
                        )
                    activity.runOnUiThread {
                        val marker = map.value!!.addMarker(markerOptions)
                        destinations[marker.hashCode()] = city
                        markerPopUp(marker)
                    }
                }
            }.start()

        }

        fun mapDrawingReset(position: Offset) {
            map.value?.clear()
            destinations.clear()
            polygonOpt = PolygonOptions()
            polygonOpt.add(screenCoordinatesToLatLng(position, map.value))
            polygonOpt
                .strokeColor(Color.parseColor("#FF808ea7"))
                .fillColor(Color.parseColor("#88808ea7"))
            map.value?.addPolygon(polygonOpt)
        }

        fun mapDrawing(motionEvent: PointerInputChange, polygonOpt: PolygonOptions) {

            val latLng = screenCoordinatesToLatLng(motionEvent.position, map.value) ?: return

            if (!polygonOpt.points.none { point -> point.equals(latLng) })
                return

            map.value?.clear()
            polygonOpt.add(latLng)

            map.value?.addPolygon(polygonOpt)
        }

        fun markerClick(marker: Marker): Boolean {
            val destination = destinations[marker.hashCode()]
            if (destination != null) {
                currentDestination.value = destination
                destinationSelected.value = true
                return true
            }
            destinationSelected.value = false
            return false
        }


        fun mapInit(context: Context) {
            map.value!!.uiSettings.isZoomControlsEnabled = false

            map.value?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    mapStyle
                )
            )

            map.value!!.uiSettings.isMapToolbarEnabled = false

            map.value?.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 6f))

            map.value?.setOnMarkerClickListener { marker -> markerClick(marker) }
        }


        val systemUiController = rememberSystemUiController()
        systemUiController.setSystemBarsColor(
            color = textLightColor
        )

        var mapView: MapView? = null
        if (loadingScreen.value > 5)
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
                            if (!mapLoaded.value) {
                                mapView.getMapAsync { mMap ->
                                    if (!mapLoaded.value) {
                                        map.value = mMap
                                        mapInit(context)
                                        mapLoaded.value = true
                                    }
                                }
                            }
                        }
                    }

                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .height(200.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                textLightColor,
                                Transparent
                            )
                        )
                    )
            )

            Text(
                text = "\uD83C\uDF0D Spin & pin",
                color = White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = textHeading,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(cardPadding)
            )
            if (drawingEnabled.value) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.TopCenter)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { position ->
                                    mapDrawingReset(position)
                                },
                                onDrag = { event, _ ->
                                    mapDrawing(
                                        event,
                                        polygonOpt
                                    )
                                },
                                onDragEnd = {
                                    populateMapDrawing(activity)
                                    toggleDrawing()
                                }
                            )
                        }
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 150.dp)
                    .fillMaxWidth()
            ) {

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    IconButton(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(cardPadding)
                            .width(30.dp)
                            .height(30.dp),
                        onClick = {
                            toggleDrawing()
                        }) {
                        FaIcon(
                            faIcon = FaIcons.HandPointUpRegular,
                            tint = if (drawingEnabled.value) MaterialTheme.colors.surface else iconLightColor
                        )
                    }

                    IconButton(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(cardPadding)
                            .width(30.dp)
                            .height(30.dp),
                        onClick = {
                            switchTo3D()
                        }) {
                        FaIcon(
                            faIcon = FaIcons.BuildingRegular,
                            tint = MaterialTheme.colors.surface
                        )
                    }
                }

                DestinationCard(
                    destination = currentDestination.value,
                    open = destinationSelected.value && !drawingEnabled.value
                )

            }

            if (!mapLoaded.value) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(White)
                        .wrapContentSize(Alignment.Center)
                ) {
                    Text(
                        text = "Loading...",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.surface,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        textAlign = TextAlign.Center,
                        fontSize = textHeading
                    )
                    loadingScreen.value++
                }
            }
        }
    }
}
