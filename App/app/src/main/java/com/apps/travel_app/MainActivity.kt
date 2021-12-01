package com.apps.travel_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apps.travel_app.models.Destination
import com.apps.travel_app.models.Trip
import com.apps.travel_app.ui.components.BottomBarItem
import com.apps.travel_app.ui.components.BottomNavigationBar
import com.apps.travel_app.ui.pages.*
import com.apps.travel_app.ui.theme.MainActivity_Travel_AppTheme
import com.apps.travel_app.ui.theme.Travel_AppTheme
import com.apps.travel_app.ui.theme.followSystem
import com.guru.fontawesomecomposelib.FaIconType

class MainActivity : ComponentActivity() {

    private var destination: Destination? = null
    lateinit var navController: NavHostController
    var prova: MutableState<Boolean> = mutableStateOf(true)

    fun setDestination(destination: Destination, openScreen: Boolean = false) {
        this.destination = destination
        if (openScreen) {
            navController.navigate(SubPages.Location.route) {
                navController.graph.startDestinationRoute?.let { route ->
                    popUpTo(route) {
                        saveState = true
                    }
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    fun setTrip(trip: Trip, active: Boolean = false) {
        if (active) {
            val intent = Intent(this, ActiveTripActivity::class.java)
            intent.putExtra("trip", trip)
            startActivity(intent)
        } else {
            val intent = Intent(this, TripActivity::class.java)
            intent.putExtra("trip", trip)
            startActivity(intent)
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val systemTheme = sharedPref.getBoolean("darkTheme", true)

        setContent {
            MainActivity_Travel_AppTheme(systemTheme = systemTheme) {
                MainScreen(this,this)
            }
        }
    }


    @Composable
    fun MainScreen(context: Context, activity: MainActivity) {
        navController = rememberNavController()
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) }
        ) {
            Navigation(navController,context, activity)
        }
    }


    @Composable
    fun Navigation(navController: NavHostController, context: Context, activity: MainActivity) {
        val mapScreen = remember{ mutableStateOf(MapScreen())}
        NavHost(navController, startDestination = BottomBarItem.Home.route) {
            composable(BottomBarItem.Home.route) {
                HomeScreen(navController, activity)
            }
            composable(BottomBarItem.Map.route) {
                mapScreen.value.MapScreen(context, activity)
            }
            composable(BottomBarItem.Trips.route) {
                TripsScreen(activity)
            }
            composable(BottomBarItem.Explore.route) {
                ExploreScreen(navController, activity)
            }
            composable(BottomBarItem.Profile.route) {
                ProfileScreen(activity)
            }
            composable(SubPages.Location.route) {
                if (destination != null) {
                    LocationScreen(navController, destination!!, activity)
                }
            }
        }
    }

    sealed class SubPages(var route: String, var icon: FaIconType, var title: String) {
        object Location : SubPages("location", FaIcons.Home, "Location")
        object Trip : SubPages("trip", FaIcons.Home, "Trip")
    }


    override fun onResume() {

        super.onResume()
    }
}
