package com.artrubadur.tonemo.agent.tools.impl

import com.artrubadur.tonemo.agent.tools.AgentTool
import com.artrubadur.tonemo.agent.tools.NoToolArgs
import com.artrubadur.tonemo.agent.tools.ToolResult
import com.artrubadur.tonemo.agent.tools.ToolRisk
import com.artrubadur.tonemo.agent.tools.toJsonSchema
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TimeTool : AgentTool<NoToolArgs> {

    override val name = "time"

    override val description = "Returns the current time as structured data."

    override val risk = ToolRisk.SAFE

    override val argsSerializer = NoToolArgs.serializer()

    override val argsSchema = argsSerializer.toJsonSchema()

    private val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm")

    override suspend fun execute(args: NoToolArgs): ToolResult {
        val nowStr = LocalDateTime.now().format(formatter)
        return ToolResult.Success("""{"time":"$nowStr"}""")
    }
}
