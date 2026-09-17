package com.artrubadur.teno.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.artrubadur.teno.R
import com.artrubadur.teno.ui.components.buttons.PlainIconButton
import com.artrubadur.teno.ui.components.buttons.PrimaryIconButton

@Composable
fun ScreenHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onAdd: (() -> Unit)? = null,
    addEnabled: Boolean = true,
    addContentDescription: String = "Add",
    addModifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.weight(1f),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )

        Row {
            if (onAdd != null) {
                PrimaryIconButton(
                    iconRes = R.drawable.ic_add,
                    contentDescription = addContentDescription,
                    modifier = addModifier,
                    onClick = onAdd,
                    enabled = addEnabled,
                )
            }

            PlainIconButton(
                iconRes = R.drawable.ic_arrow,
                contentDescription = "Back",
                onClick = onBack,
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
