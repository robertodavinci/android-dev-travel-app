package com.apps.travel_app.ui.components
/**
 * Composable function that handles the location selection when adding a destination.
 */
import FaIcons
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.apps.travel_app.R
import com.apps.travel_app.models.Destination
import com.apps.travel_app.ui.pages.viewmodels.LocationSelectionViewModel
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.ui.utils.rememberMapViewWithLifecycle
import com.guru.fontawesomecomposelib.FaIcon
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(
    ExperimentalMaterialApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class,
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

    val width = convertPixelsToDp(Resources.getSystem().displayMetrics.widthPixels)

    val permissionStorage = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    val viewModel = remember {
        LocationSelectionViewModel(activity, {
            onAddStep(it)
        }, {
            onMainDestinationSelected(it)
        })
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
                                viewModel.mapInit(context)
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
                IconButton(modifier = Modifier.semantics{testTag = "back"},onClick = { onBack() }) {
                    FaIcon(FaIcons.ArrowLeft, tint = colors.surface)
                }
                TextField(
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search, capitalization = KeyboardCapitalization.Sentences),
                    keyboardActions = KeyboardActions(
                        onSearch = { viewModel.search(viewModel.searchTerm);keyboardController?.hide() }),

                    value = viewModel.searchTerm, onValueChange = { viewModel.searchTerm = it },
                    shape = RoundedCornerShape(cardRadius),
                    modifier = Modifier
                        .weight(1f).semantics { testTag = "searchText"},
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Transparent,
                        disabledIndicatorColor = Transparent,
                        unfocusedIndicatorColor = Transparent,
                        backgroundColor = colors.onBackground,
                    ),
                    placeholder = {
                        Text(
                            stringResource(R.string.search),
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
            Row(Modifier.align(CenterHorizontally).padding(cardPadding / 2), horizontalArrangement = SpaceBetween) {
                if (!viewModel.userIsAddingAPlace) {
                    Button(onClick = {
                        viewModel.userIsAddingAPlace = true
                    }, background = secondaryColor) {
                        Text(
                            stringResource(R.string.not_what_i_want),
                            color = White,
                            fontSize = textNormal
                        )
                    }
                } else {
                    Text(
                        stringResource(R.string.pin_place),
                        color = colors.surface,
                        fontSize = textNormal
                    )
                }
                /*Button(onClick = {
                    viewModel.search(viewModel.searchTerm);keyboardController?.hide()
                }, background = primaryColor) {
                    Text(
                        stringResource(R.string.search),
                        color = White,
                        fontSize = textNormal
                    )
                }*/
            }
        }


        val height: Float by animateFloatAsState(
            if (!viewModel.openCards) 0f else 500f, animationSpec = tween(
                durationMillis = 300,
                easing = LinearOutSlowInEasing
            )
        )

        Column(Modifier.align(Alignment.BottomCenter)) {
            if (viewModel.cities.isNotEmpty()) {
                Button (
                    onClick = {
                        viewModel.openCards = !viewModel.openCards
                    },
                    modifier = Modifier.align(End).padding(end = cardPadding)
                ) {
                    FaIcon(
                        if(viewModel.openCards) FaIcons.Times else FaIcons.ArrowUp, tint = colors.surface)
                }
            }
            ComposePagerSnapHelper(
                width = width.dp,
                snap = {
                    viewModel.moveTo(it)
                }
            ) { listState ->

                LazyRow(
                    state = listState,
                    modifier = Modifier.heightIn(0.dp, height.dp)
                ) {
                    items(viewModel.cities) { item ->
                        Card(
                            elevation = cardElevation,
                            backgroundColor = colors.background,
                            shape = RoundedCornerShape(cardRadius),
                            modifier = Modifier
                                .width(width.dp)
                                .padding(cardPadding)
                        ) {

                            Column(
                                modifier = Modifier.fillMaxWidth().padding(cardPadding)
                            ) {
                                if (!item.thumbnailUrl.isEmpty()) {
                                    GlideImage(
                                        imageModel = item.thumbnailUrl,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                            .padding(bottom = 5.dp)
                                            .graphicsLayer {
                                                shape = RoundedCornerShape(cardRadius)
                                                clip = true
                                            }
                                    )
                                }
                                Heading(item.name)
                                Text(
                                    text = item.description,
                                    color = colors.surface,
                                    textAlign = TextAlign.Start,
                                    fontSize = textNormal,
                                    lineHeight = textNormal / 2,
                                    modifier = Modifier
                                        .fillMaxWidth().padding(bottom = 5.dp),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = SpaceBetween
                                ) {
                                    Button(
                                        modifier = Modifier.semantics { testTag = "main" },
                                        onClick = {
                                            onMainDestinationSelected(item)
                                            viewModel.mainDestinationSelected = true
                                        },
                                        background = if (viewModel.mainDestinationSelected) success else colors.onSurface
                                    ) {
                                        Text(
                                            stringResource(R.string.set_main),
                                            fontSize = textNormal,
                                            color = White
                                        )
                                    }
                                    Button(
                                        modifier = Modifier.semantics { testTag = "add" },
                                        onClick = {
                                            onAddStep(item)
                                            viewModel.stepAdded = true
                                        },
                                        background = if (viewModel.stepAdded) success else colors.onSurface
                                    ) {
                                        Text(
                                            stringResource(R.string.add_step),
                                            color = White,
                                            fontSize = textNormal
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

    if (viewModel.userIsAddingAPlace && viewModel.addedMarker != null) {
        val permission =
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                activity,
                permissionStorage,
                1
            )
        }

        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == ComponentActivity.RESULT_OK && result.data != null) {
                    viewModel.galleryImageSelected(result)
                }

            }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = {
                viewModel.closeNewDestination()

            },

            ) {
            LazyColumn(
                modifier = Modifier
                    .padding(cardPadding)
                    .graphicsLayer {
                        shape = RoundedCornerShape(cardRadius)
                        clip = true
                    }
                    .background(colors.background)
                    .fillMaxWidth()
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = CenterHorizontally
                    ) {
                        Text(
                            stringResource(R.string.please_more_info),
                            color = colors.surface,
                            fontSize = textSmall
                        )
                        Heading(stringResource(R.string.name))
                        TextField(
                            value = viewModel.newDestinationName, onValueChange = { viewModel.newDestinationName = it },
                            shape = RoundedCornerShape(cardRadius),
                            colors = TextFieldDefaults.textFieldColors(
                                focusedIndicatorColor = Transparent,
                                disabledIndicatorColor = Transparent,
                                unfocusedIndicatorColor = Transparent,
                                backgroundColor = colors.onBackground,
                            ),
                            placeholder = {
                                Text(
                                    stringResource(R.string.name),
                                    color = colors.surface,
                                    fontSize = textNormal,
                                    modifier = Modifier.alpha(0.5f)
                                )
                            },
                            trailingIcon = {
                                FaIcon(FaIcons.InfoCircle, tint = colors.surface)
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = colors.surface,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Heading(stringResource(R.string.description))
                        TextField(
                            value = viewModel.newDestinationDesc, onValueChange = { viewModel.newDestinationDesc = it },
                            shape = RoundedCornerShape(cardRadius),
                            colors = TextFieldDefaults.textFieldColors(
                                focusedIndicatorColor = Transparent,
                                disabledIndicatorColor = Transparent,
                                unfocusedIndicatorColor = Transparent,
                                backgroundColor = colors.onBackground,
                            ),
                            placeholder = {
                                Text(
                                    stringResource(R.string.description),
                                    color = colors.surface,
                                    fontSize = textNormal,
                                    modifier = Modifier.alpha(0.5f)
                                )
                            },
                            trailingIcon = {
                                FaIcon(FaIcons.InfoCircle, tint = colors.surface)
                            },
                            singleLine = false,
                            textStyle = TextStyle(
                                color = colors.surface,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Heading(stringResource(R.string.type))
                        TextField(
                            value = viewModel.newDestinationType, onValueChange = { viewModel.newDestinationType = it },
                            shape = RoundedCornerShape(cardRadius),
                            colors = TextFieldDefaults.textFieldColors(
                                focusedIndicatorColor = Transparent,
                                disabledIndicatorColor = Transparent,
                                unfocusedIndicatorColor = Transparent,
                                backgroundColor = colors.onBackground,
                            ),
                            placeholder = {
                                Text(
                                    stringResource(R.string.type),
                                    color = colors.surface,
                                    fontSize = textNormal,
                                    modifier = Modifier.alpha(0.5f)
                                )
                            },
                            trailingIcon = {
                                FaIcon(FaIcons.QuestionCircle, tint = colors.surface)
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = colors.surface,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        if (viewModel.newDestinationThumbnail != null) {
                            Card(
                                elevation = cardElevation,
                                backgroundColor = colors.onBackground,
                                shape = RoundedCornerShape(cardRadius),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                            ) {
                                GlideImage(
                                    imageModel = viewModel.newDestinationThumbnail,
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

                                    launcher.launch(gallery)
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
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(cardPadding),
                            horizontalArrangement = SpaceBetween
                        ) {
                            Button(onClick = {
                                viewModel.saveNewDestination()
                            }, background = success) {
                                Text(stringResource(R.string.save) ,color = White)
                            }
                            Button(onClick = {
                                viewModel.closeNewDestination()
                            }, background = danger) {
                                Text(stringResource(R.string.close) ,color = White)
                            }
                        }


                    }
                }
            }
        }
    }

}




