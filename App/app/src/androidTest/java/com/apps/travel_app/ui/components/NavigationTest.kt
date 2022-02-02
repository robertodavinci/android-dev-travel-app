package com.apps.travel_app.ui.components
// Vincenzo Manto
import android.util.Log
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.apps.travel_app.MainActivity
import com.apps.travel_app.ui.pages.AroundMeActivity
import com.apps.travel_app.ui.pages.InspirationActivity
import junit.framework.TestCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random.Default.nextInt

/**
 * This is a test case related to navigation through bottom bar
 */
@RunWith(AndroidJUnit4::class)
class NavigationTest : TestCase() {


    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun navigation() {
        for (i in 0..6) {
            composeTestRule.onNodeWithTag("tab" + nextInt(0, 4)).performClick()
            Thread.sleep(3000)
        }
        composeTestRule.onNodeWithTag("tab4").performClick()
        assertNotNull(composeTestRule.onNode(hasText("Astro",true,true)))
        composeTestRule.onAllNodesWithTag("positive").onFirst().performClick()
        Thread.sleep(2000)
        composeTestRule.onAllNodesWithTag("negative").onFirst().performClick()

        composeTestRule.onNodeWithTag("tab3").performClick()

        composeTestRule.onNodeWithTag("around").performClick()
        Thread.sleep(3000)
        getInstrumentation().runOnMainSync {


            val activity =
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(
                    Stage.RESUMED
                ).first()

            assertTrue(activity is AroundMeActivity) // seems to not work in Debug test mode
            // works in release

        }

    }

}