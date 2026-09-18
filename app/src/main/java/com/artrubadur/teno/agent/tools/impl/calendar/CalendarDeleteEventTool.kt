package com.artrubadur.teno.agent.tools.impl.calendar

import android.content.Context
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.calendar.deleteCalendarEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

class CalendarDeleteEventTool(private val context: Context) : Tool<CalendarDeleteEventTool.Args> {
    override val name = "calendar_delete_event"
    override val title = "Delete calendar event"
    override val description = "Deletes a calendar event"
    override val group = ToolGroup.CALENDAR
    override val risk = ToolRisk.REQUIRES_CONFIRMATION
    override val enabled = true
    override val requiredPermissions = setOf(ToolPermission.WRITE_CALENDAR)
    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject =
        context.contentResolver.deleteCalendarEvent(args.eventId)

    @Serializable
    data class Args(
        @SerialName("event_id") val eventId: Long,
    )
}
