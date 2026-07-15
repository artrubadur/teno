package com.artrubadur.teno.ui.components.eventlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.R
import com.artrubadur.teno.agent.orchestration.AgentEvent
import java.util.Locale

@Composable
fun WorkDurationRow(
    duration: WorkDuration,
    now: Long,
) {
    val elapsedSeconds = duration.elapsedSeconds(now)
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_kb_arrow),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = if (duration.endTime == null) {
                "Working for ${String.format(Locale.US, "%.1fs", elapsedSeconds)}"
            } else {
                "Worked for ${String.format(Locale.US, "%.1fs", elapsedSeconds)}"
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun SingleEventRow(event: AgentEvent) {
    when (event) {
        is AgentEvent.FinalAnswer -> MessageRow(event.message)
        is AgentEvent.Failed -> ErrorMessageRow(event.reason)
        else -> Unit
    }
}

@Composable
private fun MessageRow(
    message: String,
) {
    Text(
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyMedium,
        text = message
    )
}

@Composable
fun ErrorMessageRow(message: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_error_circle),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp)
        )

        Text(
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            text = message
        )

    }
}
