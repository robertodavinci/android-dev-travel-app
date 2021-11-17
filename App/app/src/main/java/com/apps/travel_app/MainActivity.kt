package com.apps.travel_app

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.activity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apps.travel_app.ui.components.BottomBarItem
import com.apps.travel_app.ui.components.BottomNavigationBar
import com.apps.travel_app.ui.pages.*
import com.apps.travel_app.ui.theme.Travel_AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Travel_AppTheme {
                MainScreen(this,this)
            }
        }
    }
}

@Composable
fun MainScreen(context: Context, activity: MainActivity) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) {
        Navigation(navController,context, activity)
    }
}

@Composable
fun Navigation(navController: NavHostController, context: Context, activity: Activity) {
    NavHost(navController, startDestination = BottomBarItem.Home.route) {
        composable(BottomBarItem.Home.route) {
            HomeScreen()
        }
        composable(BottomBarItem.Map.route) {
            MapScreen(context, activity)
        }
        composable(BottomBarItem.Trips.route) {
            TripsScreen()
        }
        composable(BottomBarItem.Explore.route) {
            ExploreScreen()
        }
        composable(BottomBarItem.Profile.route) {
            ProfileScreen()
        }
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    //MainScreen(LocalContext.current)
}