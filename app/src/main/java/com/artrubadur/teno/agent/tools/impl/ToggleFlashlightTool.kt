package com.artrubadur.teno.agent.tools.impl

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ToggleFlashlightTool(
    private val context: Context
) : Tool<ToggleFlashlightToolArgs> {

    override val name = "toggle_flashlight"

    override val title = "Toggle flashlight"

    override val description =
        "Turns device flashlight on or off"

    override val group = ToolGroup.SYSTEM

    override val risk = ToolRisk.SAFE

    override val requiredPermissions =
        setOf(ToolPermission.CAMERA)

    override val argsSerializer = ToggleFlashlightToolArgs.serializer()

    override suspend fun executeTyped(
        args: ToggleFlashlightToolArgs
    ): JsonObject {
        val cameraManager =
            context.getSystemService(
                Context.CAMERA_SERVICE
            ) as CameraManager

        val cameraId = findFlashCamera(cameraManager)
            ?: error("Flashlight is not available")

        try {
            cameraManager.setTorchMode(
                cameraId,
                args.enabled
            )
        } catch (e: CameraAccessException) {
            error("Cannot access flashlight: ${e.message}")
        }

        return buildJsonObject {
            put("ok", true)
        }
    }

    private fun findFlashCamera(
        cameraManager: CameraManager
    ): String? {
        return cameraManager.cameraIdList.firstOrNull { id ->

            val characteristics =
                cameraManager.getCameraCharacteristics(id)

            characteristics.get(
                CameraCharacteristics.FLASH_INFO_AVAILABLE
            ) == true
        }
    }
}

@Serializable
data class ToggleFlashlightToolArgs(
    val enabled: Boolean
)