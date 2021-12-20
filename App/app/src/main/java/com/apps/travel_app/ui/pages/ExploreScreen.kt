package com.apps.travel_app.ui.pages

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.apps.travel_app.MainActivity
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.Trip
import com.apps.travel_app.ui.components.Heading
import com.apps.travel_app.ui.theme.cardElevation
import com.apps.travel_app.ui.theme.cardPadding
import com.apps.travel_app.ui.theme.cardRadius
import com.apps.travel_app.ui.theme.textSmall
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.guru.fontawesomecomposelib.FaIconType
import com.skydoves.landscapist.glide.GlideImage


val filterIcons = arrayListOf(
    FilterIcon(FaIcons.Cocktail, "Bar"),
    FilterIcon(FaIcons.DrumstickBite, "Restaurant"),
    FilterIcon(FaIcons.Hotel, "Lodging"),
    FilterIcon(FaIcons.DollarSign, "Bank"),
    FilterIcon(FaIcons.Opencart, "Supermarket"),
    FilterIcon(FaIcons.GasPump, "Gas_Station"),
    FilterIcon(FaIcons.HatCowboy, "Tourist_attraction"),
    FilterIcon(FaIcons.Monument, "Museum"),
    FilterIcon(FaIcons.Running, "Gym"),
    FilterIcon(FaIcons.Taxi, "Taxi_stand"),
    FilterIcon(FaIcons.Bus, "Bus_station"),
    FilterIcon(FaIcons.Train, "Train_station"),
    FilterIcon(FaIcons.Parking, "parking"),
    FilterIcon(FaIcons.Film, "cinema"),
    FilterIcon(FaIcons.Coffee, "cafe"),
    FilterIcon(FaIcons.Church, "church"),
    FilterIcon(FaIcons.Mosque, "mosque")
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExploreScreen(navController: NavController, mainActivity: MainActivity) {



    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = colors.background
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),

        ) {
        Heading(
            "Take a deep breath...",
            color = colors.surface,
            modifier = Modifier.padding(cardPadding)
        )
        Text(
            "Just a moment for you to get inspired by the wonder of our world",
            color = colors.surface,
            modifier = Modifier
                .padding(cardPadding)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = textSmall
        )
        Heading(
            "... and make it yours",
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
                                    navController.navigate("Map") {

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
                        "Map drawing",
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
                        "The wall",
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
                        "Around you",
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

class Response {
    var places: ArrayList<Destination> = arrayListOf()
    var cities: ArrayList<Destination> = arrayListOf()
    var trips: ArrayList<Trip> = arrayListOf()
}
