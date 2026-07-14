package com.artrubadur.teno.ui.components.buttons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.artrubadur.teno.R
import com.artrubadur.teno.ui.theme.AppTheme
import androidx.compose.material3.FilledIconButton as MaterialFilledIconButton
import androidx.compose.material3.IconButton as MaterialIconButton
import androidx.compose.material3.OutlinedIconButton as MaterialOutlinedIconButton

@Composable
fun PrimaryIconButton(
    @DrawableRes iconRes: Int,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconModifier: Modifier = Modifier.size(ButtonDefaults.IconSize),
    shape: Shape = IconButtonDefaults.standardShape,
    interactionSource: MutableInteractionSource? = remember { MutableInteractionSource() },
) {
    FilledIconButtonBase(
        iconRes = iconRes,
        contentDescription = contentDescription,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        iconModifier = iconModifier,
        shape = shape,
        interactionSource = interactionSource,
        colors = IconButtonDefaults.filledIconButtonColors(),
    )
}

@Composable
fun SecondaryIconButton(
    @DrawableRes iconRes: Int,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconModifier: Modifier = Modifier.size(ButtonDefaults.IconSize),
    shape: Shape = IconButtonDefaults.standardShape,
    interactionSource: MutableInteractionSource? = remember { MutableInteractionSource() },
) {
    FilledIconButtonBase(
        iconRes = iconRes,
        contentDescription = contentDescription,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        iconModifier = iconModifier,
        shape = shape,
        interactionSource = interactionSource,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
        ),
    )
}

@Composable
fun OutlinedIconButton(
    @DrawableRes iconRes: Int,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconModifier: Modifier = Modifier.size(ButtonDefaults.IconSize),
    shape: Shape = IconButtonDefaults.standardShape,
    interactionSource: MutableInteractionSource? = remember { MutableInteractionSource() },
) {
    MaterialOutlinedIconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = IconButtonDefaults.outlinedIconButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.38f
            ),
        ),
        border = BorderStroke(
            width = IconButtonDefaults.outlinedIconButtonBorder(enabled).width,
            color = if (enabled) {
                MaterialTheme.colorScheme.outline
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            },
        ),
        interactionSource = interactionSource,
        shape = shape,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = iconModifier,
        )
    }
}

@Composable
fun PlainIconButton(
    @DrawableRes iconRes: Int,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.primary,
    iconModifier: Modifier = Modifier.size(ButtonDefaults.IconSize),
    shape: Shape = IconButtonDefaults.standardShape,
    interactionSource: MutableInteractionSource? = remember { MutableInteractionSource() },
) {
    MaterialIconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = tint,
        ),
        interactionSource = interactionSource,
        shape = shape,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = iconModifier,
        )
    }
}

@Composable
fun ErrorIconButton(
    @DrawableRes iconRes: Int,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.error,
    containerColor: Color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
    iconModifier: Modifier = Modifier.size(ButtonDefaults.IconSize),
    shape: Shape = IconButtonDefaults.standardShape,
    interactionSource: MutableInteractionSource? = remember { MutableInteractionSource() },
) {
    FilledIconButtonBase(
        iconRes = iconRes,
        contentDescription = contentDescription,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        iconModifier = iconModifier,
        shape = shape,
        interactionSource = interactionSource,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = containerColor,
            contentColor = tint,
        ),
    )
}

@Composable
private fun FilledIconButtonBase(
    @DrawableRes iconRes: Int,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    iconModifier: Modifier,
    shape: Shape,
    interactionSource: MutableInteractionSource?,
    colors: IconButtonColors,
) {
    MaterialFilledIconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        shape = shape,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = iconModifier,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryEnabledPreview() {
    AppTheme {
        PrimaryIconButton(
            iconRes = R.drawable.ic_delete,
            onClick = {},
            contentDescription = "",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryDisabledPreview() {
    AppTheme {
        PrimaryIconButton(
            iconRes = R.drawable.ic_delete,
            onClick = {},
            contentDescription = "",
            enabled = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SecondaryEnabledPreview() {
    AppTheme {
        SecondaryIconButton(
            iconRes = R.drawable.ic_delete,
            onClick = {},
            contentDescription = "",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SecondaryDisabledPreview() {
    AppTheme {
        SecondaryIconButton(
            iconRes = R.drawable.ic_delete,
            onClick = {},
            contentDescription = "",
            enabled = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OutlinedEnabledPreview() {
    AppTheme {
        OutlinedIconButton(
            iconRes = R.drawable.ic_delete,
            onClick = {},
            contentDescription = "",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OutlinedDisabledPreview() {
    AppTheme {
        OutlinedIconButton(
            iconRes = R.drawable.ic_delete,
            onClick = {},
            contentDescription = "",
            enabled = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlainEnabledPreview() {
    AppTheme {
        PlainIconButton(
            iconRes = R.drawable.ic_delete,
            onClick = {},
            contentDescription = "",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlainDisabledPreview() {
    AppTheme {
        PlainIconButton(
            iconRes = R.drawable.ic_delete,
            onClick = {},
            contentDescription = "",
            enabled = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorEnabledPreview() {
    AppTheme {
        ErrorIconButton(
            iconRes = R.drawable.ic_delete,
            onClick = {},
            contentDescription = "",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorDisabledPreview() {
    AppTheme {
        ErrorIconButton(
            iconRes = R.drawable.ic_delete,
            onClick = {},
            contentDescription = "",
            enabled = false
        )
    }
}
