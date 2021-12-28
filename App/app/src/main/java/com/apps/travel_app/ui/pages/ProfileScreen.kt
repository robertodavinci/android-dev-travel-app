package com.apps.travel_app.ui.pages

import FaIcons
<<<<<<< Updated upstream
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
=======
import android.content.Context
import android.preference.PreferenceManager
import android.provider.Settings.Global.getString
>>>>>>> Stashed changes
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apps.travel_app.MainActivity
import com.apps.travel_app.ui.components.Button
import com.apps.travel_app.ui.components.Heading
import com.apps.travel_app.ui.components.NiceSwitch
import com.apps.travel_app.ui.components.NiceSwitchStates
<<<<<<< Updated upstream
import com.apps.travel_app.ui.components.login.LoginActivity
import com.apps.travel_app.ui.theme.followSystem
import com.facebook.login.LoginManager
=======
import com.apps.travel_app.ui.theme.Travel_AppTheme
import com.apps.travel_app.ui.theme.followSystem
import com.apps.travel_app.ui.theme.primaryColor
>>>>>>> Stashed changes
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


    val offlineMode: Boolean = activity?.getPreferences( Context.MODE_PRIVATE).getBoolean("offlineMode", false)
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = MaterialTheme.colors.background
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background), horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Heading("General settings")

        NiceSwitch(followSystem.value, onChecked = {
            followSystem.value = it
            val sharedPref = activity?.getSharedPreferences("darkTheme", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putBoolean("darkTheme", it)
                apply()
            }
        }, NiceSwitchStates(FaIcons.MoonRegular, FaIcons.SunRegular), label = "App theme")

        Heading("Astronaut preferences")

        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.background),horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colors.background),
                horizontalArrangement = Arrangement.Center
            )
            {
                Button(
                    onClick = {

                    },
                    content = {
                        Text(
                            text = "Account details",
                            color = MaterialTheme.colors.surface,
                            modifier = Modifier.padding(5.dp)
                        )
                    }

                    /*modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                background = Color.White*/

<<<<<<< Updated upstream
        NiceSwitch(true, onChecked = {
                                     // Image upload


        },
            NiceSwitchStates(FaIcons.Tractor, FaIcons.City))


        NiceSwitch(
=======
                )
                //Text(text = "")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        NiceSwitch(true, onChecked = { }, NiceSwitchStates(FaIcons.Tractor, FaIcons.City))
            NiceSwitch(
>>>>>>> Stashed changes
            true,
            onChecked = { },
            NiceSwitchStates(FaIcons.WineGlass, FaIcons.ShoppingBasket)
        )
        NiceSwitch(true, onChecked = { }, NiceSwitchStates(FaIcons.Mountain, FaIcons.Water))
        NiceSwitch(followSystem.value, onChecked = {
            followSystem.value = it
            val sharedPref = activity?.getSharedPreferences("offlineMode",Context.MODE_PRIVATE) ?: return@NiceSwitch
            with(sharedPref.edit()) {
                putBoolean("offlineMode", it)
                apply()
            }
        }, NiceSwitchStates(FaIcons.InternetExplorer, FaIcons.DoorClosed), label =
            if(offlineMode)  "Offline"
            else "Online"
        )
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


