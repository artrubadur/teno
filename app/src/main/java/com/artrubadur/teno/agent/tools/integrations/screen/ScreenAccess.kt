package com.artrubadur.teno.agent.tools.integrations.screen

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.view.accessibility.AccessibilityEvent

@SuppressLint("AccessibilityPolicy")
class ScreenAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        ScreenAccessibilityBridge.connect(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        ScreenAccessibilityBridge.disconnect(this)
        super.onDestroy()
    }
}

object ScreenAccessibilityBridge {
    @Volatile
    var service: ScreenAccessibilityService? = null
        private set

    fun connect(service: ScreenAccessibilityService) {
        this.service = service
    }

    fun disconnect(service: ScreenAccessibilityService) {
        if (this.service != service) return
        this.service = null
    }
}
