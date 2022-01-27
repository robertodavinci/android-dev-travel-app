package com.apps.travel_app.ui.components.login

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.apps.travel_app.ui.theme.textNormal
import com.facebook.Profile

@Composable
fun LogRegButton(profile: Profile?, login: () -> Unit, logout: () -> Unit) {
    val buttonText = if (profile == null) {
        "Continue with Facebook"
    } else {
        "Log out"
    }
    val onClick = if (profile == null) {
        login
    } else {
        logout
    }
    Button(
        onClick = {
            onClick
        }
    ) {
        Text(
            text = buttonText,
            fontSize = textNormal
        )
    }
}