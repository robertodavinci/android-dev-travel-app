package com.apps.travel_app

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.apps.travel_app.ui.components.login.LoginActivity
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class LoginTest : TestCase() {

    @OptIn(
        ExperimentalFoundationApi::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class,
        androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class
    )
    @get:Rule
    val composeTestRule = createAndroidComposeRule<LoginActivity>()


    @OptIn(
        ExperimentalCoroutinesApi::class,
        androidx.compose.foundation.ExperimentalFoundationApi::class,
        androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class
    )
    @Test
    fun checkLogin() {
        val login = composeTestRule.onNodeWithTag("login")
        val usr = composeTestRule.onNodeWithTag("username")
        val pwd = composeTestRule.onNodeWithTag("password")
            usr.performTextInput("vins@vins.com")

            pwd.performTextInput("vins")

            login.performClick()

        composeTestRule.onNode(hasText("Authentication failed"))

        assertFalse(composeTestRule.activity.isNewUser ?: false)

        pwd.performTextClearance()

        pwd.performTextInput("vinsvins")

        login.performClick()


        Thread.sleep(5000)
        getInstrumentation().runOnMainSync {


            val activity =
                ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(
                    Stage.RESUMED
                ).first()

            assertTrue(activity is MainActivity)

        }


    }
}