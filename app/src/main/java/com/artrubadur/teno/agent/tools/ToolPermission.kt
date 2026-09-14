package com.artrubadur.teno.agent.tools

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.artrubadur.teno.agent.tools.integrations.ScreenAccessibilityService

enum class ToolPermission(
    val title: String,
    val description: String,
    val grantType: PermissionGrantType
) {
    WRITE_SETTINGS(
        title = "Write system settings",
        description = "Allows the agent to change device settings",
        grantType = PermissionGrantType.INTENT
    ),

    CAMERA(
        title = "Camera access",
        description = "Allows the agent to use the camera and flashlight",
        grantType = PermissionGrantType.RUNTIME
    ),

    MODIFY_AUDIO_SETTINGS(
        title = "Modify audio settings",
        description = "Allows the agent to control device audio",
        grantType = PermissionGrantType.NONE
    ),

    ACCESSIBILITY_SERVICE(
        title = "Accessibility service",
        description = "Allows the agent to read on-screen content and interact with visible controls",
        grantType = PermissionGrantType.INTENT
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

            ACCESSIBILITY_SERVICE ->
                isAccessibilityServiceEnabled(context)

            else -> true
        }
    }

    fun grantIntent(context: Context): Intent? {
        return when (this) {
            WRITE_SETTINGS -> Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                "package:${context.packageName}".toUri()
            )

            ACCESSIBILITY_SERVICE -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

            else -> null
        }
    }

    fun manifestPermission(): String? {
        return when (this) {
            CAMERA -> Manifest.permission.CAMERA
            else -> null
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val manager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

        val expected = ComponentName(
            context,
            ScreenAccessibilityService::class.java
        )

        return manager
            .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { info ->
                val service = info.resolveInfo.serviceInfo
                service.packageName == expected.packageName &&
                        service.name == expected.className
            }
    }
}

enum class PermissionGrantType {
    INTENT,
    RUNTIME,
    NONE
}
