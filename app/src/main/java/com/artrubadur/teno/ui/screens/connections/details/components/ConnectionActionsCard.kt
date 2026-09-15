package com.artrubadur.teno.ui.screens.connections.details.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.R
import com.artrubadur.teno.ui.components.AppCard
import com.artrubadur.teno.ui.components.SectionLabel
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
fun ConnectionActionsCard(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionLabel(text = "ACTIONS")

        ActionCard(
            iconRes = R.drawable.ic_edit,
            text = "Edit connection",
            onClick = onEditClick,
        )

        ActionCard(
            iconRes = R.drawable.ic_delete,
            text = "Delete connection",
            onClick = onDeleteClick,
            isError = true,
        )
    }
}

@Composable
private fun ActionCard(
    iconRes: Int,
    text: String,
    onClick: () -> Unit,
    isError: Boolean = false,
) {
    val contentColor = if (isError) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    AppCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = if (isError) contentColor else MaterialTheme.colorScheme.primary,
            )

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                color = contentColor,
            )

            Icon(
                painter = painterResource(R.drawable.ic_kb_arrow),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
private fun ConnectionActionsCardPreview() {
    AppTheme {
        ConnectionActionsCard(
            onEditClick = {},
            onDeleteClick = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}
