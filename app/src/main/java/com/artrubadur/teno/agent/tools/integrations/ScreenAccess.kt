package com.artrubadur.teno.agent.tools.integrations

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.view.accessibility.AccessibilityEvent
import java.util.concurrent.CopyOnWriteArraySet

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

    private val listeners = CopyOnWriteArraySet<() -> Unit>()

    fun connect(service: ScreenAccessibilityService) {
        this.service = service
        listeners.forEach { it() }
    }

    fun disconnect(service: ScreenAccessibilityService) {
        if (this.service != service) return
        this.service = null
        listeners.forEach { it() }
    }

    fun addListener(listener: () -> Unit) {
        listeners += listener
    }

    fun removeListener(listener: () -> Unit) {
        listeners -= listener
    }
}
