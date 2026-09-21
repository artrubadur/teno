package com.artrubadur.teno.agent.tools.impl.contacts

import android.content.Context
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.contacts.findContactIds
import com.artrubadur.teno.agent.tools.integrations.contacts.readContact
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

class ContactsSearchTool(private val context: Context) : Tool<ContactsSearchTool.Args> {
    override val name = "contacts_search"
    override val title = "Search contacts"
    override val description = "Searches existing contacts by name, phone, or email"
    override val group = ToolGroup.CONTACTS
    override val risk = ToolRisk.SAFE
    override val enabled = true
    override val requiredPermissions = setOf(ToolPermission.READ_CONTACTS)
    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject {
        require(args.query.isNotBlank()) { "query must not be blank" }
        val contacts = context.contentResolver.findContactIds(args.query)
            .map { context.contentResolver.readContact(it, includeEmptyLists = false) }
        return kotlinx.serialization.json.buildJsonObject {
            put("contacts", JsonArray(contacts))
        }
    }

    @Serializable
    data class Args(val query: String)
}
