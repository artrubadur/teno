package com.artrubadur.teno.agent.tools.impl.system

import com.artrubadur.teno.agent.tools.NoArgs
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.time.formatTimestamp
import kotlinx.serialization.json.JsonObject

class SystemGetCurrentTimeTool : Tool<NoArgs> {

    override val name = "system_get_current_time"

    override val title = "Get current time"

    override val description =
        "Returns the current date, weekday, time, and timezone"
    override val group = ToolGroup.SYSTEM
    override val risk = ToolRisk.SAFE

    override val enabled = true

    override val argsSerializer = NoArgs.serializer()

    override suspend fun executeTyped(args: NoArgs): JsonObject {
        return formatTimestamp(System.currentTimeMillis())
    }
}
