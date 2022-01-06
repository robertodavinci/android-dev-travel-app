package com.apps.travel_app.ui.pages

import FaIcons
import androidx.preference.PreferenceManager
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.apps.travel_app.MainActivity
import com.apps.travel_app.ui.components.Button
import com.apps.travel_app.ui.components.Heading
import com.apps.travel_app.ui.components.NiceSwitch
import com.apps.travel_app.ui.components.NiceSwitchStates
import com.apps.travel_app.ui.theme.cardPadding
import com.apps.travel_app.ui.theme.followSystem
import com.apps.travel_app.ui.theme.smallPadding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.guru.fontawesomecomposelib.FaIcon
import com.apps.travel_app.ui.theme.Travel_AppTheme
import com.apps.travel_app.user
import com.guru.fontawesomecomposelib.FaIconType


private val showUsernameChange = mutableStateOf(false)
private val showRealNameChange = mutableStateOf(false)

@Composable
fun ProfileScreen(activity: MainActivity) {


   /* val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = MaterialTheme.colors.background
    )*/
    val firebaseId = FirebaseAuth.getInstance().currentUser?.uid
    var currentUsername: String? = user.displayName
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(cardPadding)
    ) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
        Spacer(Modifier.height(cardPadding * 2))
        Heading("General settings")

        NiceSwitch(checked = followSystem.value, onChecked = {
            followSystem.value = it
            with(sharedPref.edit()) {
                putBoolean("darkTheme", it)
                apply()
            }
            Log.i("Theme 1 - ", it.toString())
            Log.i("Theme 2 - ", sharedPref.getBoolean("darkTheme", false).toString())
        }, states = NiceSwitchStates(T = FaIcons.MoonRegular, F = FaIcons.SunRegular), label = "Theme")

        NiceSwitch(checked = sharedPref.getBoolean("receiveNotification", false), onChecked = {
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
        }, states =  NiceSwitchStates(FaIcons.BellRegular, FaIcons.BellSlashRegular), label = "Receive personal notification")

        Heading("Astronaut preferences")
        drawButton(function = { displayChangeBox(true) },"Change username",FaIcons.User, MaterialTheme.colors.surface, MaterialTheme.colors.onBackground)
        //if (showUsernameChange.value)
        AnimatedVisibility(visible = showUsernameChange.value, enter = expandVertically(expandFrom = Alignment.Top), exit = shrinkVertically(shrinkTowards = Alignment.Top)) {
                detailsChange(currentUsername)
            }
        Spacer(Modifier.padding(smallPadding))
        drawButton(function = { displayChangeBox(false) },"Change real credentials",FaIcons.UserTag,MaterialTheme.colors.surface, MaterialTheme.colors.onBackground)
        AnimatedVisibility(visible = showRealNameChange.value, enter = expandVertically(expandFrom = Alignment.Top), exit = shrinkVertically(shrinkTowards = Alignment.Top)) {
            detailsChange(currentUsername)
        }
        Spacer(Modifier.padding(smallPadding))
        drawButton(function = { activity.signOut()},"Log out",FaIcons.DoorOpen,MaterialTheme.colors.surface, MaterialTheme.colors.onBackground)
    }
}

fun displayChangeBox(details:Boolean){
    if (details) {
        showUsernameChange.value = !showUsernameChange.value
        showRealNameChange.value = false
    }
    else {
        showUsernameChange.value = false
        showRealNameChange.value = !showRealNameChange.value
    }
}

@Composable
fun drawButton(function:() ->Unit,text:String,icon: FaIconType?,colorText:Color, colorButton:Color){
    Button(onClick = function, background = colorButton) {
        Row {
            if (icon != null) FaIcon(faIcon = icon, tint = colorText)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, color = colorText)
        }
    }
}

@Composable
fun detailsChange(text: String?){
        Spacer(Modifier.padding(smallPadding))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize()
        ) {
            Spacer(Modifier.padding(smallPadding))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(15.dp))
                    .background(MaterialTheme.colors.onBackground)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            "Current username: ",
                            color = MaterialTheme.colors.surface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )

                        text?.let {
                            Text(
                                text,
                                color = MaterialTheme.colors.surface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                            ?: Text(
                                "No username set",
                                color = MaterialTheme.colors.surface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                    }
                    drawTextField()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        drawButton(
                            function = { /*TODO*/ },
                            text = "Set new username",
                            icon = null,
                            colorText = MaterialTheme.colors.onPrimary,
                            colorButton = MaterialTheme.colors.onSurface
                        )
                    }
                }
            }
        }
}


@Composable
fun drawTextField(){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp, start = 10.dp, end = 10.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(0.dp)) {
            val textState = remember { mutableStateOf(TextFieldValue()) }
            TextField(
                value = textState.value,
                onValueChange = { textState.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp),
                shape = RoundedCornerShape(15.dp),
                placeholder = {
                    Text(
                        "New Username",
                        color = MaterialTheme.colors.onSecondary,
                        modifier = Modifier.alpha(0.5f)
                    )
                },
                textStyle = MaterialTheme.typography.body1,
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = MaterialTheme.colors.primary,
                    disabledIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    backgroundColor = MaterialTheme.colors.secondaryVariant,
                    textColor = MaterialTheme.colors.surface
                )
            )
        }
    }
}
