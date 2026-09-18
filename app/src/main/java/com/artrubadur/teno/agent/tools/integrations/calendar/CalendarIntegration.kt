package com.artrubadur.teno.agent.tools.integrations.calendar

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.provider.CalendarContract
import com.artrubadur.teno.agent.tools.integrations.search.expandSearchQueries
import com.artrubadur.teno.agent.tools.integrations.search.matchesSearchQuery
import com.artrubadur.teno.agent.tools.integrations.time.formatTimestamp
import com.artrubadur.teno.agent.tools.integrations.time.parseTimestamp
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.math.abs

private const val DEFAULT_RANGE_MILLIS = 30L * 24 * 60 * 60 * 1000
private const val CALENDAR_ACCESS_LEVEL = "calendar_access_level"

internal fun ContentResolver.listCalendarEvents(
    queries: List<String>,
    startTime: String?,
    endTime: String?,
    timezone: String?,
    limit: Int,
): JsonArray {
    val now = System.currentTimeMillis()
    val searchQueries = expandSearchQueries(queries)
    val noFilters = searchQueries.isEmpty() && startTime == null && endTime == null
    val start = startTime?.let { parseTimestamp(it, timezone) }
        ?: if (endTime == null) now else 0L
    val end = endTime?.let { parseTimestamp(it, timezone) }
        ?: if (noFilters) start + DEFAULT_RANGE_MILLIS else Long.MAX_VALUE
    require(end > start) { "end_time must be after start_time" }

    val events = buildList {
        val beforeEnd = minOf(now, end)
        if (start < beforeEnd) {
            addAll(
                queryCalendarEventsInRange(
                    start,
                    beforeEnd,
                    searchQueries,
                    limit,
                    descending = true
                )
            )
        }

        val afterStart = maxOf(now, start)
        if (afterStart < end) {
            addAll(
                queryCalendarEventsInRange(
                    afterStart,
                    end,
                    searchQueries,
                    limit,
                    descending = false
                )
            )
        }
    }

    val sortedEvents = events
        .distinctBy { it.id }
        .sortedBy { abs(it.timestamp - now) }
        .take(limit)

    return JsonArray(sortedEvents.map { it.value })
}

private fun ContentResolver.queryCalendarEventsInRange(
    start: Long,
    end: Long,
    queries: List<String>,
    limit: Int,
    descending: Boolean,
): List<CalendarRecord> {
    val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
        .appendPath(start.toString())
        .appendPath(end.toString())
        .build()
    val direction = if (descending) "DESC" else "ASC"
    val sortOrder = "${CalendarContract.Instances.BEGIN} $direction"
    return if (queries.isEmpty()) {
        queryCalendarEvents(uri, null, sortOrder, limit)
    } else {
        queries.flatMap { query ->
            queryCalendarEvents(uri, query, sortOrder, limit)
        }
    }
}

private fun ContentResolver.queryCalendarEvents(
    uri: android.net.Uri,
    searchQuery: String?,
    sortOrder: String,
    limit: Int,
): List<CalendarRecord> {
    val events = mutableListOf<CalendarRecord>()
    query(
        uri,
        arrayOf(
            CalendarContract.Instances._ID,
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.DESCRIPTION,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
        ),
        null,
        null,
        sortOrder,
    )?.use { cursor ->
        val instanceIdColumn = cursor.getColumnIndexOrThrow(CalendarContract.Instances._ID)
        val eventIdColumn = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
        val titleColumn = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
        val descriptionColumn = cursor.getColumnIndexOrThrow(CalendarContract.Instances.DESCRIPTION)
        val locationColumn = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_LOCATION)
        val beginColumn = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
        val endColumn = cursor.getColumnIndexOrThrow(CalendarContract.Instances.END)
        val allDayColumn = cursor.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)

        while (cursor.moveToNext() && events.size < limit) {
            val begin = cursor.getLong(beginColumn)
            val title = cursor.getString(titleColumn).orEmpty()
            val description = cursor.getString(descriptionColumn).orEmpty()
            val location = cursor.getString(locationColumn).orEmpty()
            if (searchQuery != null && listOf(title, description, location)
                    .none { it.matchesSearchQuery(searchQuery) }
            ) {
                continue
            }
            val event = buildJsonObject {
                put("event_id", cursor.getLong(eventIdColumn))
                put("title", title)
                description.takeIf { it.isNotBlank() }
                    ?.let { put("description", it) }
                location.takeIf { it.isNotBlank() }
                    ?.let { put("location", it) }
                put("start_time", formatTimestamp(begin))
                put("end_time", formatTimestamp(cursor.getLong(endColumn)))
                put("all_day", cursor.getInt(allDayColumn) != 0)
            }
            events += CalendarRecord(cursor.getLong(instanceIdColumn), begin, event)
        }
    }
    return events
}

private data class CalendarRecord(
    val id: Long,
    val timestamp: Long,
    val value: JsonObject,
)

internal fun ContentResolver.createCalendarEvent(
    calendarId: Long?,
    title: String,
    startTime: String,
    endTime: String,
    description: String?,
    location: String?,
    allDay: Boolean,
): JsonObject {
    val start = parseTimestamp(startTime)
    val end = parseTimestamp(endTime)
    require(end > start) { "end_time must be after start_time" }
    val targetCalendarId = calendarId ?: findWritableCalendarId()
    ?: error("No writable calendar found")

    val values = ContentValues().apply {
        put(CalendarContract.Events.CALENDAR_ID, targetCalendarId)
        put(CalendarContract.Events.TITLE, title)
        put(CalendarContract.Events.DTSTART, start)
        put(CalendarContract.Events.DTEND, end)
        put(
            CalendarContract.Events.EVENT_TIMEZONE,
            if (allDay) "UTC" else java.util.TimeZone.getDefault().id,
        )
        put(CalendarContract.Events.ALL_DAY, if (allDay) 1 else 0)
        description?.let { put(CalendarContract.Events.DESCRIPTION, it) }
        location?.let { put(CalendarContract.Events.EVENT_LOCATION, it) }
    }

    val eventUri = insert(CalendarContract.Events.CONTENT_URI, values)
        ?: error("Failed to create calendar event")
    return buildJsonObject {
        put("event_id", ContentUris.parseId(eventUri))
        put("ok", true)
    }
}

internal fun ContentResolver.updateCalendarEvent(
    eventId: Long,
    title: String?,
    startTime: String?,
    endTime: String?,
    timezone: String?,
    description: String?,
    location: String?,
    allDay: Boolean?,
): JsonObject {
    require(title != null || startTime != null || endTime != null || description != null || location != null || allDay != null) {
        "At least one event field must be provided"
    }
    val start = startTime?.let { parseTimestamp(it, timezone) }
    val end = endTime?.let { parseTimestamp(it, timezone) }
    require(start == null || end == null || end > start) {
        "end_time must be after start_time"
    }

    val values = ContentValues().apply {
        title?.let { put(CalendarContract.Events.TITLE, it) }
        start?.let { put(CalendarContract.Events.DTSTART, it) }
        end?.let { put(CalendarContract.Events.DTEND, it) }
        description?.let { put(CalendarContract.Events.DESCRIPTION, it) }
        location?.let { put(CalendarContract.Events.EVENT_LOCATION, it) }
        allDay?.let {
            put(CalendarContract.Events.ALL_DAY, if (it) 1 else 0)
            put(
                CalendarContract.Events.EVENT_TIMEZONE,
                if (it) "UTC" else timezone ?: java.util.TimeZone.getDefault().id,
            )
        }
    }
    val updated = update(
        ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId),
        values,
        null,
        null,
    )
    require(updated > 0) { "Calendar event not found" }
    return buildJsonObject {
        put("event_id", eventId)
        put("ok", true)
    }
}

internal fun ContentResolver.deleteCalendarEvent(eventId: Long): JsonObject {
    val deleted = delete(
        ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId),
        null,
        null,
    )
    require(deleted > 0) { "Calendar event not found" }
    return buildJsonObject {
        put("event_id", eventId)
        put("ok", true)
    }
}

private fun ContentResolver.findWritableCalendarId(): Long? {
    query(
        CalendarContract.Calendars.CONTENT_URI,
        arrayOf(CalendarContract.Calendars._ID),
        "${CalendarContract.Calendars.VISIBLE}=1 AND $CALENDAR_ACCESS_LEVEL >= ?",
        arrayOf("500"),
        "${CalendarContract.Calendars._ID} ASC",
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            return cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID))
        }
    }
    return null
}
