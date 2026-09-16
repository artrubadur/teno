package com.artrubadur.teno.ui.overlay

import android.Manifest
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.artrubadur.teno.agent.controller.AgentControllerEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class OverlayForegroundService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private lateinit var controller: OverlayController
    private lateinit var notificationFactory: OverlayNotificationFactory
    private val stateRequestReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != ACTION_REQUEST_STATE) return
            publishRunningState(true)
        }
    }
    private var applicationWindowManager: WindowManager? = null
    private var applicationOverlayView: OverlayHostView? = null

    override fun onCreate() {
        super.onCreate()
        ContextCompat.registerReceiver(
            this,
            stateRequestReceiver,
            IntentFilter(ACTION_REQUEST_STATE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        notificationFactory = OverlayNotificationFactory(this)
        notificationFactory.ensureChannel()
        controller = OverlayController(scope, application)

        showForegroundNotification()
        publishRunningState(true)

        controller.state
            .map { state ->
                NotificationState(
                    isReady = state.isReady,
                    isLoading = state.isLoading,
                    isWorking = state.isWorking,
                    controllerEvents = state.controllerEvents,
                )
            }
            .distinctUntilChanged()
            .onEach { updateNotification(controller.state.value) }
            .launchIn(scope)

        controller.state
            .map { state -> state.isOverlayVisible }
            .distinctUntilChanged()
            .onEach(::syncOverlay)
            .launchIn(scope)

        controller.state
            .map { state -> state.isWorking }
            .distinctUntilChanged()
            .onEach { isWorking -> if (!isWorking) syncOverlayMode(false) }
            .launchIn(scope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                showForegroundNotification()
                controller.onShowIsland(false)
            }

            ACTION_INPUT -> controller.onOpenInput()
            ACTION_STOP -> controller.stopWork()
            ACTION_LAUNCH -> controller.launchActiveConnection()
            ACTION_TERMINATE -> controller.terminateConnection()
            ACTION_SHUTDOWN -> {
                controller.terminateConnection()
                stopSelf()
            }

            else -> Unit
        }

        return START_STICKY
    }

    override fun onDestroy() {
        publishRunningState(false)
        removeOverlay()
        if (::controller.isInitialized) {
            controller.terminateConnection()
            controller.close()
        }
        runCatching {
            unregisterReceiver(stateRequestReceiver)
        }
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun showForegroundNotification() {
        ServiceCompat.startForeground(
            this,
            OverlayNotificationFactory.NOTIFICATION_ID,
            notificationFactory.create(controller.state.value),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    private fun updateNotification(state: OverlayState) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        runCatching {
            NotificationManagerCompat.from(this).notify(
                OverlayNotificationFactory.NOTIFICATION_ID,
                notificationFactory.create(state)
            )
        }
    }

    private fun syncOverlay(isOverlayVisible: Boolean) {
        if (isOverlayVisible && Settings.canDrawOverlays(this)) {
            showApplicationOverlayIfNeeded()
        } else {
            removeOverlay()
        }
    }

    private fun showApplicationOverlayIfNeeded() {
        if (applicationOverlayView != null) return

        val view = OverlayHostView(this, controller, onCollapsed = { syncOverlayMode(true) })
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            overlayFlags(passThrough = false),
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            setFitInsetsTypes(0)
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        }

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(view, params)
        applicationWindowManager = windowManager
        view.onAttachedToWindowManager()
        applicationOverlayView = view
    }

    private fun syncOverlayMode(isWorking: Boolean) {
        if (isWorking && !controller.state.value.isWorking) return
        val view = applicationOverlayView ?: return
        val params = view.layoutParams as? WindowManager.LayoutParams ?: return
        val flags = overlayFlags(passThrough = isWorking)
        if (params.flags != flags) {
            params.flags = flags
            runCatching {
                applicationWindowManager?.updateViewLayout(view, params)
            }.getOrElse { return }
        }
        if (isWorking) {
            view.postOnAnimation {
                view.post { controller.onOverlayCollapsed() }
            }
        }
    }

    private fun removeOverlay() {
        val view = applicationOverlayView ?: return
        runCatching {
            applicationWindowManager?.removeView(view)
        }
        view.onDetachedFromWindowManager()
        applicationOverlayView = null
        applicationWindowManager = null
    }

    private fun publishRunningState(isRunning: Boolean) {
        sendBroadcast(
            Intent(ACTION_STATE)
                .setPackage(packageName)
                .putExtra(EXTRA_IS_RUNNING, isRunning)
        )
    }

    companion object {
        private const val ACTION_REQUEST_STATE =
            "com.artrubadur.teno.overlay.REQUEST_STATE"
        const val ACTION_STATE = "com.artrubadur.teno.overlay.STATE"
        const val EXTRA_IS_RUNNING = "com.artrubadur.teno.overlay.extra.IS_RUNNING"

        const val ACTION_START = "com.artrubadur.teno.overlay.START"
        const val ACTION_INPUT = "com.artrubadur.teno.overlay.INPUT"
        const val ACTION_STOP = "com.artrubadur.teno.overlay.STOP"
        const val ACTION_LAUNCH = "com.artrubadur.teno.overlay.LAUNCH"
        const val ACTION_TERMINATE = "com.artrubadur.teno.overlay.TERMINATE"
        const val ACTION_SHUTDOWN = "com.artrubadur.teno.overlay.SHUTDOWN"

        fun start(context: Context) {
            val intent = Intent(context, OverlayForegroundService::class.java).apply {
                action = ACTION_START
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun shutdown(context: Context) {
            context.stopService(Intent(context, OverlayForegroundService::class.java))
        }

        fun requestRunningState(context: Context) {
            context.sendBroadcast(
                Intent(ACTION_REQUEST_STATE)
                    .setPackage(context.packageName)
            )
        }
    }
}

internal fun overlayFlags(passThrough: Boolean): Int {
    var flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
    if (passThrough) {
        flags = flags or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
    }
    return flags
}

private data class NotificationState(
    val isReady: Boolean,
    val isLoading: Boolean,
    val isWorking: Boolean,
    val controllerEvents: List<AgentControllerEvent>,
)
