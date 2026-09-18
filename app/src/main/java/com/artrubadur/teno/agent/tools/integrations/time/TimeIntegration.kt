package com.artrubadur.teno.agent.tools.integrations.time

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

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

internal fun parseTimestamp(value: String, timezone: String? = null): Long {
    val normalized = value.trim()
    require(normalized.isNotEmpty()) { "time must not be blank" }

    normalized.toLongOrNull()?.let { return it }

    val zone = timezone?.let(ZoneId::of) ?: ZoneId.systemDefault()
    val instant = runCatching { Instant.parse(normalized) }
        .recoverCatching { OffsetDateTime.parse(normalized).toInstant() }
        .recoverCatching { ZonedDateTime.parse(normalized).toInstant() }
        .recoverCatching { LocalDateTime.parse(normalized).atZone(zone).toInstant() }
        .recoverCatching { LocalDate.parse(normalized).atStartOfDay(zone).toInstant() }
        .getOrElse {
            error("Invalid time '$value'. Use ISO 8601, for example 2026-09-18T15:00:00")
        }

    return instant.toEpochMilli()
}
