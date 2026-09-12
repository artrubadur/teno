package com.artrubadur.teno.agent.tools

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.ContextCompat
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

    CAMERA(
        title = "Camera access",
        description = "Allows to take pictures, record video, toggle the device flashlight",
        grantType = PermissionGrantType.RUNTIME
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

            CAMERA ->
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

            else -> true
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
            CAMERA -> Manifest.permission.CAMERA
            else -> null
        }
    }
}

enum class PermissionGrantType {
    INTENT,
    RUNTIME,
    NONE
}