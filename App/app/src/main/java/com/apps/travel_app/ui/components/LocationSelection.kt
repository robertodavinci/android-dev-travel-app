package com.apps.travel_app.ui.pages

import FaIcons
import android.app.Activity
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.apps.travel_app.models.Destination
import com.apps.travel_app.ui.pages.viewmodels.LocationSelectionViewModel
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.ui.utils.*
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.model.MapStyleOptions
import com.guru.fontawesomecomposelib.FaIcon
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterialApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.ui.ExperimentalComposeUiApi::class
)
@Composable
fun LocationSelection(
    context: Context,
    activity: Activity,
    onBack: () -> Unit,
    onAddStep: (Destination?) -> Unit,
    onMainDestinationSelected: (Destination?) -> Unit
) {

    val viewModel = remember { LocationSelectionViewModel(activity) }
    
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


    viewModel.mapView = rememberMapViewWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {

        //if (viewModel.mapView != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .wrapContentSize(Alignment.Center)
        ) {
            AndroidView({ viewModel.mapView!! }) { mapView ->
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
        //}

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .height(200.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colors.background,
                            Transparent
                        )
                    )
                )
        )
        val keyboardController = LocalSoftwareKeyboardController.current
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(vertical = cardPadding * 2, horizontal = cardPadding)
        ) {
            Row {
                IconButton(onClick = { onBack() }) {
                    FaIcon(FaIcons.ArrowLeft, tint = colors.surface)
                }
                TextField(
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onDone = {viewModel.search(viewModel.searchTerm);keyboardController?.hide()}),
                    value = viewModel.searchTerm, onValueChange = { viewModel.searchTerm = it },
                    shape = RoundedCornerShape(cardRadius),
                    modifier = Modifier
                        .weight(1f),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Transparent,
                        disabledIndicatorColor = Transparent,
                        unfocusedIndicatorColor = Transparent,
                        backgroundColor = colors.onBackground,
                    ),
                    placeholder = {
                        Text(
                            "Search",
                            color = colors.surface,
                            fontSize = textNormal,
                            modifier = Modifier.alpha(0.5f)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            viewModel.search(
                                viewModel.searchTerm
                            )
                            keyboardController?.hide()
                        }) {
                            FaIcon(FaIcons.Search, tint = colors.surface)
                        }
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = colors.surface,
                        fontWeight = FontWeight.Bold
                    ),
                )
            }
            Row {

            }
        }

        if (viewModel.currentDestination != null) {
            Column(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Card(
                    elevation = cardElevation,
                    backgroundColor = colors.onBackground,
                    shape = RoundedCornerShape(cardRadius),
                    modifier = Modifier
                        .heightIn(0.dp, 100.dp)
                        .wrapContentSize()
                        .padding(cardPadding),
                    onClick = {

                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        GlideImage(
                            imageModel = viewModel.currentDestination?.thumbnailUrl,
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxHeight()
                                .widthIn(0.dp, 100.dp)
                                .align(Alignment.CenterVertically)
                        )

                        Column(
                            modifier = Modifier.padding(cardPadding)
                        ) {
                            Text(
                                text = viewModel.currentDestination?.name ?: "",
                                color = colors.surface,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Start,
                                fontSize = textNormal,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Start)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(cardPadding),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    com.apps.travel_app.ui.components.Button(onClick = {
                        onMainDestinationSelected(viewModel.currentDestination)
                        viewModel.mainDestinationSelected = true
                    },background = if (viewModel.mainDestinationSelected) success else primaryColor) {
                        Text("Set as starting point", fontSize = textNormal, color = White)
                    }
                    Spacer(modifier = Modifier.padding(5.dp))
                    com.apps.travel_app.ui.components.Button(onClick = {
                        onAddStep(viewModel.currentDestination)
                        viewModel.stepAdded = true
                    },background = if (viewModel.stepAdded) success else primaryColor) {
                        Text("Add as step", color = White, fontSize = textNormal)
                    }
                }
            }
        }

    }

}



