package com.apps.travel_app.ui.pages
// Vincenzo Manto
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.apps.travel_app.R
import junit.framework.TestCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test case involving creation (with failure and success) of trips
 */
@RunWith(AndroidJUnit4::class)
class TripCreationActivityTest : TestCase() {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<TripCreationActivity>()

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun create() {
        composeTestRule.onNodeWithTag("name").performTextInput("Nome")
        composeTestRule.onNodeWithTag("description").performTextInput("Description")
        val tag = composeTestRule.onNodeWithTag("tag")
        tag.performTextInput("tag1")
        tag.performTextInput(",")
        tag.performTextInput("tag2")
        tag.performTextInput(",")
        composeTestRule.onNodeWithTag("main").performClick()
        Thread.sleep(1000)
        composeTestRule.onNodeWithTag("searchText").performTextInput("verona")
        composeTestRule.onNodeWithTag("searchText").performImeAction()
        Thread.sleep(4000)
        composeTestRule.onAllNodes(hasText("Verona",true,true)).onFirst().assertExists()
        val addStep = composeTestRule.onAllNodesWithTag("add").onFirst()
        addStep.assertExists()
        addStep.performClick()
        val setMain = composeTestRule.onAllNodesWithTag("main").onFirst()
        setMain.assertExists()
        setMain.performClick()
        composeTestRule.onNodeWithTag("back").performClick()
        Thread.sleep(500)
        val nodes = composeTestRule.onAllNodes(hasText("verona",true,true))
        assertNotNull(nodes)
        nodes.assertCountEquals(2)
        composeTestRule.onNodeWithTag("confirm").performClick()
        Thread.sleep(2000)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {


            val activity =
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(
                    Stage.DESTROYED
                ).first()

            assertTrue(activity is TripCreationActivity)

        }
    }

    @Test
    fun creationFailure() {
        for (i in 0..3) {
            if (i == 0)
                composeTestRule.onNodeWithTag("name").performTextInput("Nome")
            if (i == 1)
                composeTestRule.onNodeWithTag("description").performTextInput("Description")
            if (i == 2) {
                val tag = composeTestRule.onNodeWithTag("tag")
                tag.performTextInput("tag1")
                tag.performTextInput(",")
                tag.performTextInput("tag2")
                tag.performTextInput(",")
            }
            if (i == 3) {
                composeTestRule.onNodeWithTag("main").performClick()
                Thread.sleep(1000)
                composeTestRule.onNodeWithTag("searchText").performTextInput("verona")
                composeTestRule.onNodeWithTag("searchText").performImeAction()
                Thread.sleep(4000)
                composeTestRule.onAllNodes(hasText("Verona", true, true)).onFirst().assertExists()
                val addStep = composeTestRule.onAllNodesWithTag("add").onFirst()
                addStep.assertExists()
                addStep.performClick()
                val setMain = composeTestRule.onAllNodesWithTag("main").onFirst()
                setMain.assertExists()
                setMain.performClick()
                composeTestRule.onNodeWithTag("back").performClick()
                Thread.sleep(500)
                val nodes = composeTestRule.onAllNodes(hasText("verona", true, true))
                assertNotNull(nodes)
                nodes.assertCountEquals(2)
            }
            composeTestRule.onNodeWithTag("confirm").performClick()
            Thread.sleep(1000)
            val text = composeTestRule.activity.getString(R.string.not_enough_info)
            if (i != 3)
                onView(withText(text)).check(matches(isDisplayed()))
            Thread.sleep(2000)
        }

    }
}