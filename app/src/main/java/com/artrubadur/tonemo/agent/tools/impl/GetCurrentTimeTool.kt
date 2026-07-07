package com.artrubadur.tonemo.agent.tools.impl

import com.artrubadur.tonemo.agent.tools.NoToolArgs
import com.artrubadur.tonemo.agent.tools.Tool
import com.artrubadur.tonemo.agent.tools.ToolRisk
import com.artrubadur.tonemo.agent.tools.toJsonSchema
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class GetCurrentTimeTool : Tool<NoToolArgs> {

    override val name = "get_current_time"

    override val description =
        "Use when the user asks for the current time or date. Returns the current date, weekday and time."

    override val risk = ToolRisk.SAFE

    override val argsSerializer = NoToolArgs.serializer()

    override val argsSchema = argsSerializer.toJsonSchema()

    override suspend fun executeTyped(args: NoToolArgs): Map<String, String> {
        val zdt = ZonedDateTime.now()

        return mapOf(
            "date" to zdt.toLocalDate().toString(),
            "weekday" to zdt.dayOfWeek.name,
            "time" to zdt.toLocalTime().truncatedTo(ChronoUnit.SECONDS).toString(),
            "timezone" to zdt.zone.id
        )
    }
}
