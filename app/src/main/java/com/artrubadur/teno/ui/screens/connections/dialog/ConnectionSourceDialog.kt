package com.artrubadur.teno.ui.screens.connections.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.artrubadur.teno.R
import com.artrubadur.teno.ui.components.buttons.OutlinedLeadingIconButton
import com.artrubadur.teno.ui.components.buttons.PlainLeadingIconButton
import com.artrubadur.teno.ui.theme.TenoTheme


@Composable
fun ConnectionSourceDialog(
    onLocalSelect: () -> Unit,
    onExternalSelect: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        DialogContent(
            onLocalSelect = onLocalSelect,
            onExternalSelect = onExternalSelect,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun DialogContent(
    onLocalSelect: () -> Unit,
    onExternalSelect: () -> Unit,
    onDismiss: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedLeadingIconButton(
                iconRes = R.drawable.ic_storage,
                text = "Local",
                contentDescription = "Add remote connection",
                onClick = onLocalSelect,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedLeadingIconButton(
                iconRes = R.drawable.ic_link,
                text = "Remote",
                contentDescription = "Add remote connection",
                onClick = onExternalSelect,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            PlainLeadingIconButton(
                iconRes = R.drawable.ic_close,
                text = "Cancel",
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
private fun DialogPreview() {
    TenoTheme {
        DialogContent(
            onLocalSelect = {},
            onExternalSelect = {},
            onDismiss = {}
        )
    }
}
