package com.apps.travel_app

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apps.travel_app.ui.components.login.LoginActivity
import com.apps.travel_app.ui.pages.HomeScreen
import com.apps.travel_app.ui.theme.Travel_AppTheme
import com.apps.travel_app.ui.theme.primaryColor
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runner.manipulation.Ordering

@RunWith(AndroidJUnit4::class)
class Tests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @ExperimentalMaterialApi
    @ExperimentalAnimationApi
    @ExperimentalFoundationApi
    @Before
    fun setUp(){
        composeTestRule.setContent { LoginActivity::class }
        //thread.
    }

    /* @Before
    fun setUp(){
        composeTestRule.setContent {
            HomeScreen(navController = composeTestRule.activity.navController, mainActivity = )
            Travel_AppTheme(true) {
            composeTestRule.activity.MainScreen(context = composeTestRule.activity, activity = composeTestRule.activity)
        } }
    }

    @Test
    fun app_launches() {
        // Check app launches at the correct destination
        composeTestRule.onNodeWithText("Trips").assertIsDisplayed()
        //composeTestRule.onNodeWithText("Android's picks").assertIsDisplayed()
    }*/



}