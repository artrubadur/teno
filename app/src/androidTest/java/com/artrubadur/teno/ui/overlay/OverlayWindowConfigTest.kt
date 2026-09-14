package com.artrubadur.teno.ui.overlay

import android.view.WindowManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayWindowConfigTest {
    @Test
    fun accessibilityOverlayPassesInputThroughWhileAgentWorks() {
        assertEquals(
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            overlayWindowType(accessibilityServiceAvailable = true)
        )

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

    @Test
    fun applicationOverlayRemainsAvailableWithoutAccessibilityService() {
        assertEquals(
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            overlayWindowType(accessibilityServiceAvailable = false)
        )
    }

    private infix fun Int.has(flag: Int): Boolean = this and flag != 0
}
