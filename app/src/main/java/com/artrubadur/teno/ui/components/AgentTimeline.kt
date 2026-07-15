package com.artrubadur.teno.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.agent.controller.AgentControllerEvent
import com.artrubadur.teno.ui.components.eventlist.ErrorMessageRow
import com.artrubadur.teno.ui.components.eventlist.EventEntry
import com.artrubadur.teno.ui.components.eventlist.SingleEventRow
import com.artrubadur.teno.ui.components.eventlist.ToolCallRow
import com.artrubadur.teno.ui.components.eventlist.WorkDurationRow
import com.artrubadur.teno.ui.components.eventlist.toEventEntries
import com.artrubadur.teno.ui.components.eventlist.workDurationUntil

@Composable
fun AgentTimeline(
    events: List<AgentControllerEvent>,
    now: Long,
    onApproveConfirmation: (String) -> Unit,
    onRejectConfirmation: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val entries = events.toEventEntries()

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        entries.forEachIndexed { index, entry ->
            item(key = entry.key) {
                AgentEventRow(
                    entry = entry,
                    now = now,
                    onApproveConfirmation = onApproveConfirmation,
                    onRejectConfirmation = onRejectConfirmation,
                )
            }

            entry.workDurationUntil(entries.getOrNull(index + 1))?.let { duration ->
                item(key = "${entry.key}-work") {
                    WorkDurationRow(duration = duration, now = now)
                }
            }
        }
    }
}

@Composable
fun AgentTimelineColumn(
    events: List<AgentControllerEvent>,
    now: Long,
    onApproveConfirmation: (String) -> Unit,
    onRejectConfirmation: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val entries = events.toEventEntries()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        entries.forEachIndexed { index, entry ->
            AgentEventRow(
                entry = entry,
                now = now,
                onApproveConfirmation = onApproveConfirmation,
                onRejectConfirmation = onRejectConfirmation,
            )

            entry.workDurationUntil(entries.getOrNull(index + 1))?.let { duration ->
                WorkDurationRow(duration = duration, now = now)
            }
        }
    }
}


@Composable
private fun AgentEventRow(
    entry: EventEntry,
    now: Long,
    onApproveConfirmation: (String) -> Unit,
    onRejectConfirmation: (String) -> Unit,
) {
    when (entry) {
        is EventEntry.Message -> ErrorMessageRow(entry.message)
        is EventEntry.Single -> SingleEventRow(entry.event)
        is EventEntry.Tool -> ToolCallRow(
            entry = entry,
            now = now,
            onApproveConfirmation = onApproveConfirmation,
            onRejectConfirmation = onRejectConfirmation,
        )
    }
}
