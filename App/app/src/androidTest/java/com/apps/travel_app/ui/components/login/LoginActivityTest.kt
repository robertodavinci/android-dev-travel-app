package com.apps.travel_app.ui.components.login

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.content.ContextCompat.startActivity
import com.apps.travel_app.MainActivity
import org.junit.Assert.*
import org.junit.Rule

import org.junit.Test

class LoginActivityTest {

    @ExperimentalMaterialApi
    @ExperimentalAnimationApi
    @ExperimentalFoundationApi
    @get:Rule
    val composeTestRule = createAndroidComposeRule<LoginActivity>()

    @ExperimentalFoundationApi
    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    @Test
    fun finalLoginPart() {

        //if(inte != null) intent.putExtra("findTripID", inte)
        //startActivity(Intent)
    }

    @Test
    fun loginAndRegistrationUI() {
    }

    @Test
    fun emailPassScreen() {
    }

    @Test
    fun sharedPrefUserEdit() {
    }

    @Test
    fun linkAccount() {
    }
}