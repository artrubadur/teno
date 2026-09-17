package com.artrubadur.teno.ui.onboarding

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.artrubadur.teno.App
import com.artrubadur.teno.ui.overlays.onboarding.LocalTourTargets
import com.artrubadur.teno.ui.overlays.onboarding.OnboardingOverlay
import com.artrubadur.teno.ui.overlays.onboarding.TourTargets
import com.artrubadur.teno.ui.overlays.onboarding.tourSteps
import com.artrubadur.teno.ui.overlays.onboarding.tourTarget
import com.artrubadur.teno.ui.theme.AppTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingTest {
    @get:Rule
    val composeRule = createComposeRule()
    private val preferences = ApplicationProvider.getApplicationContext<Context>()
        .getSharedPreferences("onboarding", Context.MODE_PRIVATE)
    private var wasCompleted = false

    @Before
    fun resetCompletion() {
        wasCompleted = preferences.getBoolean("completed", false)
        preferences.edit().putBoolean("completed", false).commit()
    }

    @After
    fun restoreCompletion() {
        preferences.edit().putBoolean("completed", wasCompleted).commit()
    }

    @Test
    fun firstRunSupportsSpotlightBackSkipPersistenceAndRestart() {
        val generation = mutableIntStateOf(0)
        composeRule.setContent {
            AppTheme { key(generation.intValue) { App() } }
        }
        composeRule.onNodeWithText("Welcome to Teno").assertIsDisplayed()
        composeRule.onNodeWithText("Next").performClick()
        composeRule.onNodeWithText("Choose your model").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Connections").performClick()
        composeRule.onNodeWithText("3 / ${tourSteps.size}").assertIsDisplayed()
        composeRule.onNodeWithText("Back").performClick()
        composeRule.onNodeWithText("Choose your model").assertIsDisplayed()
        composeRule.onNodeWithText("Skip").performClick()
        composeRule.onNodeWithText("Welcome to Teno").assertDoesNotExist()
        composeRule.runOnIdle { assertTrue(preferences.getBoolean("completed", false)) }
        composeRule.runOnIdle { generation.intValue++ }
        composeRule.onNodeWithText("Welcome to Teno").assertDoesNotExist()
        composeRule.onNodeWithText("How it works").performScrollTo().performClick()
        composeRule.onNodeWithText("Take a tour").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Welcome to Teno").assertIsDisplayed()
    }

    @Test
    fun nextCanCompleteTourWithoutConnectionPermissionsOrOpeningShade() {
        composeRule.setContent { AppTheme { App() } }
        repeat(tourSteps.lastIndex) { index ->
            tourSteps[index].target?.let { target ->
                composeRule.onNodeWithContentDescription("Highlighted $target button")
                    .assertHasClickAction()
            }
            composeRule.onNodeWithText("Next").performClick()
        }
        composeRule.onNodeWithText("You're ready").assertIsDisplayed()
        composeRule.onNodeWithText("Finish").performClick()
        composeRule.onNodeWithText("You're ready").assertDoesNotExist()
        composeRule.runOnIdle { assertTrue(preferences.getBoolean("completed", false)) }
    }

    @Test
    fun finishCompletesAfterAnimationWithoutAdvancingStep() {
        var completed = 0
        var advanced = 0
        composeRule.setContent {
            AppTheme {
                OnboardingOverlay(
                    tourSteps.lastIndex,
                    TourTargets(),
                    { advanced++ },
                    {},
                    { completed++ })
            }
        }
        composeRule.waitForIdle()
        composeRule.mainClock.autoAdvance = false
        composeRule.onNodeWithText("Finish").performClick()
        composeRule.mainClock.advanceTimeBy(150)
        composeRule.runOnIdle { assertEquals(0, completed); assertEquals(0, advanced) }
        composeRule.mainClock.advanceTimeBy(300)
        composeRule.runOnIdle { assertEquals(1, completed); assertEquals(0, advanced) }
    }

    @Test
    fun spotlightBlocksBackgroundAndActivatesHighlightedTarget() {
        var activated = 0
        var advanced = 0
        val targets = TourTargets()
        composeRule.setContent {
            AppTheme {
                CompositionLocalProvider(LocalTourTargets provides targets) {
                    Box(Modifier.fillMaxSize()) {
                        Button(
                            onClick = { activated++ },
                            modifier = Modifier.tourTarget("connections") { activated++ }) {
                            Text("Actual target")
                        }
                        OnboardingOverlay(1, targets, { advanced++ }, {}, {})
                    }
                }
            }
        }
        val background = composeRule.onNodeWithContentDescription("Highlighted connections button")
        background.performTouchInput { click(center) }
        composeRule.runOnIdle { assertEquals(0, activated); assertEquals(0, advanced) }
        val position =
            composeRule.onNodeWithText("Actual target").fetchSemanticsNode().boundsInRoot.center
        composeRule.onRoot().performTouchInput { click(position) }
        composeRule.runOnIdle { assertEquals(1, activated); assertEquals(1, advanced) }
    }
}
