package com.apps.travel_app.ui.pages

import FaIcons
import android.preference.PreferenceManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apps.travel_app.MainActivity
import com.apps.travel_app.ui.components.Button
import com.apps.travel_app.ui.components.Heading
import com.apps.travel_app.ui.components.NiceSwitch
import com.apps.travel_app.ui.components.NiceSwitchStates
import com.apps.travel_app.ui.theme.followSystem
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.guru.fontawesomecomposelib.FaIcon


@Composable
fun ProfileScreen(activity: MainActivity) {


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
            with(sharedPref.edit()) {
                putBoolean("darkTheme", it)
                apply()
            }
        }, NiceSwitchStates(FaIcons.MoonRegular, FaIcons.SunRegular), label = "Theme")

        Heading("Astronaut preferences")


        NiceSwitch(true, onChecked = { }, NiceSwitchStates(FaIcons.Tractor, FaIcons.City))
        NiceSwitch(
            true,
            onChecked = { },
            NiceSwitchStates(FaIcons.WineGlass, FaIcons.ShoppingBasket)
        )
        NiceSwitch(true, onChecked = { }, NiceSwitchStates(FaIcons.Mountain, FaIcons.Water))
        NiceSwitch(true, onChecked = { }, NiceSwitchStates(FaIcons.FootballBall, FaIcons.Book))


        Button(onClick = {
            activity.signOut()
        }, background = MaterialTheme.colors.onBackground) {
            Row {
                FaIcon(faIcon = FaIcons.DoorOpen, tint = MaterialTheme.colors.surface)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log out", color = MaterialTheme.colors.surface)
            }
        }


    }
}
