package com.artrubadur.teno.agent.tools.impl.phone

import android.content.Context
import android.telephony.PhoneNumberUtils
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.phone.callPhoneNumber
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

class PhoneCallTool(private val context: Context) : Tool<PhoneCallTool.Args> {
    override val name = "phone_call"
    override val title = "Make phone call"
    override val description = "Calls a phone number"
    override val group = ToolGroup.PHONE
    override val risk = ToolRisk.REQUIRES_CONFIRMATION
    override val enabled = true
    override val requiredPermissions = setOf(ToolPermission.CALL_PHONE)
    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject {
        val phoneNumber = args.phoneNumber.trim()
        require(phoneNumber.isNotBlank()) { "phone_number must not be blank" }
        require(PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
            "phone_number must be a valid phone number"
        }
        return context.callPhoneNumber(phoneNumber)
    }

    @Serializable
    data class Args(
        @SerialName("phone_number")
        val phoneNumber: String,
    )
}
