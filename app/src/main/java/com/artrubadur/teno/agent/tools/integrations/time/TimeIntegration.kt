package com.artrubadur.teno.agent.tools.integrations.time

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal fun formatTimestamp(timestamp: Long): JsonObject {
    val zone = ZoneId.systemDefault()
    val dateTime = Instant.ofEpochMilli(timestamp).atZone(zone)

    return buildJsonObject {
        put("date", dateTime.toLocalDate().toString())
        put("weekday", dateTime.dayOfWeek.name)
        put("time", dateTime.toLocalTime().truncatedTo(ChronoUnit.SECONDS).toString())
        put("timezone", zone.id)
    }
}
