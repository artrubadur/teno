package com.artrubadur.teno.ui.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.artrubadur.teno.MainActivity
import com.artrubadur.teno.R

class OverlayNotificationFactory(
    private val context: Context,
) {
    fun ensureChannel() {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Teno overlay",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    fun create(state: OverlayState): Notification {
        val views = RemoteViews(context.packageName, R.layout.notification_overlay_controls).apply {
            setImageViewResource(
                R.id.overlay_notification_action_button,
                when {
                    state.isWorking -> R.drawable.ic_stop
                    else -> R.drawable.ic_keyboard
                }
            )
            setImageViewResource(
                R.id.overlay_notification_lifecycle_button,
                when {
                    state.isLoading -> R.drawable.ic_hourglass
                    state.isReady -> R.drawable.ic_stop
                    else -> R.drawable.ic_launch
                }
            )

            setContentDescription(
                R.id.overlay_notification_action_button,
                when {
                    state.isWorking -> "Stop agent"
                    else -> "Open input"
                }
            )
            setContentDescription(
                R.id.overlay_notification_lifecycle_button,
                when {
                    state.isLoading -> "Agent is loading"
                    state.isReady -> "Terminate agent"
                    else -> "Launch agent"
                }
            )

            setOnClickPendingIntent(
                R.id.overlay_notification_action_button,
                when {
                    state.isWorking -> serviceIntent(OverlayForegroundService.ACTION_STOP, 1)
                    else -> serviceIntent(OverlayForegroundService.ACTION_INPUT, 2)
                }

            )
            setOnClickPendingIntent(
                R.id.overlay_notification_lifecycle_button,
                when {
                    state.isReady -> serviceIntent(OverlayForegroundService.ACTION_TERMINATE, 3)
                    else -> serviceIntent(OverlayForegroundService.ACTION_LAUNCH, 4)
                }
            )
            setOnClickPendingIntent(
                R.id.overlay_notification_shutdown_button,
                serviceIntent(OverlayForegroundService.ACTION_SHUTDOWN, 5)
            )

            setBoolean(
                R.id.overlay_notification_lifecycle_button,
                "setEnabled",
                !state.isLoading
            )
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_keyboard)
            .setOngoing(true)
            .setAutoCancel(false)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setLocalOnly(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCustomContentView(views)
            .setCustomBigContentView(views)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentIntent(activityIntent())
            .setDeleteIntent(serviceIntent(OverlayForegroundService.ACTION_START, 7))
            .build()
    }

    private fun serviceIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, OverlayForegroundService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun activityIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            6,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val CHANNEL_ID = "teno_overlay"
        const val NOTIFICATION_ID = 1001
    }
}
