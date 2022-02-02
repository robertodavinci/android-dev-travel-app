package com.apps.travel_app.ui.pages
// Vincenzo Manto
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.apps.travel_app.MainActivity
import junit.framework.TestCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test case stimulating map drawing
 */
@RunWith(AndroidJUnit4::class)
class MapScreenTest : TestCase() {


    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun checkSelection() {
        composeTestRule.onNodeWithTag("tab1").performClick()
        Thread.sleep(1000)
        composeTestRule.onNodeWithTag("draw").performClick()
        Thread.sleep(1000)
        val device = UiDevice.getInstance(getInstrumentation())


        // It requires to disable animations by phone settings
        // + declare injection_event permission
        // For the release, this requirements have been turned off
        try {
            composeTestRule.onRoot().performGesture {
                down(Offset(50f,50f))
                moveBy(Offset(500f,500f))
                moveBy(Offset(500f,0f))
                moveBy(Offset(-500f,500f))
            }
            composeTestRule.onNodeWithTag("draw").performClick()
        }
        catch (e: Exception) {
            Log.e("TEST",e.localizedMessage)
        }


        val marker = device.findObject(UiSelector().descriptionContains("Venice"))
        marker.click()



    }





}