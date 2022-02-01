package com.apps.travel_app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.apps.travel_app.ui.pages.HomeScreen
import com.apps.travel_app.ui.theme.MainActivity_Travel_AppTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ComposeTests {

    private lateinit var navController: TestNavHostController

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp(){
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun testIfTitleExists(){
        composeTestRule.setContent {
            //MainActivity().navController.

            MainActivity_Travel_AppTheme(true) {


            }
        }
        composeTestRule.onNodeWithText("Top destinations").assertIsDisplayed()
    }

}