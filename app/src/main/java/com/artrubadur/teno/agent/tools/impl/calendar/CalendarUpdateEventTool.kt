package com.artrubadur.teno.agent.tools.impl.calendar

import android.content.Context
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.calendar.updateCalendarEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

class CalendarUpdateEventTool(private val context: Context) : Tool<CalendarUpdateEventTool.Args> {
    override val name = "calendar_update_event"
    override val title = "Update calendar event"
    override val description = "Changes a calendar event; times use ISO 8601"
    override val group = ToolGroup.CALENDAR
    override val risk = ToolRisk.REQUIRES_CONFIRMATION
    override val enabled = true
    override val requiredPermissions = setOf(ToolPermission.WRITE_CALENDAR)
    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject {
        require(args.allDay in -1..1) {
            "all_day must be -1 (leave unchanged), 0 (false), or 1 (true)"
        }
        return context.contentResolver.updateCalendarEvent(
            args.eventId,
            args.title.ifBlank { null },
            args.startTime.ifBlank { null },
            args.endTime.ifBlank { null },
            null,
            args.description.ifBlank { null },
            args.location.ifBlank { null },
            args.allDay.takeIf { it != -1 }?.let { it == 1 },
        )
    }

    @Serializable
    data class Args(
        @SerialName("event_id") val eventId: Long,
        val title: String = "",
        @SerialName("start_time") val startTime: String = "",
        @SerialName("end_time") val endTime: String = "",
        val description: String = "",
        val location: String = "",
        @SerialName("all_day") val allDay: Int = -1,
    )
}
