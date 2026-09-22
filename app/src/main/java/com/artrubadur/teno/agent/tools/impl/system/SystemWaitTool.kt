package com.artrubadur.teno.agent.tools.impl.system

import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolRisk
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Duration.Companion.milliseconds

class SystemWaitTool : Tool<SystemWaitTool.Args> {
    override val name = "system_wait"
    override val title = "Wait"
    override val description =
        "Waits for the specified seconds; use after a tool call when its result needs time to appear or a repeated transient error may resolve"
    override val group = ToolGroup.SYSTEM
    override val risk = ToolRisk.SAFE
    override val enabled = true
    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject {
        delay((args.seconds * 1_000L).milliseconds)
        return buildJsonObject {
            put("ok", true)
        }
    }

    @Serializable
    data class Args(
        val seconds: Int,
    )
}
