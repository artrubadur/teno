package com.artrubadur.teno.agent.tools.impl.calendar

import android.content.Context
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.calendar.createCalendarEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

class CalendarCreateEventTool(private val context: Context) : Tool<CalendarCreateEventTool.Args> {
    override val name = "calendar_create_event"
    override val title = "Create calendar event"
    override val description = "Creates a calendar event; times use ISO 8601"
    override val group = ToolGroup.CALENDAR
    override val risk = ToolRisk.REQUIRES_CONFIRMATION
    override val enabled = true
    override val requiredPermissions = setOf(ToolPermission.WRITE_CALENDAR)
    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject {
        require(args.title.isNotBlank()) { "title must not be blank" }
        return context.contentResolver.createCalendarEvent(
            null,
            args.title,
            args.startTime,
            args.endTime,
            args.description.ifBlank { null },
            args.location.ifBlank { null },
            args.allDay,
        )
    }

    @Serializable
    data class Args(
        val title: String,
        @SerialName("start_time") val startTime: String,
        @SerialName("end_time") val endTime: String,
        val description: String = "",
        val location: String = "",
        @SerialName("all_day") val allDay: Boolean = false,
    )
}
