package com.apps.travel_app.ui.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performGesture
import com.apps.travel_app.MainActivity
import com.apps.travel_app.ui.components.login.LoginActivity
import junit.framework.TestCase
import org.junit.Rule
import org.junit.Test

class MapScreenTest : TestCase() {


    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun checkSelection() {
        composeTestRule.onNodeWithTag("tab1").performClick()
        Thread.sleep(1000)
        composeTestRule.onRoot().performGesture {
            
        }
    }

}