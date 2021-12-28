package com.apps.travel_app.ui.pages

import FaIcons
import android.preference.PreferenceManager
import android.util.Log
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
import com.apps.travel_app.ui.theme.cardPadding
import com.apps.travel_app.ui.theme.followSystem
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.guru.fontawesomecomposelib.FaIcon


@Composable
fun ProfileScreen(activity: MainActivity) {


   /* val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = MaterialTheme.colors.background
    )*/

    val firebaseId = FirebaseAuth.getInstance().currentUser?.uid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(cardPadding)
    ) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
        Spacer(Modifier.height(cardPadding * 2))
        Heading("General settings")

        NiceSwitch(followSystem.value, onChecked = {
            followSystem.value = it
            with(sharedPref.edit()) {
                putBoolean("darkTheme", it)
                apply()
            }
        }, NiceSwitchStates(FaIcons.MoonRegular, FaIcons.SunRegular), label = "Theme")

        Heading("Astronaut preferences")


        NiceSwitch(sharedPref.getBoolean("receiveNotification", false), onChecked = {
            if (it) {
                FirebaseMessaging.getInstance().subscribeToTopic(firebaseId.toString()).addOnCompleteListener {
                    Log.d("FCM","Subscribed")
                }
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(firebaseId.toString()).addOnCompleteListener {
                    Log.d("FCM","Unsubscribed")
                }
            }
            with(sharedPref.edit()) {
                putBoolean("receiveNotification", it)
                apply()
            }
        }, NiceSwitchStates(FaIcons.BellRegular, FaIcons.BellSlashRegular), label = "Receive personal notification")


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
