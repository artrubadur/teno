package com.artrubadur.tonemo.ui.screens.connections.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.artrubadur.tonemo.R
import com.artrubadur.tonemo.connection.ConnectionType
import com.artrubadur.tonemo.ui.components.buttons.PlainLeadingIconButton
import com.artrubadur.tonemo.ui.components.buttons.PrimaryLeadingIconButton
import com.artrubadur.tonemo.ui.theme.TonemoTheme


@Composable
fun ConnectionDetailsDialog(
    initialName: String,
    initialType: ConnectionType,
    onDismiss: () -> Unit,
    onConfirm: (String, ConnectionType) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        DialogContent(
            initialName = initialName,
            initialType = initialType,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
        )
    }
}

@Composable
private fun DialogContent(
    initialName: String,
    initialType: ConnectionType,
    onDismiss: () -> Unit,
    onConfirm: (String, ConnectionType) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var modelType by remember { mutableStateOf(initialType) }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    label = { Text("Name") },
                    singleLine = true
                )

//                DropdownMenu(
//                        options = ConnectionType.entries.map(ConnectionType::name),
//                selectedOption = modelType.name,
//                onSelect = { selectedName ->
//                    selectedName?.let { name ->
//                        ConnectionType.entries.firstOrNull { it.name == name }?.let { modelType = it }
//                    }
//                },
//                modifier = Modifier.padding(top = 8.dp),
//                buttonModifier = Modifier
//                    .size(56.dp),
//                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PrimaryLeadingIconButton(
                    iconRes = R.drawable.ic_confirm,
                    text = "Confirm",
                    onClick = { onConfirm(name.trim(), modelType) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)

                )

                PlainLeadingIconButton(
                    iconRes = R.drawable.ic_close,
                    text = "Cancel",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun DialogPreview() {
    TonemoTheme {
        DialogContent(
            initialName = "",
            initialType = ConnectionType.LLM,
            onDismiss = {},
            onConfirm = { name, type -> },
        )
    }
}
