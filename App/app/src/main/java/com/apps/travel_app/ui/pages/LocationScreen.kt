package com.apps.travel_app.ui.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.navigation.NavController
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator
import com.apps.travel_app.MainActivity
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.Rating
import com.apps.travel_app.ui.components.*
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.ui.utils.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Math.random


lateinit var ratings: MutableState<ArrayList<Rating>>
var loaded: MutableState<Boolean> = mutableStateOf(false)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocationScreen(
    navController: NavController,
    destination: Destination,
    mainActivity: MainActivity
) {

    val activities = arrayListOf(
        "What to do",
        "Where to stay",
        "Where to eat",
        "Crowd-less",
        "1-day trip",
        "Nearby"
    ).toList()
    loaded = remember { mutableStateOf(false) }
    ratings = remember { mutableStateOf(ArrayList()) }

    if (!loaded.value)
        getRatings(destination)


    val scrollState = rememberScrollState()
    val maxScroll = 100
    var percentage = scrollState.value.toFloat() / maxScroll
    percentage = if (percentage > 1) 1f else percentage

    BoxWithConstraints {
        Box(
            modifier = Modifier
                .background(lightBackground)
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
                        badges = arrayListOf("Cultural", "Youth"),
                        views = 206,
                        mainActivity = mainActivity,
                        padding = (cardPadding * (1 - percentage)),
                        radius = (cardRadius * (1 - percentage))
                    )
                }

                FlexibleRow(
                    alignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(cardPadding / 2)
                        .fillMaxWidth()
                ) {
                    activities.forEach { activity ->
                        Button(onClick = {}, modifier = Modifier.padding(5.dp)) {
                            Text(activity)
                        }
                    }
                }

                Heading(
                    "Top trips"
                )

                LazyRow(
                    modifier = Modifier.padding(cardPadding)
                ) {
                    items(3) {
                        val trip = Destination()
                        trip.thumbnailUrl =
                            "https://cdn.britannica.com/q:60/50/161650-050-FAEF5CAC/Group-Waltshire-cows-England.jpg"
                        trip.name = "Roadtrip among cows"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            MainCard(
                                destination = trip,
                                rating = 4.5f,
                                padding = 5.dp,
                                shadow = 10.dp,
                                badges = arrayListOf("Youth", "Nature","Crowd-less"),
                                mainActivity = mainActivity,
                                infoScale = 0.8f,
                                imageMaxHeight = 130f
                            )
                        }
                    }
                }

                Heading(
                    "Description"
                )

                Text(
                    text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum",
                    fontSize = textSmall,
                    color = textLightColor,
                    modifier = Modifier.padding(cardPadding).fillMaxWidth()
                )

                Heading(
                    "Facilities"
                )

                LazyRow(
                    modifier = Modifier.padding(cardPadding)
                ) {
                    items(3) { i ->
                        val trip = Destination()
                        trip.thumbnailUrl =
                            "https://media-cdn.tripadvisor.com/media/photo-s/04/07/f9/d0/da-nino.jpg"
                        trip.name = "Da Nino"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(0.dp, 120.dp)
                        ) {
                            MainCard(
                                destination = trip,
                                rating = 4.8f,
                                padding = 5.dp,
                                shadow = 10.dp,
                                mainActivity = mainActivity,
                                infoScale = 0.8f,
                                icon = if (i % 2 == 0) FaIcons.Google else FaIcons.Tripadvisor
                            )
                        }
                    }
                }

                Heading(
                    "Top ratings"
                )

                Box(
                    modifier = Modifier.padding(bottom = 40.dp)
                ) {
                    if (ratings.value.size <= 0) {
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

                            ratings.value.forEach { rating ->
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
                                        lightBackground,
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

fun getRatings(destionation: Destination) {
    loaded.value = true

    if (ratings.value.size <= 0) {
        Thread {

            val result = ArrayList<Rating>()

            val request = "${destionation.latitude},${destionation.longitude}"
            val ratingsText = sendPostRequest(request, action = "ratings")
            val gson = Gson()
            val itemType = object : TypeToken<List<Rating>>() {}.type
            val _ratings: List<Rating> = gson.fromJson(ratingsText, itemType)
            for (rating in _ratings) {
                rating.rating = random().toFloat() * 5f
                result.add(rating)
            }
            ratings.value = result
        }.start()
    }
}




