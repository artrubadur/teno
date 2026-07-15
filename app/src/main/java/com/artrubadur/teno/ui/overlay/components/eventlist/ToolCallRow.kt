package com.artrubadur.teno.ui.overlay.components.eventlist

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.R
import com.artrubadur.teno.agent.orchestration.AgentEvent
import com.artrubadur.teno.ui.components.buttons.OutlinedLeadingIconButton
import com.artrubadur.teno.ui.components.buttons.PrimaryLeadingIconButton
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.util.Locale

private val PrettyJson = Json { prettyPrint = true }

@Composable
fun ToolCallRow(
    entry: EventEntry.Tool,
    now: Long,
    onApproveConfirmation: (String) -> Unit,
    onRejectConfirmation: (String) -> Unit,
) {
    val event = entry.result?.event
    val elapsedSeconds = ((entry.result?.time ?: now) - entry.started.time).coerceAtLeast(0) / 1000f

    val resultJson = when (event) {
        is AgentEvent.ToolExecuted -> event.result.result
        is AgentEvent.ToolFailed -> event.result.result
        is AgentEvent.ToolBlocked -> event.result.result
        else -> null
    }

    var expanded by remember(entry.call.id) { mutableStateOf(false) }
    val expandable = !entry.call.arguments.isEmpty() || !resultJson.isNullOrEmpty()

    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) -90f else 90f,
        label = "tool_call_expand_arrow",
    )

    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp)
        )
        {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = entry.call.tool.replace('_', ' ')
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )

                    if (expandable) {
                        Icon(
                            painter = painterResource(R.drawable.ic_kb_arrow),
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(arrowRotation),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                ToolCallStatus(
                    result = event,
                    elapsedSeconds = elapsedSeconds,
                    onApproveConfirmation = onApproveConfirmation,
                    onRejectConfirmation = onRejectConfirmation,
                )
            }

            if (expandable) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(expandFrom = Alignment.Top),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                        if (!entry.call.arguments.isEmpty()) {
                            Text(
                                text = "Arguments",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium,
                            )

                            JsonBox(entry.call.arguments)
                        }
                        if (!resultJson.isNullOrEmpty()) {
                            Text(
                                text = "Result",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium,
                            )

                            JsonBox(resultJson)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JsonBox(result: JsonObject) {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline,
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = prettyJson(result),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(8.dp)
        )
    }
}

private fun prettyJson(json: JsonObject): String =
    PrettyJson.encodeToString(JsonObject.serializer(), json)

@Composable
private fun ToolCallStatus(
    result: AgentEvent?,
    elapsedSeconds: Float,
    onApproveConfirmation: (String) -> Unit,
    onRejectConfirmation: (String) -> Unit,
) {
    when (result) {
        is AgentEvent.ToolExecuted -> ToolCallStatusLabel(
            color = MaterialTheme.colorScheme.primary,
            text = "Executed",
            elapsedSeconds = elapsedSeconds,
            iconRes = R.drawable.ic_check_circle
        )

        is AgentEvent.ToolFailed -> ToolCallStatusLabel(
            color = MaterialTheme.colorScheme.error,
            text = "Failed",
            elapsedSeconds = elapsedSeconds,
            iconRes = R.drawable.ic_error_circle
        )

        is AgentEvent.ToolBlocked -> ToolCallStatusLabel(
            color = MaterialTheme.colorScheme.tertiary,
            text = "Blocked",
            elapsedSeconds = elapsedSeconds,
            iconRes = R.drawable.ic_warning
        )

        is AgentEvent.ConfirmationRequired -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ToolCallStatusLabel(
                color = MaterialTheme.colorScheme.primary,
                text = "Confirmation required",
                elapsedSeconds = elapsedSeconds,
                iconRes = R.drawable.ic_circle_question
            )

            ToolCallConfirmation(
                event = result,
                onApproveConfirmation = onApproveConfirmation,
                onRejectConfirmation = onRejectConfirmation,
            )
        }

        null -> ToolCallStatusLabel(
            color = MaterialTheme.colorScheme.primary,
            text = "Executing",
            elapsedSeconds = elapsedSeconds,
            iconRes = R.drawable.ic_circle_circle
        )

        else -> Unit
    }
}

@Composable
private fun ToolCallStatusLabel(
    color: Color,
    text: String,
    elapsedSeconds: Float,
    @DrawableRes iconRes: Int,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )

        Text(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            text = "$text - ${String.format(Locale.US, "%.1fs", elapsedSeconds)}"
        )

    }
}


@Composable
private fun ToolCallConfirmation(
    event: AgentEvent.ConfirmationRequired,
    onApproveConfirmation: (String) -> Unit,
    onRejectConfirmation: (String) -> Unit,
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PrimaryLeadingIconButton(
            iconRes = R.drawable.ic_confirm,
            text = "Approve",
            onClick = { onApproveConfirmation(event.confirmationId) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
        )
        OutlinedLeadingIconButton(
            iconRes = R.drawable.ic_close,
            text = "Reject",
            onClick = { onRejectConfirmation(event.confirmationId) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
        )
    }
}
