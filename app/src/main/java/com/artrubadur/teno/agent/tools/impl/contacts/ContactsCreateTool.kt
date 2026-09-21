package com.artrubadur.teno.agent.tools.impl.contacts

import android.content.Context
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.contacts.insertContact
import com.artrubadur.teno.agent.tools.integrations.contacts.readContact
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

class ContactsCreateTool(private val context: Context) : Tool<ContactsCreateTool.Args> {
    override val name = "contacts_create"
    override val title = "Create contact"
    override val description = "Creates a new contact with a name, phone, or email"
    override val group = ToolGroup.CONTACTS
    override val risk = ToolRisk.REQUIRES_CONFIRMATION
    override val enabled = true
    override val requiredPermissions = setOf(
        ToolPermission.READ_CONTACTS,
        ToolPermission.WRITE_CONTACTS,
    )
    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject {
        require(args.name.isNotBlank()) { "name must not be blank" }
        require(args.phone.isNotBlank() || args.email.isNotBlank()) {
            "phone or email is required"
        }
        val id = context.contentResolver.insertContact(
            name = args.name,
            phone = args.phone.takeIf { it.isNotBlank() },
            email = args.email.takeIf { it.isNotBlank() },
        )
        return context.contentResolver.readContact(id)
    }

    @Serializable
    data class Args(
        val name: String,
        val phone: String = "",
        val email: String = "",
    )
}
