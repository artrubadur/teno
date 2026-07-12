package com.artrubadur.teno.ui.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import com.artrubadur.teno.ui.theme.TenoTheme
import androidx.compose.material3.Button as MaterialButton
import androidx.compose.material3.OutlinedButton as MaterialOutlinedButton

@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
) {
    FilledButtonBase(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(),
        content = content,
    )
}

@Composable
fun SecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
) {
    FilledButtonBase(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
        ),
        content = content,
    )
}

@Composable
fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
) {
    MaterialOutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.outline,
            disabledContentColor = MaterialTheme.colorScheme.outlineVariant,
        ),
        border = BorderStroke(
            width = ButtonDefaults.outlinedButtonBorder(enabled).width,
            color = if (enabled) {
                MaterialTheme.colorScheme.outline
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    ) {
        content()
    }
}

@Composable
fun PlainButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
        ),
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    ) {
        content()
    }
}

@Composable
private fun FilledButtonBase(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    shape: Shape,
    contentPadding: PaddingValues,
    interactionSource: MutableInteractionSource?,
    colors: ButtonColors,
    content: @Composable () -> Unit,
) {
    MaterialButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryEnabledPreview() {
    TenoTheme {
        PrimaryButton(onClick = {}) {
            Text("Primary")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryDisabledPreview() {
    TenoTheme {
        PrimaryButton(onClick = {}, enabled = false) {
            Text("Primary")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SecondaryEnabledPreview() {
    TenoTheme {
        SecondaryButton(onClick = {}) {
            Text("Secondary")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SecondaryDisabledPreview() {
    TenoTheme {
        SecondaryButton(onClick = {}, enabled = false) {
            Text("Secondary")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OutlinedEnabledPreview() {
    TenoTheme {
        OutlinedButton(onClick = {}) {
            Text("Outlined")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OutlinedDisabledPreview() {
    TenoTheme {
        OutlinedButton(onClick = {}, enabled = false) {
            Text("Outlined")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlainEnabledPreview() {
    TenoTheme {
        PlainButton(onClick = {}) {
            Text("Plain")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlainDisabledPreview() {
    TenoTheme {
        PlainButton(onClick = {}, enabled = false) {
            Text("Plain")
        }
    }
}
