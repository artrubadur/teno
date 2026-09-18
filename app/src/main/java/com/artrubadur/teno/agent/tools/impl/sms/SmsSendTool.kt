package com.artrubadur.teno.agent.tools.impl.sms

import android.content.Context
import android.telephony.PhoneNumberUtils
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.sms.sendSms
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

class SmsSendTool(private val context: Context) : Tool<SmsSendTool.Args> {
    override val name = "sms_send"
    override val title = "Send SMS"
    override val description = "Sends a text message"
    override val group = ToolGroup.SMS
    override val risk = ToolRisk.REQUIRES_CONFIRMATION
    override val enabled = true
    override val requiredPermissions = setOf(ToolPermission.SEND_SMS)
    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject {
        val phoneNumber = args.phoneNumber.trim()
        require(phoneNumber.isNotBlank()) { "phone_number must not be blank" }
        require(PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
            "phone_number must be a valid phone number"
        }
        require(args.message.isNotBlank()) { "message must not be blank" }
        return context.sendSms(phoneNumber, args.message)
    }

    @Serializable
    data class Args(
        @SerialName("phone_number")
        val phoneNumber: String,
        val message: String,
    )
}
