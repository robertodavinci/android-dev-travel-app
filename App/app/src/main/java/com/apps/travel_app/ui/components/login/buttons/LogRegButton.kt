package com.apps.travel_app.ui.components.login

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.apps.travel_app.R
import com.apps.travel_app.ui.theme.textNormal
import com.facebook.Profile

@Composable
fun LogRegButton(profile: Profile?, login: () -> Unit, logout: () -> Unit) {
    val buttonText = if (profile == null) {
        stringResource(R.string.continue_fb)
    } else {
        stringResource(R.string.logout)
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