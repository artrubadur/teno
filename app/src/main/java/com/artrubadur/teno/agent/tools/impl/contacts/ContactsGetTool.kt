package com.artrubadur.teno.agent.tools.impl.contacts

import android.content.Context
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.contacts.readContact
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

class ContactsGetTool(private val context: Context) : Tool<ContactsGetTool.Args> {
    override val name = "contacts_get"
    override val title = "Get contact"
    override val description = "Returns a contact card by contact_id"
    override val group = ToolGroup.CONTACTS
    override val risk = ToolRisk.SAFE
    override val enabled = true
    override val requiredPermissions = setOf(ToolPermission.READ_CONTACTS)
    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject =
        context.contentResolver.readContact(args.contactId)

    @Serializable
    data class Args(
        @SerialName("contact_id")
        val contactId: Long
    )
}
