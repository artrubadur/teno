package com.artrubadur.tonemo.overlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager

class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: OverlayView? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopSelf()
            else -> showOverlayIfNeeded()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        removeOverlay()
        isRunning = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun showOverlayIfNeeded() {
        if (overlayView != null) return

        val wm = windowManager ?: (getSystemService(WINDOW_SERVICE) as WindowManager).also {
            windowManager = it
        }

        val view = OverlayView(this)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 0
        }

        wm.addView(view, params)
        view.onAttachedToWindowManager()
        overlayView = view
    }

    private fun removeOverlay() {
        val view = overlayView ?: return
        runCatching {
            windowManager?.removeView(view)
        }
        view.onDetachedFromWindowManager()
        overlayView = null
    }
    companion object {
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