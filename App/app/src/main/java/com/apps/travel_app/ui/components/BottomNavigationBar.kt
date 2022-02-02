package com.apps.travel_app.ui.components

import FaIcons
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.apps.travel_app.MainActivity
import com.apps.travel_app.R
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.Trip
import com.apps.travel_app.ui.theme.iconLightColor
import com.apps.travel_app.ui.theme.primaryColor
import com.apps.travel_app.ui.utils.Response
import com.apps.travel_app.ui.utils.errorMessage
import com.apps.travel_app.ui.utils.isOnline
import com.apps.travel_app.ui.utils.sendPostRequest
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.guru.fontawesomecomposelib.FaIcon


@OptIn(ExperimentalMaterialApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
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

    val localFocusManager = LocalFocusManager.current
    fun Search(text: String, types: List<String> = arrayListOf()) {

        Thread {

            val request = "{\"text\": \"${text.replace('\n',' ').trim()}\"}" // NON-NLS
            println(request)
            val resultText = sendPostRequest(request, action = "search") //NON-NLS // NON-NLS
            if (!resultText.isNullOrEmpty()) {
                try {
                    val gson = Gson()
                    val itemType = object : TypeToken<Response>() {}.type
                    val response: Response = gson.fromJson(resultText, itemType)

                    trips.value = response.trips
                    cities.value = response.cities
                    places.value = response.places
                } catch (e: Exception) {
                    errorMessage(mainActivity.window.decorView.rootView).show()
                }

            }
        }.start()
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    Box {
        FullHeightBottomSheet(mH = 160f, background = colors.onBackground, MH = 200) { status ->
            if (status == States.COLLAPSED) {
                searchTerm = ""
                DisposableEffect(Unit) {
                    localFocusManager.clearFocus()
                    onDispose { }
                }
            }
            Column(
                modifier = Modifier
                    .height(1000.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier
                    .padding(7.dp)
                    .fillMaxWidth(0.95f)
                    .graphicsLayer {
                        shape = RoundedCornerShape(100)
                        clip = true
                    }
                    .background(colors.background), verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onDone = {Search(searchTerm);keyboardController?.hide()}),
                        value = searchTerm, onValueChange = { searchTerm = it },
                        modifier = Modifier
                            .padding(start = 10.dp, end = 10.dp)
                            .fillMaxWidth()
                            .weight(1f)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    changeState(States.EXPANDED)
                                }
                            }.semantics {
                                testTag = "searchText"
                            },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = colors.surface,
                            fontWeight = FontWeight.Bold
                        ),
                        cursorBrush = SolidColor(colors.surface)
                    )
                    IconButton(modifier = Modifier.semantics {
                        testTag = "search"
                    },onClick = {
                        Search(
                            searchTerm
                        )
                        keyboardController?.hide()
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
                                Heading(stringResource(R.string.search_your_places) )

                            }
                        }
                        if (places.value.size > 0) {
                            item {
                                Heading(stringResource(R.string.places))
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
                                                isGooglePlace = true,
                                                onClick = {
                                                    changeState(States.COLLAPSED)
                                                }
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
                                                    isGooglePlace = true,
                                                    onClick = {
                                                        changeState(States.COLLAPSED)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (trips.value.size > 0) {
                            item {
                                Heading(stringResource(R.string.trips))
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
                                                onClick = {

                                                    changeState(States.COLLAPSED)
                                                }
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
                                                    imageMaxHeight = 150f,
                                                    onClick = {

                                                        changeState(States.COLLAPSED)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (cities.value.size > 0) {
                            item {
                                Heading(stringResource(R.string.destinations))
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
                                                onClick = {

                                                    changeState(States.COLLAPSED)
                                                }
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
                                                    imageMaxHeight = 150f,
                                                    onClick = {

                                                        changeState(States.COLLAPSED)
                                                    }
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
                .height(80.dp)
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
                        ).semantics {
                            testTag = "tab$index"
                        }
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




