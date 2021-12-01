package com.apps.travel_app.ui.pages

import FaIcons
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.apps.travel_app.ui.components.Heading
import com.apps.travel_app.ui.components.NiceSwitch
import com.apps.travel_app.ui.components.NiceSwitchStates
import com.apps.travel_app.ui.theme.followSystem
import com.google.accompanist.systemuicontroller.rememberSystemUiController


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(activity: Activity) {

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = MaterialTheme.colors.background
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {

        Heading("General settings")

        NiceSwitch(followSystem.value, onChecked = {
            followSystem.value = it
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
            with (sharedPref.edit()) {
                putBoolean("darkTheme", it)
                apply()
            }
        }, NiceSwitchStates(FaIcons.MoonRegular, FaIcons.SunRegular), label = "Theme")

        Heading("Astronaut preferences")


        NiceSwitch(true, onChecked = { }, NiceSwitchStates(FaIcons.Tractor, FaIcons.City))
        NiceSwitch(true, onChecked = { }, NiceSwitchStates(FaIcons.WineGlass, FaIcons.ShoppingBasket))
        NiceSwitch(true, onChecked = { }, NiceSwitchStates(FaIcons.Mountain, FaIcons.Water))
        NiceSwitch(true, onChecked = { }, NiceSwitchStates(FaIcons.FootballBall, FaIcons.Book))



    }
}
