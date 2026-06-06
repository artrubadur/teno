package com.artrubadur.tonemo.ui.screens.modelManager

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.artrubadur.tonemo.R
import com.artrubadur.tonemo.data.model.ModelType
import com.artrubadur.tonemo.ui.components.DropdownMenu
import com.artrubadur.tonemo.ui.components.buttons.PlainIconButton
import com.artrubadur.tonemo.ui.components.buttons.PrimaryIconButton


@Composable
fun ModelDialog(
    modelName: String,
    modelType: ModelType,
    onNameChange: (String) -> Unit,
    onTypeChange: (ModelType) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = modelName,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Model name") },
                    singleLine = true
                )

                DropdownMenu(
                    options = ModelType.entries.map(ModelType::name),
                    selectedOption = modelType.name,
                    onSelect = { selectedName ->
                        selectedName?.let { name ->
                            ModelType.entries.firstOrNull { it.name == name }?.let(onTypeChange)
                        }
                    },
                    buttonModifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    PlainIconButton(
                        iconRes = R.drawable.ic_close,
                        onClick = onDismiss,
                        contentDescription = "Cancel"
                    )

                    PrimaryIconButton(
                        iconRes = R.drawable.ic_confirm,
                        onClick = onConfirm,
                        contentDescription = "Add"
                    )
                }
            }
        }
    }
}
