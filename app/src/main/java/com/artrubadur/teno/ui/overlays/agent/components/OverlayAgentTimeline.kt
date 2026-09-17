package com.artrubadur.teno.ui.overlays.agent.components

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.agent.controller.AgentControllerEvent
import com.artrubadur.teno.agent.orchestration.AgentEvent
import com.artrubadur.teno.agent.tools.ToolCall
import com.artrubadur.teno.agent.tools.ToolResult
import com.artrubadur.teno.ui.components.AgentTimeline
import com.artrubadur.teno.ui.components.eventlist.ToolCallConfirmation
import com.artrubadur.teno.ui.components.eventlist.hasLiveTimer
import com.artrubadur.teno.ui.components.eventlist.pendingConfirmation
import com.artrubadur.teno.ui.components.eventlist.toEventEntries
import com.artrubadur.teno.ui.components.markdown.MarkdownText
import com.artrubadur.teno.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun OverlayAgentTimeline(
    events: List<AgentControllerEvent>,
    isWorking: Boolean,
    onApproveConfirmation: (String) -> Unit,
    onRejectConfirmation: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val entries = remember(events) { events.toEventEntries() }
    val finalAnswer = events.asReversed()
        .firstNotNullOfOrNull { (it as? AgentControllerEvent.Agent)?.event as? AgentEvent.FinalAnswer }
    val serviceMessage =
        events.asReversed().firstNotNullOfOrNull { (it as? AgentControllerEvent.Message)?.message }
    val confirmation = remember(entries) { entries.pendingConfirmation() }
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    var expanded by remember(events) { mutableStateOf(false) }

    LaunchedEffect(entries.hasLiveTimer()) {
        while (entries.hasLiveTimer()) {
            now = SystemClock.elapsedRealtime()
            delay(100.milliseconds)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
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
                if (entries.size > 1) {
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

                if (expanded && entries.size > 1) {
                    AgentTimeline(
                        events = events,
                        now = now,
                        onApproveConfirmation = onApproveConfirmation,
                        onRejectConfirmation = onRejectConfirmation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                    )
                } else if (confirmation != null) {
                    Column(
                        modifier = if (entries.size > 1) Modifier.padding(top = 12.dp) else Modifier,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Agent wants to ${confirmation.call.tool.replace("_", " ")}",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        ToolCallConfirmation(
                            event = confirmation,
                            onApproveConfirmation = onApproveConfirmation,
                            onRejectConfirmation = onRejectConfirmation,
                        )
                    }
                } else {
                    val contentModifier =
                        if (entries.size > 1) Modifier.padding(top = 12.dp) else Modifier
                    if (finalAnswer != null) {
                        MarkdownText(
                            markdown = finalAnswer.message,
                            modifier = contentModifier,
                            maxLines = 3,
                        )
                    } else {
                        Text(
                            text = when {
                                isWorking -> "Agent is working..."
                                serviceMessage != null -> serviceMessage
                                else -> "Agent was interrupted"
                            },
                            modifier = contentModifier,
                            color = if (isWorking || serviceMessage != null) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
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
private fun OverlayAgentTimelinePreview() {
    AppTheme {
        OverlayAgentTimeline(
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
