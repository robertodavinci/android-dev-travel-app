package com.apps.travel_app.ui.pages
/**
 * Composable function of the home screen, the initial screen after login.
 * It can also be accessed by pressing the first button in the navigation
 * controller. Displays two different sections, Trips and Destinations, in which
 * both of those are located in the form of cards. Uses a small gif as a
 * loading transition before fetching all of the data. Directly communicates
 * with the HomeViewModel for handling fetching and displaying the data.
 *
 */
// Vincenzo Manto + Robert Medvedec
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.apps.travel_app.MainActivity
import com.apps.travel_app.R
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.Trip
import com.apps.travel_app.ui.components.*
import com.apps.travel_app.ui.pages.viewmodels.HomeViewModel
import com.apps.travel_app.ui.theme.*
import com.apps.travel_app.ui.utils.getTriangularMask
import com.apps.travel_app.ui.utils.isOnline
import com.apps.travel_app.user
import com.skydoves.landscapist.glide.GlideImage


@Composable
fun HomeScreen(navController: NavController, mainActivity: MainActivity) {

   
    val viewModel = remember { HomeViewModel(mainActivity) }
    
    val tabs = arrayListOf(
        stringResource(R.string.destinations),
        //"Adventures",
        stringResource(R.string.trips))

    

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        item {

            var experienceImage: Bitmap? by remember { mutableStateOf(null) }

            if (experienceImage == null) {
                experienceImage = getTriangularMask(
                    R.drawable.landscape,
                    true,
                    mainActivity.resources
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(0.dp,300.dp)
            ) {

                val modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                if (experienceImage != null) {
                    GlideImage(
                        imageModel = experienceImage,
                        modifier = modifier,
                        contentScale = ContentScale.Crop
                    )
                }
                Column(
                    Modifier
                        .fillMaxSize()
                        .height(300.dp)
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,

                    ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                end = cardPadding,
                                start = cardPadding,
                                top = cardPadding * 2,
                                bottom = 10.dp
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val text = with(AnnotatedString.Builder("")) {
                            pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                            append(stringResource(R.string.hi))
                            pop()
                            pushStyle(SpanStyle(fontFamily = pacifico))
                            append(user.displayName ?: user.email)
                            toAnnotatedString()
                        }
                        Text(
                            text = text,
                            fontSize = textHeading,
                            color = Color.White
                        )
                        IconButton(onClick = {
                            navController.navigate("profile") {
                                navController.graph.startDestinationRoute?.let { route ->
                                    popUpTo(route) {
                                        saveState = true
                                    }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }

                        }) {
                            GlideImage(
                                imageModel = R.mipmap.icon,
                                contentDescription = "",
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(40.dp)
                                    .graphicsLayer {
                                        shape = RoundedCornerShape(100)
                                        clip = true
                                    }
                            )
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(R.string.explore_new),
                            color = Color.White,
                            textAlign = Center,
                            fontSize = textHeading,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(R.string.experiences),
                            color = Color.White,
                            textAlign = Center,
                            fontFamily = pacifico,
                            fontSize = textHeading * 1.5,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }


            if (!isOnline(mainActivity)) {
                NetworkError()
            } else {
                Spacer(Modifier.height(15.dp))
                var tab by remember { mutableStateOf(0) }
                TabRow(
                    selectedTabIndex = tab,
                    divider = {},
                    backgroundColor = Transparent,
                    contentColor = Transparent
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            text = {
                                Column {
                                    Text(title, color = colors.surface,fontSize = textNormal)
                                    Spacer(
                                        Modifier
                                            .height(3.dp)
                                            .width(20.dp)
                                            .background(
                                                if (tab == index) primaryColor else Transparent
                                            )
                                    )

                                }
                            },
                            selected = tab == index,
                            onClick = { tab = index }
                        )
                    }
                }
                Heading(
                    "Top ${tabs[tab]}"
                )

                Box(
                    modifier = Modifier.padding(bottom = 40.dp)
                ) {
                    if (viewModel.trips.size <= 0) {
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
                            if (tab == 0) {
                                val loadedCities = ArrayList<Destination>()
                                viewModel.cities.forEachIndexed { index, destination ->
                                    if (!loadedCities.contains(destination)) {
                                        loadedCities.add(destination)
                                        Row {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(0.dp, 120.dp)
                                            ) {
                                                MainCard(
                                                    destination = destination,
                                                    rating = destination.rating,
                                                    padding = 5.dp,
                                                    shadow = 10.dp,
                                                    mainActivity = mainActivity
                                                )
                                            }
                                            if (destination.rating <= 4.5f && index < viewModel.cities.size - 1) {
                                                val trip2 = viewModel.cities[index + 1]
                                                loadedCities.add(trip2)
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .heightIn(0.dp, 120.dp)
                                                ) {
                                                    MainCard(
                                                        destination = trip2,
                                                        rating = trip2.rating,
                                                        padding = 5.dp,
                                                        shadow = 10.dp,
                                                        mainActivity = mainActivity
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                val loadedTrips = ArrayList<Trip>()
                                val array = (viewModel.trips)
                                array.forEachIndexed { index, trip ->
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
                                                    padding = 5.dp,
                                                    shadow = 10.dp,
                                                    mainActivity = mainActivity
                                                )
                                            }
                                            if (trip.rating <= 2.5f && index < array.size - 1) {
                                                val trip2 = array[index + 1]
                                                loadedTrips.add(trip2)
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .heightIn(0.dp, 120.dp)
                                                ) {
                                                    TripCard(
                                                        trip = trip2,
                                                        rating = trip2.rating,
                                                        padding = 5.dp,
                                                        shadow = 10.dp,
                                                        mainActivity = mainActivity
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(120.dp))
                        }


                    }
                }

            }
        }
    }

}







