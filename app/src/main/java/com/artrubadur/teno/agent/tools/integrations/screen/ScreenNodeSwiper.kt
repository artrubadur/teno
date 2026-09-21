package com.artrubadur.teno.agent.tools.integrations.screen

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.graphics.PointF
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.coroutines.resume

@Serializable
enum class ScreenSwipeDirection {
    @SerialName("left")
    LEFT,

    @SerialName("right")
    RIGHT,

    @SerialName("up")
    UP,

    @SerialName("down")
    DOWN,
}

class ScreenNodeSwiper(
    private val context: Context,
    private val reader: ScreenTreeReader,
) {
    suspend fun swipe(
        reference: ScreenNodeReference?,
        direction: ScreenSwipeDirection,
    ) {
        val node = reference?.let { reader.find(it) }
        val metrics = context.resources.displayMetrics
        val bounds = node?.bounds
        val center = PointF(
            bounds?.exactCenterX() ?: (metrics.widthPixels / 2f),
            bounds?.exactCenterY() ?: (metrics.heightPixels / 2f),
        )
        val distance = when (direction) {
            ScreenSwipeDirection.LEFT, ScreenSwipeDirection.RIGHT -> bounds?.width()?.div(2f)
                ?: (metrics.widthPixels / 2f)

            ScreenSwipeDirection.UP, ScreenSwipeDirection.DOWN -> bounds?.height()?.div(2f)
                ?: (metrics.heightPixels / 2f)
        }
        val end = when (direction) {
            ScreenSwipeDirection.LEFT -> PointF(center.x - distance, center.y)
            ScreenSwipeDirection.RIGHT -> PointF(center.x + distance, center.y)
            ScreenSwipeDirection.UP -> PointF(center.x, center.y - distance)
            ScreenSwipeDirection.DOWN -> PointF(center.x, center.y + distance)
        }

        val service = ScreenAccessibilityBridge.service
            ?: error("Accessibility service is unavailable")
        val path = Path().apply {
            moveTo(center.x, center.y)
            lineTo(end.x, end.y)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 300))
            .build()

        suspendCancellableCoroutine { continuation ->
            val dispatched = service.dispatchGesture(
                gesture,
                object : AccessibilityService.GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription) {
                        continuation.resume(Unit)
                    }

                    override fun onCancelled(gestureDescription: GestureDescription) {
                        continuation.cancel()
                    }
                },
                null,
            )
            if (!dispatched) continuation.cancel()
        }
    }
}
