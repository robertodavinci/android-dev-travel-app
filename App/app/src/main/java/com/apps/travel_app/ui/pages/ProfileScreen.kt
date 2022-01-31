package com.apps.travel_app.ui.pages

import FaIcons
import android.content.Context
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import com.apps.travel_app.MainActivity
import com.apps.travel_app.R
import com.apps.travel_app.models.updateNightMode
import com.apps.travel_app.models.updateNotifications
import com.apps.travel_app.models.updateUserInfo
import com.apps.travel_app.models.updateUserRealcredentials
import com.apps.travel_app.ui.components.Button
import com.apps.travel_app.ui.components.Heading
import com.apps.travel_app.ui.components.NiceSwitch
import com.apps.travel_app.ui.components.NiceSwitchStates
import com.apps.travel_app.ui.theme.cardPadding
import com.apps.travel_app.ui.theme.followSystem
import com.apps.travel_app.ui.theme.smallPadding
import com.apps.travel_app.ui.theme.textNormal
import com.apps.travel_app.user
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.messaging.FirebaseMessaging
import com.guru.fontawesomecomposelib.FaIcon
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
    val currentRealName: String? = user.realName
    val currentRealSurname: String? = user.realSurname
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(cardPadding)
    ) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
        Spacer(Modifier.height(cardPadding * 2))
        Heading(stringResource(R.string.settings))

        NiceSwitch(checked = followSystem.value, onChecked = {
            followSystem.value = it
            with(sharedPref.edit()) {
                putBoolean("darkTheme", it)
                apply()
            }
            updateNightMode(activity.db, user.id,it)
            //Log.i("Theme 1 - ", it.toString())
            //Log.i("Theme 2 - ", sharedPref.getBoolean("darkTheme", false).toString())
        }, states = NiceSwitchStates(T = FaIcons.MoonRegular, F = FaIcons.SunRegular), label = stringResource(
            R.string.theme))

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
            updateNotifications(activity.db, user.id,it)
        }, states =  NiceSwitchStates(FaIcons.BellRegular, FaIcons.BellSlashRegular), label = stringResource(
            R.string.receive_notifications))

        Heading(stringResource(R.string.astronaut_prefs))
        drawButton(function = { displayChangeBox(true) },stringResource(R.string.change_usr),FaIcons.User, MaterialTheme.colors.surface, MaterialTheme.colors.onBackground)
        //if (showUsernameChange.value)
        AnimatedVisibility(visible = showUsernameChange.value, enter = expandVertically(expandFrom = Alignment.Top), exit = shrinkVertically(shrinkTowards = Alignment.Top)) {
            detailsChange(false, activity)
        }
        Spacer(Modifier.padding(smallPadding))
        drawButton(function = { displayChangeBox(false) },stringResource(R.string.change_cred),FaIcons.UserTag,MaterialTheme.colors.surface, MaterialTheme.colors.onBackground)
        AnimatedVisibility(visible = showRealNameChange.value, enter = expandVertically(expandFrom = Alignment.Top), exit = shrinkVertically(shrinkTowards = Alignment.Top)) {
            detailsChange(true, activity)
        }
        Spacer(Modifier.padding(smallPadding))
        drawButton(function = { activity.signOut()},stringResource(R.string.logout),FaIcons.DoorOpen,MaterialTheme.colors.surface, MaterialTheme.colors.onBackground)
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
            Text(text, color = colorText,fontSize = textNormal)
        }
    }
}

@Composable
fun detailsChange(oneTwo:Boolean, activity:MainActivity){
    //val newUsername = remember { mutableStateOf(TextFieldValue()) }
    val newName = remember { mutableStateOf(TextFieldValue()) }
    val newSurname = remember { mutableStateOf(TextFieldValue()) }
    var text = when(oneTwo){
        false -> {
            stringResource(
                R.string.username)
        }
        true -> {
            stringResource(
                R.string.real_credentials)
        }
    }
    var textTwo = when(oneTwo){
        false -> { stringResource(R.string.new_usr) }
        true -> { stringResource(R.string.new_name) }
    }
    var textThree = remember { mutableStateOf("") }
    textThree.value = when(oneTwo){
        false -> { user.displayName + "" }
        true -> { user.realName + " " + user.realSurname }
    }
    var function: () -> Unit = {}
    function = if (oneTwo) {
        {
            updateUserRealcredentials(activity.db, user.id,name = newName.value.text, surname = newSurname.value.text)
            user.realName = newName.value.text
            user.realSurname = newSurname.value.text
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
            val editor = sharedPref.edit()
            editor.putString("realName", user.realName)
            editor.putString("realSurname", user.realSurname)
            editor.apply()
        }
    } else {
        {
            updateUserInfo(activity.db, user.id,newName.value.text)
            user.displayName = newName.value.text
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
            val editor = sharedPref.edit()
            editor.putString("displayName", user.displayName)
            editor.apply()
            val profileUpdates: UserProfileChangeRequest = UserProfileChangeRequest.Builder()
                .setDisplayName(user.displayName).build()
            activity.auth.currentUser?.updateProfile(profileUpdates)
        }
    }
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
                        stringResource(R.string.current) + " " + text,
                        fontSize = textNormal,
                        color = MaterialTheme.colors.surface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                    //if (textThree == "") textThree = "Not set"
                    textThree.let {

                        Text(
                            textThree.value!!,
                            fontSize = textNormal,
                            color = MaterialTheme.colors.surface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                drawTextField(textTwo, newName)
                if(oneTwo) drawTextField(stringResource(R.string.new_surname), newSurname)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    drawButton(
                        function = function,
                        text = stringResource(R.string.set_new) + " " + text,
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
fun drawTextField(placeholder:String, returnText: MutableState<TextFieldValue>){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp, start = 10.dp, end = 10.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)) {
            TextField(
                value = returnText.value,
                onValueChange = { returnText.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp),
                shape = RoundedCornerShape(15.dp),
                placeholder = {
                    Text(
                        placeholder,
                        fontSize = textNormal,
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