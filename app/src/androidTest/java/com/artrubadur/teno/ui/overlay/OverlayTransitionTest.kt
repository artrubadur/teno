package com.artrubadur.teno.ui.overlay

import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.artrubadur.teno.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OverlayTransitionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun collapseSignalsOnceAfterTheSharedAnimation() {
        var state by mutableStateOf(OverlayState(isOverlayVisible = true))
        var collapsed = 0
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            AppTheme {
                OverlayView(
                    state = state,
                    onInputChanged = {}, onSend = {}, onStopWork = {},
                    onLaunchActiveConnection = {},
                    onApproveConfirmation = {}, onRejectConfirmation = {},
                    onOutsideClick = {}, onIslandHidden = {},
                    onCollapsed = { collapsed++ },
                )
            }
        }
        composeRule.mainClock.advanceTimeBy(300)
        composeRule.runOnUiThread { state = state.copy(isWorking = true) }
        composeRule.mainClock.advanceTimeBy(100)
        composeRule.runOnIdle { assertEquals(0, collapsed) }
        composeRule.mainClock.advanceTimeBy(400)
        composeRule.runOnIdle { assertEquals(1, collapsed) }
        composeRule.mainClock.advanceTimeBy(400)
        composeRule.runOnIdle { assertEquals(1, collapsed) }
    }

    @Test
    fun workingOverlayPassesInputThrough() {
        val flags = overlayFlags(passThrough = true)
        assertTrue(flags has WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        assertTrue(flags has WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
    }

    @Test
    fun interactiveOverlayRemainsTouchableAndFocusable() {
        val flags = overlayFlags(passThrough = false)
        assertFalse(flags has WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        assertFalse(flags has WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
    }

    private infix fun Int.has(flag: Int): Boolean = this and flag != 0
}
