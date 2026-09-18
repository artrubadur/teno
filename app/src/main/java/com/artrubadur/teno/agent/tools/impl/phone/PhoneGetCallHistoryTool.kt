package com.artrubadur.teno.agent.tools.impl.phone

import android.content.Context
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.phone.getCallHistory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

class PhoneGetCallHistoryTool(private val context: Context) :
    Tool<PhoneGetCallHistoryTool.Args> {
    override val name = "phone_get_call_history"
    override val title = "Get call history"
    override val description =
        "Searches call history; an empty query returns recent calls"
    override val group = ToolGroup.PHONE
    override val risk = ToolRisk.SAFE
    override val enabled = true
    override val requiredPermissions = setOf(ToolPermission.READ_CALL_LOG)
    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject {
        require(args.limit in 1..100) { "limit must be between 1 and 100" }
        val queries = args.query.map(String::trim).filter(String::isNotBlank)
        return buildJsonObject {
            put("calls", context.getCallHistory(queries, args.limit))
        }
    }

    @Serializable
    data class Args(
        val query: List<String> = emptyList(),
        val limit: Int = 20,
    )
}
