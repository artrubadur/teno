package com.artrubadur.teno.agent.tools.impl.sms

import android.content.Context
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.sms.searchSms
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

class SmsSearchTool(private val context: Context) : Tool<SmsSearchTool.Args> {
    override val name = "sms_search"
    override val title = "Search SMS"
    override val description = "Searches text messages; an empty query returns recent messages"
    override val group = ToolGroup.SMS
    override val risk = ToolRisk.SAFE
    override val enabled = true
    override val requiredPermissions = setOf(
        ToolPermission.READ_SMS,
        ToolPermission.READ_CONTACTS,
    )
    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject {
        require(args.limit in 1..100) { "limit must be between 1 and 100" }
        val queries = args.query.map(String::trim).filter(String::isNotBlank)
        return buildJsonObject {
            put("messages", context.searchSms(queries, args.limit))
        }
    }

    @Serializable
    data class Args(
        val query: List<String> = emptyList(),
        val limit: Int = 20,
    )
}
