package com.artrubadur.teno.agent.tools.impl.contacts

import android.content.Context
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.screen.readContact
import com.artrubadur.teno.agent.tools.integrations.screen.updateContact
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

class ContactsUpdateTool(private val context: Context) : Tool<ContactsUpdateTool.Args> {
    override val name = "contacts_update"
    override val title = "Update contact"
    override val description = "Updates an existing contact name, phone, or email"
    override val group = ToolGroup.CONTACTS
    override val risk = ToolRisk.REQUIRES_CONFIRMATION
    override val enabled = true
    override val requiredPermissions = setOf(
        ToolPermission.READ_CONTACTS,
        ToolPermission.WRITE_CONTACTS,
    )
    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject {
        require(args.name.isNotBlank() || args.phone.isNotBlank() || args.email.isNotBlank()) {
            "at least one field is required"
        }
        context.contentResolver.updateContact(
            contactId = args.contactId,
            name = args.name.takeIf { it.isNotBlank() },
            phone = args.phone.takeIf { it.isNotBlank() },
            email = args.email.takeIf { it.isNotBlank() },
        )
        return context.contentResolver.readContact(args.contactId)
    }

    @Serializable
    data class Args(
        @SerialName("contact_id")
        val contactId: Long,
        val name: String = "",
        val phone: String = "",
        val email: String = "",
    )
}
