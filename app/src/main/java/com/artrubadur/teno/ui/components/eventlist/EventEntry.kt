package com.artrubadur.teno.ui.components.eventlist

import com.artrubadur.teno.agent.controller.AgentControllerEvent
import com.artrubadur.teno.agent.orchestration.AgentEvent
import com.artrubadur.teno.agent.tools.ToolCall

sealed interface EventEntry {
    val key: String
    val startTime: Long
    val endTime: Long

    data class Message(
        val index: Int,
        val message: String,
        override val startTime: Long,
    ) : EventEntry {
        override val endTime = startTime
        override val key = "message-$index"
    }

    data class Single(
        val index: Int,
        val event: AgentEvent,
        override val startTime: Long,
    ) : EventEntry {
        override val endTime = startTime
        override val key = "event-$index"
    }

    data class Tool(
        val started: AgentControllerEvent.Agent,
        val result: AgentControllerEvent.Agent? = null,
    ) : EventEntry {
        val call: ToolCall = (started.event as AgentEvent.ToolStarted).call
        override val startTime = started.time
        override val endTime = result?.time ?: started.time
        override val key = "tool-${call.id}"
    }
}

data class WorkDuration(
    val startTime: Long,
    val endTime: Long?,
) {
    fun elapsedSeconds(now: Long): Float = ((endTime ?: now) - startTime).coerceAtLeast(0) / 1000f
}

fun List<AgentControllerEvent>.toEventEntries(): List<EventEntry> {
    val entries = mutableListOf<EventEntry>()

    forEachIndexed { index, controllerEvent ->
        when (controllerEvent) {
            is AgentControllerEvent.Message -> entries += EventEntry.Message(
                index = index,
                message = controllerEvent.message,
                startTime = controllerEvent.time,
            )

            is AgentControllerEvent.StateChanged -> Unit
            is AgentControllerEvent.Agent -> entries.addAgentEvent(index, controllerEvent)
        }
    }

    return entries
}

fun EventEntry.workDurationUntil(next: EventEntry?): WorkDuration? {
    if (this is EventEntry.Message) return null
    if (this is EventEntry.Single && event is AgentEvent.FinalAnswer) return null
    if (this is EventEntry.Tool && result == null) return null
    return WorkDuration(startTime = endTime, endTime = next?.startTime)
}

fun List<EventEntry>.hasLiveTimer(): Boolean {
    return any { it is EventEntry.Tool && it.result == null } || lastOrNull()?.workDurationUntil(
        null
    ) != null
}

private fun MutableList<EventEntry>.addAgentEvent(
    index: Int,
    controllerEvent: AgentControllerEvent.Agent,
) {
    val event = controllerEvent.event
    val terminalToolCallId = event.terminalToolCallId()

    when {
        event is AgentEvent.ToolStarted -> this += EventEntry.Tool(controllerEvent)
        terminalToolCallId != null -> if (!attachToolResult(terminalToolCallId, controllerEvent)) {
            this += EventEntry.Single(index, event, controllerEvent.time)
        }

        else -> this += EventEntry.Single(index, event, controllerEvent.time)
    }
}

private fun AgentEvent.terminalToolCallId(): String? = when (this) {
    is AgentEvent.ToolExecuted -> result.toolCallId
    is AgentEvent.ToolFailed -> result.toolCallId
    is AgentEvent.ToolBlocked -> result.toolCallId
    is AgentEvent.ConfirmationRequired -> call.id
    else -> null
}

private fun MutableList<EventEntry>.attachToolResult(
    toolCallId: String,
    result: AgentControllerEvent.Agent,
): Boolean {
    val index = indexOfLast { it is EventEntry.Tool && it.call.id == toolCallId }
    if (index < 0) return false
    val entry = this[index] as EventEntry.Tool
    this[index] = entry.copy(result = result)
    return true
}

