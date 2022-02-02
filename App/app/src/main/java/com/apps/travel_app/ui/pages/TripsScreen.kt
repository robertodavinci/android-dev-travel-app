package com.apps.travel_app.ui.pages

/**
 * Composable screen showing all of the trips the user has favourited or saved.
 * Accessed by pressing the middle blue button on the navigation bar of the app.
 * Trips saved and fetched in this function can be accessed both in online and offline mode
 * of the app. Directly connected tp the TripsViewModel that handles actions that are occurring in this
 * file.
 */

import FaIcons
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.apps.travel_app.MainActivity
import com.apps.travel_app.R
import com.apps.travel_app.data.room.db.AppDatabase
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.Trip
import com.apps.travel_app.ui.components.Button
import com.apps.travel_app.ui.components.Heading
import com.apps.travel_app.ui.components.MainCard
import com.apps.travel_app.ui.components.TripCard
import com.apps.travel_app.ui.pages.viewmodels.TripsViewModel
import com.apps.travel_app.ui.theme.cardPadding
import com.apps.travel_app.ui.theme.cardRadius
import com.apps.travel_app.ui.theme.danger
import com.apps.travel_app.ui.theme.primaryColor
import com.apps.travel_app.user
import com.google.firebase.messaging.FirebaseMessaging
import com.guru.fontawesomecomposelib.FaIcon

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TripsScreen(mainActivity: MainActivity) {



    val db = Room.databaseBuilder(
        mainActivity,
        AppDatabase::class.java, AppDatabase.NAME
    ).build()



    val saved = remember { mutableStateListOf<Destination>() }
    val savedTrips = remember { mutableStateListOf<Trip>() }

    val viewModel = remember {TripsViewModel(db) { dests, trips ->
        saved.addAll(dests.toList())
        savedTrips.addAll(trips.toList())
    } }

    Column(modifier = Modifier.background(MaterialTheme.colors.background)) {

        Row(
            modifier = Modifier
                .graphicsLayer {
                    shape = RoundedCornerShape(bottomStart = 50.dp)
                    clip = true
                }
                .background(primaryColor)

        ) {
            Heading(
                "${user.displayName.ifEmpty { user.email}}'s",
                color = White,
                modifier = Modifier.padding(top = cardPadding * 3, bottom = cardPadding * 2)
            )
        }

        Box(
            modifier = Modifier.background(primaryColor)
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        shape = RoundedCornerShape(topEnd = 50.dp)
                        clip = true
                    }
                    .background(MaterialTheme.colors.background)
                    .align(Center),
                horizontalAlignment = Alignment.End
            ) {
                item {

                    Button(
                        onClick = {
                            mainActivity.createTrip()
                        },
                        background = primaryColor,
                        modifier = Modifier
                            .padding(top = 15.dp, end = 25.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Row {
                            FaIcon(FaIcons.Hiking, tint = White, size = 18.dp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(stringResource(R.string.create_yours), color = White)

                        }
                    }

                    Spacer(modifier = Modifier.height(cardPadding))
                }

                    if (viewModel.saved.size > 0 || viewModel.savedTrips.size > 0) {
                        item {
                            Heading(stringResource(R.string.saved))
                        }

                        itemsIndexed(saved, key = { _, a -> a.id}) { index, destination ->
                            val state = rememberDismissState(
                                confirmStateChange = {
                                    if (it == DismissValue.DismissedToStart || it == DismissValue.DismissedToEnd) {
                                        Thread {
                                            Thread.sleep(500)
                                            db.locationDao().delete(destination.toLocation())
                                            viewModel.saved.remove(destination)
                                            saved.remove(destination)
                                            FirebaseMessaging.getInstance().unsubscribeFromTopic("city" + destination.id)
                                        }.start()
                                    }
                                    true
                                }
                            )
                            SwipeToDismiss(state = state, background = {
                                Row(
                                    Modifier
                                        .fillMaxSize()
                                        .padding(cardPadding)
                                        .graphicsLayer {
                                            shape = RoundedCornerShape(cardRadius)
                                            clip = true
                                        }
                                        .background(danger),
                                    verticalAlignment = CenterVertically,
                                    horizontalArrangement = SpaceBetween
                                ) {
                                    FaIcon(
                                        FaIcons.Trash,
                                        tint = White,
                                        modifier = Modifier.padding(cardPadding)
                                    )
                                    FaIcon(
                                        FaIcons.Trash,
                                        tint = White,
                                        modifier = Modifier.padding(cardPadding)
                                    )
                                }
                            }) {
                                MainCard(
                                    destination = destination,
                                    rating = destination.rating,
                                    mainActivity = mainActivity,
                                    imageMaxHeight = 100f
                                )
                            }
                        }

                        itemsIndexed(savedTrips) { index, trip ->
                            val state = rememberDismissState(
                                confirmStateChange = {
                                    if (it == DismissValue.DismissedToStart || it == DismissValue.DismissedToEnd) {
                                        Thread {
                                            db.tripDao().delete(trip.toTripDb(trip.mainDestination.id))
                                            trip.getTripStep(trip.id).forEach { step ->
                                                db.tripStepDao().delete(step)
                                            }
                                        }.start()
                                        viewModel.savedTrips.removeAt(index)
                                        savedTrips.removeAt(index)
                                    }
                                    true
                                }
                            )
                            SwipeToDismiss(state = state, background = {
                                Row(
                                    Modifier
                                        .fillMaxSize()
                                        .padding(cardPadding)
                                        .graphicsLayer {
                                            shape = RoundedCornerShape(cardRadius)
                                            clip = true
                                        }
                                        .background(danger),
                                    verticalAlignment = CenterVertically,
                                    horizontalArrangement = SpaceBetween
                                ) {
                                    FaIcon(
                                        FaIcons.Trash,
                                        tint = White,
                                        modifier = Modifier.padding(cardPadding)
                                    )
                                    FaIcon(
                                        FaIcons.Trash,
                                        tint = White,
                                        modifier = Modifier.padding(cardPadding)
                                    )
                                }
                            }) {
                                TripCard(
                                    trip = trip,
                                    rating = trip.rating,
                                    mainActivity = mainActivity,
                                    imageMaxHeight = 100f,
                                    icon = if (trip.incomplete) FaIcons.StickyNoteRegular else null
                                )
                            }
                        }


                    } else {
                        item {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                                Spacer(Modifier.size(40.dp))
                                FaIcon(
                                    FaIcons.Searchengin,
                                    tint = MaterialTheme.colors.surface,
                                    size = 50.dp
                                )

                                Heading(
                                    stringResource(R.string.nothing_saved),
                                    Modifier.padding(
                                        cardPadding
                                    )
                                )
                                Button(onClick = {
                                    mainActivity.goHome()
                                }

                                ) {
                                    Text(
                                        stringResource(R.string.inspired),
                                        color = MaterialTheme.colors.surface,
                                        modifier = Modifier.padding(
                                            cardPadding
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }



