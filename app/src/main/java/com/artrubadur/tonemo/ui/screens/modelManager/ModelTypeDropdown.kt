package com.artrubadur.tonemo.ui.screens.modelManager

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.artrubadur.tonemo.data.model.ModelType

@Composable
fun ModelTypeDropdown(
    selectedModelType: ModelType?,
    onModelTypeSelect: (ModelType?) -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            modifier = Modifier
                .size(48.dp),
            contentPadding = PaddingValues(0.dp),
            border = if (isExpanded) {
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            } else {
                ButtonDefaults.outlinedButtonBorder(enabled = true)
            },

            onClick = { isExpanded = true }
        ) {
            Text(selectedModelType?.name ?: "All")
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All") },
                onClick = {
                    onModelTypeSelect(null)
                    isExpanded = false
                }
            )

            ModelType.entries.forEach { modelType ->
                DropdownMenuItem(
                    text = { Text(modelType.name) },
                    onClick = {
                        onModelTypeSelect(modelType)
                        isExpanded = false
                    }
                )
            }
        }
    }
}
