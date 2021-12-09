package com.apps.travel_app.ui.pages

import FaIcons
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
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
import com.apps.travel_app.ui.components.login.LoginActivity
import com.apps.travel_app.ui.theme.followSystem
import com.facebook.login.LoginManager
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.guru.fontawesomecomposelib.FaIcon
import com.apps.travel_app.models.addUser


@Composable
fun ProfileScreen(activity: MainActivity) {

    val db = Firebase.firestore


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


        NiceSwitch(true, onChecked = {
                                     // Image upload


        },
            NiceSwitchStates(FaIcons.Tractor, FaIcons.City))


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

private fun setupCacheSize(db: FirebaseFirestore) {
    // [START fs_setup_cache]
    val settings = firestoreSettings {
        cacheSizeBytes = FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED
    }
    db.firestoreSettings = settings
    // [END fs_setup_cache]
}


