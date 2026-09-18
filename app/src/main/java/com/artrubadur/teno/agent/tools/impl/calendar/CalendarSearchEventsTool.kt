package com.artrubadur.teno.agent.tools.impl.calendar

import android.content.Context
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.calendar.listCalendarEvents
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

class CalendarSearchEventsTool(private val context: Context) : Tool<CalendarSearchEventsTool.Args> {
    override val name = "calendar_search_events"
    override val title = "Search calendar events"
    override val description =
        "Searches calendar events; use time bounds instead of query when possible; without filters returns events in the next 30 days"
    override val group = ToolGroup.CALENDAR
    override val risk = ToolRisk.SAFE
    override val enabled = true
    override val requiredPermissions = setOf(ToolPermission.READ_CALENDAR)
    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject {
        require(args.limit in 1..100) { "limit must be between 1 and 100" }
        return buildJsonObject {
            put(
                "events",
                context.contentResolver.listCalendarEvents(
                    args.query.map(String::trim).filter(String::isNotBlank),
                    args.startTime.ifBlank { null },
                    args.endTime.ifBlank { null },
                    null,
                    args.limit,
                )
            )
        }
    }

    @Serializable
    data class Args(
        val query: List<String> = emptyList(),
        @SerialName("start_time") val startTime: String = "",
        @SerialName("end_time") val endTime: String = "",
        val limit: Int = 50,
    )
}
