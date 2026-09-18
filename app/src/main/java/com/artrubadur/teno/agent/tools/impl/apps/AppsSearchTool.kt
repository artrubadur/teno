package com.artrubadur.teno.agent.tools.impl.apps

import android.content.Context
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.apps.searchApplications
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

class AppsSearchTool(private val context: Context) : Tool<AppsSearchTool.Args> {
    override val name = "apps_search"
    override val title = "Search apps"
    override val description = "Searches installed apps"
    override val group = ToolGroup.APPS
    override val risk = ToolRisk.SAFE
    override val enabled = true
    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject {
        require(args.query.isNotBlank()) { "query must not be blank" }
        return kotlinx.serialization.json.buildJsonObject {
            put("apps", context.searchApplications(args.query))
        }
    }

    @Serializable
    data class Args(val query: String)
}
