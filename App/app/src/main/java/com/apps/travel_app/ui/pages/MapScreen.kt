package com.apps.travel_app.ui.pages

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.apps.travel_app.R
import com.apps.travel_app.ui.theme.cardPadding
import com.apps.travel_app.ui.theme.textHeading
import com.apps.travel_app.ui.theme.textLightColor
import com.apps.travel_app.ui.utils.rememberMapViewWithLifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MapStyleOptions
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.android.libraries.maps.model.PolylineOptions
import com.google.maps.android.ktx.awaitMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun MapScreen(context: Context) {

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = textLightColor
    )

    val mapView = rememberMapViewWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .wrapContentSize(Alignment.Center)
        ) {
            AndroidView({ mapView }) { mapView ->
                CoroutineScope(Dispatchers.Main).launch {
                    val map = mapView.awaitMap()
                    map.uiSettings.isZoomControlsEnabled = false

                    map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.mapstyle))

                    map.uiSettings.isMapToolbarEnabled = false;

                    val pickUp = LatLng(45.0, 11.0)
                    val destination = LatLng(44.0, 10.0)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 6f))
                    val markerOptions = MarkerOptions()
                        .title("Sydney Opera House")
                        .position(pickUp)
                    map.addMarker(markerOptions)

                    val markerOptionsDestination = MarkerOptions()
                        .title("Restaurant Hubert")
                        .position(destination)
                    map.addMarker(markerOptionsDestination)

                    map.addPolyline(
                        PolylineOptions().add(pickUp, destination)
                    )

                }


            }

        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .height(200.dp)
                .background(brush = Brush.verticalGradient(
                    colors = listOf(
                        textLightColor,
                        Color.Transparent
                    )
                ))
        )

        Text(
            text = "Spin & pin",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            fontSize = textHeading,
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(cardPadding)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    //MapScreen()
}