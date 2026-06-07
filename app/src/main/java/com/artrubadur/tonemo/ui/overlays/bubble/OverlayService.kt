package com.artrubadur.tonemo.ui.overlays.bubble

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.mutableStateOf
import kotlin.math.roundToInt

class OverlayService : Service() {

    private var windowManager: WindowManager? = null

    private var overlayView: OverlayView? = null

    private var bubbleView: BubbleView? = null

    private var bubbleInputView: BubbleInputView? = null

    private val expandedState = mutableStateOf(false)

    private val onExpand = {
        showOverlayIfNeeded()
        // Expand bubble input frame
        setBubbleInputFrame(expandedBubbleInputFrame())
        expandedState.value = true
    }

    private val onCollapse = {
        expandedState.value = false
    }

    private val onOverlayCollapseEnd = {
        hideOverlay()
    }

    private val onBubbleCollapseAnimationEnd = {
        hideOverlay()
        // Collapse bubble input frame
        setBubbleInputFrame(collapsedBubbleInputFrame())
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopSelf()

            else -> {
                showOverlayIfNeeded()
                hideOverlay()

                showBubbleIfNeeded()
                showBubbleInputIfNeeded()
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        removeBubbleInput()
        removeBubble()
        removeOverlay()

        isRunning = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun showOverlayIfNeeded() {
        overlayView?.let {
            it.visibility = View.VISIBLE
            return
        }

        val wm = getWindowManager()

        val view = OverlayView(
            context = this,
            expandedState = expandedState,
            onCollapseEnd = onOverlayCollapseEnd,
            onStop = { stopSelf() }
        )

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            INPUT_WINDOW_FLAGS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        }

        wm.addView(view, params)
        view.onAttachedToWindowManager()

        overlayView = view
    }

    private fun showBubbleIfNeeded() {
        if (bubbleView != null) return

        val wm = getWindowManager()

        val view = BubbleView(
            context = this,
            expandedState = expandedState,
            onCollapseAnimationEnd = onBubbleCollapseAnimationEnd
        )

        val params = WindowManager.LayoutParams(
            56.dpPx(),
            60.dpPx(),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    INPUT_WINDOW_FLAGS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 8.dpPx()
        }

        wm.addView(view, params)
        view.onAttachedToWindowManager()

        bubbleView = view
    }

    private fun showBubbleInputIfNeeded() {
        if (bubbleInputView != null) return

        val wm = getWindowManager()

        val view = BubbleInputView(
            context = this,
            expandedState = expandedState,
            onExpand = onExpand,
            onCollapse = onCollapse
        )

        val frame = collapsedBubbleInputFrame()

        val params = WindowManager.LayoutParams(
            frame.width,
            frame.height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            INPUT_WINDOW_FLAGS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 8.dpPx()
        }

        wm.addView(view, params)
        bubbleInputView = view
    }

    private fun hideOverlay() {
        overlayView?.visibility = View.GONE
    }

    private fun removeBubble() {
        val view = bubbleView ?: return

        removeViewSafely(view)

        view.onDetachedFromWindowManager()

        bubbleView = null
    }

    private fun removeBubbleInput() {
        val view = bubbleInputView ?: return

        removeViewSafely(view)

        bubbleInputView = null
    }

    private fun removeOverlay() {
        val view = overlayView ?: return

        removeViewSafely(view)

        view.onDetachedFromWindowManager()

        overlayView = null
    }

    private fun removeViewSafely(view: View) {
        runCatching {
            windowManager?.removeView(view)
        }
    }

    private fun getWindowManager(): WindowManager {
        return windowManager
            ?: (getSystemService(WINDOW_SERVICE) as WindowManager).also {
                windowManager = it
            }
    }

    private data class BubbleInputFrame(
        val width: Int,
        val height: Int,
    )

    private fun collapsedBubbleInputFrame(): BubbleInputFrame {
        return BubbleInputFrame(
            width = 28.dpPx(),
            height = 28.dpPx(),
        )
    }

    private fun expandedBubbleInputFrame(): BubbleInputFrame {
        return BubbleInputFrame(
            width = 56.dpPx(),
            height = 60.dpPx(),
        )
    }

    private fun setBubbleInputFrame(frame: BubbleInputFrame) {
        val view = bubbleInputView ?: return
        val wm = windowManager ?: return

        val params = WindowManager.LayoutParams(
            frame.width,
            frame.height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            INPUT_WINDOW_FLAGS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 8.dpPx()
        }

        runCatching {
            wm.updateViewLayout(view, params)
        }
    }

    private fun Int.dpPx(): Int {
        return (this * resources.displayMetrics.density).roundToInt()
    }

    companion object {
        private const val INPUT_WINDOW_FLAGS =
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

        private const val ACTION_START = "com.artrubadur.tonemo.overlay.START"
        private const val ACTION_STOP = "com.artrubadur.tonemo.overlay.STOP"

        @Volatile
        var isRunning: Boolean = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_START
            }

            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_STOP
            }

            context.startService(intent)
        }
    }
}
