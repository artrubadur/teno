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
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenAccessibilityService

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

    READ_CONTACTS(
        title = "Read contacts",
        description = "Allows the agent to read contacts",
        grantType = PermissionGrantType.RUNTIME
    ),

    WRITE_CONTACTS(
        title = "Write contacts",
        description = "Allows the agent to create, change, and delete contacts",
        grantType = PermissionGrantType.RUNTIME
    ),

    CALL_PHONE(
        title = "Make phone calls",
        description = "Allows the agent to make phone calls",
        grantType = PermissionGrantType.RUNTIME
    ),

    READ_CALL_LOG(
        title = "Read call history",
        description = "Allows the agent to read call history",
        grantType = PermissionGrantType.RUNTIME
    ),

    SEND_SMS(
        title = "Send SMS",
        description = "Allows the agent to send text messages",
        grantType = PermissionGrantType.RUNTIME
    ),

    READ_SMS(
        title = "Read SMS",
        description = "Allows the agent to read text messages",
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

            READ_CONTACTS ->
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED

            WRITE_CONTACTS ->
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED

            CALL_PHONE ->
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED

            READ_CALL_LOG ->
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CALL_LOG
                ) == PackageManager.PERMISSION_GRANTED

            SEND_SMS ->
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED

            READ_SMS ->
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_SMS
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
            READ_CONTACTS -> Manifest.permission.READ_CONTACTS
            WRITE_CONTACTS -> Manifest.permission.WRITE_CONTACTS
            CALL_PHONE -> Manifest.permission.CALL_PHONE
            READ_CALL_LOG -> Manifest.permission.READ_CALL_LOG
            SEND_SMS -> Manifest.permission.SEND_SMS
            READ_SMS -> Manifest.permission.READ_SMS
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
