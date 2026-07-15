package com.artrubadur.teno.ui.overlay.components

import android.content.res.Configuration
import android.os.SystemClock
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.agent.controller.AgentControllerEvent
import com.artrubadur.teno.agent.orchestration.AgentEvent
import com.artrubadur.teno.agent.tools.ToolCall
import com.artrubadur.teno.agent.tools.ToolResult
import com.artrubadur.teno.ui.overlay.components.eventlist.ErrorMessageRow
import com.artrubadur.teno.ui.overlay.components.eventlist.EventEntry
import com.artrubadur.teno.ui.overlay.components.eventlist.SingleEventRow
import com.artrubadur.teno.ui.overlay.components.eventlist.ToolCallRow
import com.artrubadur.teno.ui.overlay.components.eventlist.WorkDurationRow
import com.artrubadur.teno.ui.overlay.components.eventlist.hasLiveTimer
import com.artrubadur.teno.ui.overlay.components.eventlist.toEventEntries
import com.artrubadur.teno.ui.overlay.components.eventlist.workDurationUntil
import com.artrubadur.teno.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Duration.Companion.seconds

@Composable
fun EventList(
    events: List<AgentControllerEvent>,
    isWorking: Boolean,
    onApproveConfirmation: (String) -> Unit,
    onRejectConfirmation: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val entries = remember(events) { events.toEventEntries() }
    val hasEvents = entries.isNotEmpty()
    val finalAnswer = entries.asReversed()
        .mapNotNull { (it as? EventEntry.Single)?.event as? AgentEvent.FinalAnswer }
        .firstOrNull()
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(entries.hasLiveTimer()) {
        while (entries.hasLiveTimer()) {
            now = SystemClock.elapsedRealtime()
            delay(1.seconds)
        }
    }

    LaunchedEffect(events.isEmpty()) {
        if (events.isEmpty()) expanded = false
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight / 2),
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                if (hasEvents) {
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                            .pointerInput(Unit) {
                                detectVerticalDragGestures { _, dragAmount ->
                                    when {
                                        dragAmount < 0f -> expanded = true
                                        dragAmount > 0f -> expanded = false
                                    }
                                }
                            }
                    )
                }

                if (expanded && hasEvents) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        entries.forEachIndexed { index, entry ->
                            item(key = entry.key) {
                                EventEntryRow(
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
                } else {
                    Text(
                        text = if (isWorking) "Agent is working..." else finalAnswer?.message.orEmpty(),
                        modifier = if (hasEvents) Modifier.padding(top = 12.dp) else Modifier,
                        color = if (isWorking) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun EventEntryRow(
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


@Preview(
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun EventListPreview() {
    AppTheme {
        EventList(
            events = listOf(
                AgentControllerEvent.Agent(
                    AgentEvent.ToolStarted(
                        ToolCall(
                            "6",
                            "tool",
                            buildJsonObject { })
                    )
                ),
                AgentControllerEvent.Agent(
                    AgentEvent.ToolStarted(
                        ToolCall(
                            "1",
                            "tool",
                            buildJsonObject { })
                    )
                ),
                AgentControllerEvent.Agent(
                    AgentEvent.ToolExecuted(
                        ToolResult(
                            "1",
                            "tool",
                            buildJsonObject { })
                    )
                ),
                AgentControllerEvent.Message("Service error"),
                AgentControllerEvent.Agent(
                    AgentEvent.ToolStarted(
                        ToolCall("2", "tool", buildJsonObject { put("arg", "value") })
                    )
                ),
                AgentControllerEvent.Agent(
                    AgentEvent.ToolFailed(
                        ToolResult("2", "tool", buildJsonObject { put("message", "Failed: X") })
                    )
                ),
                AgentControllerEvent.Agent(
                    AgentEvent.ToolStarted(
                        ToolCall(
                            "3",
                            "tool",
                            buildJsonObject { })
                    )
                ),
                AgentControllerEvent.Agent(
                    AgentEvent.ToolBlocked(
                        ToolResult("3", "tool", buildJsonObject { put("message", "Blocked: X") })
                    )
                ),
                AgentControllerEvent.Agent(
                    AgentEvent.ToolStarted(
                        ToolCall(
                            "5",
                            "tool",
                            buildJsonObject { })
                    )
                ),

                AgentControllerEvent.Agent(
                    AgentEvent.ConfirmationRequired(
                        "1",
                        ToolCall("5", "tool", buildJsonObject { }),
                        "Confirmation",
                        "required"
                    )
                ),
                AgentControllerEvent.Agent(AgentEvent.Failed("Failed")),
                AgentControllerEvent.Agent(AgentEvent.FinalAnswer("Final"))
            ),
            isWorking = true,
            onApproveConfirmation = {},
            onRejectConfirmation = {},
        )
    }
}
