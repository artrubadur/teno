package com.artrubadur.tonemo.agent.tools

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri

enum class ToolPermission(
    val title: String,
    val description: String,
) {
    WRITE_SETTINGS(
        title = "Write system settings",
        description = "Allows changing system settings such as screen brightness"
    );

    fun isGranted(context: Context): Boolean {
        return when (this) {
            WRITE_SETTINGS -> Settings.System.canWrite(context)
        }
    }

    fun grantIntent(context: Context): Intent {
        return when (this) {
            WRITE_SETTINGS -> Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                "package:${context.packageName}".toUri()
            )
        }
    }
}
