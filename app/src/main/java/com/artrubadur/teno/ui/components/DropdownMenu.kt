package com.artrubadur.teno.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.ui.components.buttons.OutlinedButton
import androidx.compose.material3.DropdownMenu as DefaultDropdownMenu

@Composable
fun DropdownMenu(
    options: List<String>,
    selectedOption: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
    menuModifier: Modifier = Modifier,
    emptyOption: String? = null,
    enabled: Boolean = true,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val buttonText = selectedOption ?: emptyOption ?: options.firstOrNull().orEmpty()

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { isExpanded = !isExpanded },
            modifier = buttonModifier.fillMaxWidth(),
            contentPadding = PaddingValues(0.dp),
            enabled = enabled,
        ) {
            Text(buttonText)
        }

        DefaultDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = menuModifier,
            shape = MenuDefaults.shape,
            containerColor = MenuDefaults.containerColor,
            tonalElevation = MenuDefaults.TonalElevation,
            shadowElevation = MenuDefaults.ShadowElevation,
            border = null,
        ) {
            if (emptyOption != null) {
                DropdownMenuItem(
                    text = { Text(emptyOption) },
                    onClick = {
                        onSelect(null)
                        isExpanded = false
                    }
                )
            }

            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        isExpanded = false
                    }
                )
            }
        }
    }
}
