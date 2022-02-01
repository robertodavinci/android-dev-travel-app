package com.apps.travel_app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.apps.travel_app.ui.components.BottomNavigationBar
import com.apps.travel_app.ui.pages.ProfileScreen
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestOne {

    /*@Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>().waitForIdle()*/


    @get:Rule
    val composeRuleMain = createAndroidComposeRule(MainActivity::class.java)

    @Test
    fun testProfileScreen() {
        composeRuleMain.setContent { BottomNavigationBar(navController = composeRuleMain.activity.navController,
            mainActivity = composeRuleMain.activity)
        }
       // composeRuleMain.onNode().printToLog("currentLabelExists")

        //Thread.sleep(5000)
        composeRuleMain.onNodeWithText("General settings").assertIsSelected()
    }
    /*@Test
    fun testProfileScreenTwo(){
        composeRuleMain.setContent {
            ProfileScreen(activity = composeRuleMain.activity)

        }
        composeRuleMain.onAllNodes()
    }*/




    /*@Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        Assert.assertEquals("com.apps.travel_app", appContext.packageName)
    }*/
}