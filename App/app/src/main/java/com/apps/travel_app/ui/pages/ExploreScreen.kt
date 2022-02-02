package com.apps.travel_app.ui.pages

/**
 * Composable function that is displayed by pressing the fourth
 * icon in the navigation bar. Generates three different cards that can
 * be pressed and that lead the user to three different  destinations
 * - "The Wall" (InspirationActivity), "Around you" (AroundMeActivity), and
 * "Map drawing" (MapScreen). Function servers mainly as a navigation and
 * doesn't have any additional functionalities.
 */

import FaIcons
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.apps.travel_app.MainActivity
import com.apps.travel_app.R
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.Trip
import com.apps.travel_app.ui.components.Heading
import com.apps.travel_app.ui.theme.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.guru.fontawesomecomposelib.FaIconType
import com.skydoves.landscapist.glide.GlideImage



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExploreScreen(navController: NavController, mainActivity: MainActivity) {


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),

        ) {
        Heading(
            stringResource(R.string.something_stupid_1) ,
            color = colors.surface,
            modifier = Modifier.padding(cardPadding * 2)
        )
        Text(
            stringResource(R.string.something_stupid_2),
            color = colors.surface,
            modifier = Modifier
                .padding(cardPadding)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = textNormal
        )
        Heading(
            stringResource(R.string.something_stupid_3),
            color = colors.surface,
            modifier = Modifier.padding(cardPadding)
        )
        Row( verticalAlignment = Alignment.CenterVertically) {
            Card(
                modifier = Modifier
                    .padding(cardPadding)
                    .weight(1f)
                    .heightIn(0.dp, 150.dp),
                elevation = cardElevation,
                shape = RoundedCornerShape(cardRadius)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    navController.navigate("map") {

                                        navController.graph.startDestinationRoute?.let { route ->
                                            popUpTo(route) {
                                                saveState = true
                                            }
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                ) {
                    GlideImage(
                        modifier = Modifier.fillMaxSize(),
                        imageModel = "https://braintrustinteractive.s3.amazonaws.com/wp-content/uploads/background-blur-gradient.png",
                        contentScale = ContentScale.Crop,
                    )
                    Heading(
                        stringResource(R.string.map_drawing)
                        ,
                        modifier = Modifier
                            .align(CenterStart)
                            .padding(cardPadding),
                        color = White
                    )
                }

            }
        }
        Row( verticalAlignment = Alignment.CenterVertically) {
            Card(
                modifier = Modifier
                    .padding(cardPadding)
                    .weight(1f)
                    .heightIn(0.dp, 150.dp),
                elevation = cardElevation,
                shape = RoundedCornerShape(cardRadius)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTapGestures (
                                onTap = {
                                    val intent = Intent(mainActivity, InspirationActivity::class.java)
                                    startActivity(mainActivity, intent, null)
                                }
                            )
                        }
                ) {
                    GlideImage(
                        modifier = Modifier.fillMaxSize(),
                        imageModel = "https://free4kwallpapers.com/uploads/originals/2019/08/28/gradient-blur-wallpaper.jpg",
                        contentScale = ContentScale.Crop,
                    )
                    Heading(
                        stringResource(R.string.wall),
                        modifier = Modifier
                            .align(CenterStart)
                            .padding(cardPadding),
                        color = White
                    )
                }

            }

            Card(
                modifier = Modifier
                    .padding(cardPadding)
                    .weight(1f)
                    .heightIn(0.dp, 150.dp),
                elevation = cardElevation,
                shape = RoundedCornerShape(cardRadius)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTapGestures (
                                onTap = {
                                    val intent = Intent(mainActivity, AroundMeActivity::class.java)
                                    startActivity(mainActivity, intent, null)
                                }
                            )
                        }
                ) {
                    GlideImage(
                        modifier = Modifier.fillMaxSize(),
                        imageModel = "https://www.brazilcouncil.org/wp-content/uploads/2021/07/istockphoto-1212284111-170667a.jpg",
                        contentScale = ContentScale.Crop,
                    )
                    Heading(
                        stringResource(R.string.around_you)
                        ,
                        modifier = Modifier
                            .align(CenterStart)
                            .padding(cardPadding),
                        color = White
                    )
                }

            }
        }
    }
}


class FilterIcon(var icon: FaIconType, var name: String) {
}

