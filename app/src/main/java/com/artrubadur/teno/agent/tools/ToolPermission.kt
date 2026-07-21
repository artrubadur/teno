package com.artrubadur.teno.agent.tools

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri

enum class ToolPermission(
    val title: String,
    val description: String,
    val grantType: PermissionGrantType
) {
    WRITE_SETTINGS(
        title = "Write system settings",
        description = "Allows changing system settings such as screen brightness",
        grantType = PermissionGrantType.INTENT
    ),

    MODIFY_AUDIO_SETTINGS(
        title = "Modify audio settings",
        description = "Allows changing system audio settings such as volume",
        grantType = PermissionGrantType.NONE
    );

    fun isGranted(context: Context): Boolean {
        return when (this) {
            WRITE_SETTINGS ->
                Settings.System.canWrite(context)

            MODIFY_AUDIO_SETTINGS ->
                true
        }
    }

    fun grantIntent(context: Context): Intent? {
        return when (this) {
            WRITE_SETTINGS -> Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                "package:${context.packageName}".toUri()
            )

            else -> null
        }
    }

    fun manifestPermission(): String? {
        return when (this) {
            else -> null
        }
    }
}

enum class PermissionGrantType {
    INTENT,
    RUNTIME,
    NONE
}