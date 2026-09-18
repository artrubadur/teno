package com.artrubadur.teno.agent.tools.impl.contacts

import android.content.ContentUris
import android.content.Context
import android.provider.ContactsContract
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ContactsDeleteTool(private val context: Context) : Tool<ContactsDeleteTool.Args> {
    override val name = "contacts_delete"
    override val title = "Delete contact"
    override val description = "Deletes a contact by contact_id"
    override val group = ToolGroup.CONTACTS
    override val risk = ToolRisk.REQUIRES_CONFIRMATION
    override val enabled = true
    override val requiredPermissions = setOf(ToolPermission.WRITE_CONTACTS)
    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject {
        val deleted = context.contentResolver.delete(
            ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, args.contactId),
            null,
            null,
        )
        return buildJsonObject {
            put("ok", deleted > 0)
            put("contact_id", args.contactId)
        }
    }

    @Serializable
    data class Args(
        @SerialName("contact_id")
        val contactId: Long
    )
}
