package com.apps.travel_app.ui.components

import android.content.Intent
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.apps.travel_app.MainActivity
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.MediumType
import com.apps.travel_app.models.Trip
import com.apps.travel_app.models.TripDestination
import com.apps.travel_app.ui.pages.AroundMeActivity
import com.apps.travel_app.ui.pages.Response
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.ui.utils.isOnline
import com.apps.travel_app.ui.utils.sendPostRequest
import com.google.android.libraries.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.guru.fontawesomecomposelib.FaIcon
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomNavigationBar(navController: NavController, mainActivity: MainActivity) {


    val items = listOf(
        BottomBarItem.Home,
        BottomBarItem.Map,
        BottomBarItem.Trips,
        BottomBarItem.Explore,
        BottomBarItem.Profile
    )

    var searchTerm by remember { mutableStateOf("") }
    val trips = remember {
        mutableStateOf(ArrayList<Trip>())
    }
    val cities = remember {
        mutableStateOf(ArrayList<Destination>())
    }
    val places = remember {
        mutableStateOf(ArrayList<Destination>())
    }

    fun Search(text: String, types: List<String> = arrayListOf()) {
        //val region = map.value?.projection?.visibleRegion ?: return

        Thread {
            val points = arrayListOf(
                LatLng(0.0, 0.0),
                LatLng(80.0, 0.0),
                LatLng(80.0, 20.0),
                LatLng(0.0, 20.0),
                LatLng(0.0, 0.0)
            )
            val request = "{\"area\":" + points.joinToString(",", "[", "]") { e ->
                "[${e.latitude},${e.longitude}]"
            } + ", \"text\": \"$text\", \"type\": \"" + types.joinToString("|").lowercase() + "\"}"
            println(request)
            val resultText = sendPostRequest(request, action = "search")
            if (!resultText.isNullOrEmpty()) {
                val gson = Gson()
                val itemType = object : TypeToken<Response>() {}.type
                val response: Response = gson.fromJson(resultText, itemType)

                trips.value = response.trips
                cities.value = response.cities
                places.value = response.places

                var index = 0

                /*mainActivity.runOnUiThread {
                    if (map.value != null) {
                        map.value!!.clear()
                        trips.value.forEach {
                            addMarker(
                                LatLng(
                                    it.startingPoint.latitude,
                                    it.startingPoint.longitude
                                ), index++, it.name
                            )
                        }
                        cities.value.forEach {
                            addMarker(
                                LatLng(it.latitude, it.longitude),
                                index++,
                                it.name
                            )
                        }
                        places.value.forEach {
                            addMarker(
                                LatLng(it.latitude, it.longitude),
                                index++,
                                it.name
                            )
                        }
                    }
                }*/
            }
        }.start()
    }

    Box {

        FullHeightBottomSheet(mH = 370, background = colors.onBackground) { status ->
            if (status == States.COLLAPSED) {
                searchTerm = ""
            }
            Column(
                modifier = Modifier
                    .height(1000.dp)
                    .fillMaxWidth()
            ) {
                Row(modifier = Modifier
                    .padding(10.dp)
                    .graphicsLayer {
                        shape = RoundedCornerShape(30)
                        clip = true
                    }
                    .background(colors.background), verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = searchTerm, onValueChange = { searchTerm = it },
                        modifier = Modifier
                            .padding(start = 10.dp, end = 10.dp)
                            .fillMaxWidth()
                            .weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = colors.surface,
                            fontWeight = FontWeight.Bold
                        ),
                    )
                    IconButton(onClick = {
                        Search(
                            searchTerm
                        )
                    }) {
                        FaIcon(FaIcons.Search, tint = iconLightColor)
                    }
                }

                if (!isOnline(mainActivity)) {
                    NetworkError()
                } else if (status == States.EXPANDED) {
                    LazyColumn {
                        if (places.value.size ==  0 && cities.value.size == 0 && trips.value.size == 0) {
                            item {
                                Heading("Search your places...")

                            }
                        }
                        if (places.value.size > 0) {
                            item {
                                Heading("Places")
                            }
                        }
                        val loaded = arrayListOf<Destination>()
                        item {
                            places.value.forEachIndexed { index, place ->
                                if (!loaded.contains(place)) {
                                    loaded.add(place)
                                    Row {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(0.dp, 120.dp)
                                        ) {
                                            MainCard(
                                                infoScale = 0.6f,
                                                destination = place,
                                                rating = place.rating,
                                                mainActivity = mainActivity,
                                                icon = FaIcons.Google,
                                                imageMaxHeight = 120f,
                                                imageMinHeight = 120f,
                                                isGooglePlace = true
                                            )
                                        }
                                        if (index < places.value.size - 1) {
                                            val place2 = places.value[index + 1]
                                            loaded.add(place2)
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(0.dp, 120.dp)
                                            ) {
                                                MainCard(
                                                    infoScale = 0.6f,
                                                    destination = place2,
                                                    rating = place2.rating,
                                                    mainActivity = mainActivity,
                                                    icon = FaIcons.Google,
                                                    imageMaxHeight = 150f,
                                                    imageMinHeight = 120f,
                                                    isGooglePlace = true
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (trips.value.size > 0) {
                            item {
                                Heading("Trips")
                            }
                        }
                        val loadedTrips = arrayListOf<Trip>()
                        item {
                            trips.value.forEachIndexed { index, trip ->
                                if (!loadedTrips.contains(trip)) {
                                    loadedTrips.add(trip)
                                    Row {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(0.dp, 120.dp)
                                        ) {
                                            TripCard(
                                                trip = trip,
                                                rating = trip.rating,
                                                imageMaxHeight = 150f,
                                                infoScale = 0.6f,
                                            )
                                        }
                                        if (index < trips.value.size - 1) {
                                            val trip2 = trips.value[index + 1]
                                            loadedTrips.add(trip2)
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(0.dp, 120.dp)
                                            ) {
                                                TripCard(
                                                    infoScale = 0.6f,
                                                    trip = trip2,
                                                    rating = trip2.rating,
                                                    imageMaxHeight = 150f
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (cities.value.size > 0) {
                            item {
                                Heading("Destinations")
                            }
                        }
                        loaded.clear()
                        item {
                            cities.value.forEachIndexed { index, city ->
                                if (!loaded.contains(city)) {
                                    loaded.add(city)
                                    Row {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(0.dp, 120.dp)
                                        ) {
                                            MainCard(
                                                destination = city,
                                                rating = city.rating,
                                                mainActivity = mainActivity,
                                                icon = FaIcons.Google,
                                                imageMaxHeight = 150f,
                                                infoScale = 0.6f,
                                            )
                                        }
                                        if (index < cities.value.size - 1) {
                                            val place2 = cities.value[index + 1]
                                            loaded.add(place2)
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(0.dp, 120.dp)
                                            ) {
                                                MainCard(
                                                    infoScale = 0.6f,
                                                    destination = place2,
                                                    rating = place2.rating,
                                                    mainActivity = mainActivity,
                                                    icon = FaIcons.Google,
                                                    imageMaxHeight = 150f
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


        }
        BottomNavigation(
            backgroundColor = colors.onBackground,
            contentColor = colors.surface,
            elevation = 0.dp,
            modifier = Modifier
                .height(60.dp)
                .align(BottomStart)
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            items.forEachIndexed { index, item ->
                val scale: Float by animateFloatAsState(
                    if (currentRoute == item.route) 1.35f else 1f,
                    animationSpec = tween(
                        durationMillis = 700,
                        easing = LinearOutSlowInEasing
                    )
                )
                BottomNavigationItem(
                    icon = {
                        if (index == 2) {
                            Row(modifier = Modifier
                                .graphicsLayer {
                                    shape = RoundedCornerShape(100)
                                    clip = true
                                }
                                .width(40.dp)
                                .height(40.dp)
                                .background(primaryColor),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center) {
                                FaIcon(
                                    item.icon,
                                    tint = Color.White,

                                    )
                            }
                        } else {
                            FaIcon(
                                item.icon,
                                tint = if (currentRoute == item.route) colors.surface else iconLightColor,
                            )
                        }
                    },
                    selectedContentColor = primaryColor,
                    unselectedContentColor = iconLightColor,
                    alwaysShowLabel = false,
                    modifier = Modifier.then(
                        Modifier.scale(
                            scale
                        )
                    ),
                    selected = currentRoute == item.route,
                    onClick = {
                        navController.navigate(item.route) {

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
        }
    }
}




