package com.artrubadur.teno.ui.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.artrubadur.teno.MainActivity
import com.artrubadur.teno.R
import com.artrubadur.teno.agent.controller.AgentControllerEvent
import com.artrubadur.teno.agent.orchestration.AgentEvent
import java.util.Locale

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
        val workAction = if (state.isWorking) {
            notificationAction(
                titleRes = R.string.notification_action_stop,
                intent = serviceIntent(OverlayForegroundService.ACTION_STOP, 1)
            )
        } else {
            notificationAction(
                titleRes = R.string.notification_action_input_short,
                intent = serviceIntent(OverlayForegroundService.ACTION_INPUT, 2)
            )
        }
        val lifecycleAction = if (state.isLoading || state.isReady) {
            notificationAction(
                titleRes = R.string.notification_action_terminate_agent,
                intent = serviceIntent(OverlayForegroundService.ACTION_TERMINATE, 3)
            )
        } else {
            notificationAction(
                titleRes = R.string.notification_action_launch_agent,
                intent = serviceIntent(OverlayForegroundService.ACTION_LAUNCH, 4)
            )
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_keyboard)
            .setContentTitle("")
            .setContentText(agentStatus(state))
            .setOngoing(true)
            .setAutoCancel(false)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setLocalOnly(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setContentIntent(activityIntent())
            .setDeleteIntent(serviceIntent(OverlayForegroundService.ACTION_START, 7))
            .addAction(workAction)
            .addAction(lifecycleAction)
            .addAction(
                notificationAction(
                    titleRes = R.string.notification_action_close,
                    intent = serviceIntent(OverlayForegroundService.ACTION_SHUTDOWN, 5)
                )
            )
            .build()
    }

    private fun agentStatus(state: OverlayState): String {
        val events = state.controllerEvents
        val terminal = events.asReversed().firstNotNullOfOrNull { event ->
            when (event) {
                is AgentControllerEvent.Message if event.message == "Stopped" -> "Stopped"
                is AgentControllerEvent.Message -> "Error"
                is AgentControllerEvent.Agent if event.event is AgentEvent.FinalAnswer -> "Done"
                is AgentControllerEvent.Agent if event.event is AgentEvent.Failed -> "Error"
                else -> null
            }
        }
        if (terminal != null && !state.isWorking) return terminal

        val tool = events.asReversed().firstNotNullOfOrNull { event ->
            (event as? AgentControllerEvent.Agent)
                ?.event
                ?.toolName()
        }
        if (tool != null) return "Calling ${tool.toStatusName()}"

        return when {
            state.isWorking -> "Working"
            state.isReady -> "Idle"
            else -> "Inactive"
        }
    }

    private fun AgentEvent.toolName(): String? = when (this) {
        is AgentEvent.ToolStarted -> call.tool
        is AgentEvent.ToolExecuted -> result.tool
        is AgentEvent.ToolFailed -> result.tool
        is AgentEvent.ToolBlocked -> result.tool
        is AgentEvent.ConfirmationRequired -> call.tool
        else -> null
    }

    private fun String.toStatusName(): String =
        replace('_', ' ')
            .replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.US) else char.toString()
            }

    private fun notificationAction(
        titleRes: Int,
        intent: PendingIntent,
    ): NotificationCompat.Action {
        return NotificationCompat.Action.Builder(
            0,
            context.getString(titleRes),
            intent
        ).build()
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
