package com.artrubadur.tonemo.agent.tools.impl

import com.artrubadur.tonemo.agent.tools.NoArgs
import com.artrubadur.tonemo.agent.tools.Tool
import com.artrubadur.tonemo.agent.tools.ToolRisk
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class GetCurrentTimeTool : Tool<NoArgs> {

    override val name = "get_current_time"

    override val description =
        "Returns the current date, weekday, time, and timezone"

    override val risk = ToolRisk.SAFE

    override val argsSerializer = NoArgs.serializer()

    override suspend fun executeTyped(args: NoArgs): JsonObject {
        val zdt = ZonedDateTime.now()

        return buildJsonObject {
            put("date", zdt.toLocalDate().toString())
            put("weekday", zdt.dayOfWeek.name)
            put("time", zdt.toLocalTime().truncatedTo(ChronoUnit.SECONDS).toString())
            put("timezone", zdt.zone.id)
        }
    }
}
