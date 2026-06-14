package com.artrubadur.tonemo.ui.components.buttons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.artrubadur.tonemo.R
import com.artrubadur.tonemo.ui.theme.TonemoTheme

@Composable
fun PrimaryLeadingIconButton(
    @DrawableRes iconRes: Int,
    text: String,
    contentDescription: String? = text,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconModifier: Modifier = Modifier.size(ButtonDefaults.IconSize),
    textModifier: Modifier = Modifier,
    shape: Shape = ButtonDefaults.shape,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = remember { MutableInteractionSource() },
) {
    PrimaryButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    ) {
        LeadingIconButtonContent(
            iconRes = iconRes,
            text = text,
            contentDescription = contentDescription,
            iconModifier = iconModifier,
            textModifier = textModifier,
        )
    }
}

@Composable
fun OutlinedLeadingIconButton(
    @DrawableRes iconRes: Int,
    text: String,
    contentDescription: String? = text,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconModifier: Modifier = Modifier.size(ButtonDefaults.IconSize),
    textModifier: Modifier = Modifier,
    shape: Shape = ButtonDefaults.shape,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = remember { MutableInteractionSource() },
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    ) {
        LeadingIconButtonContent(
            iconRes = iconRes,
            text = text,
            contentDescription = contentDescription,
            iconModifier = iconModifier,
            textModifier = textModifier,
        )
    }
}

@Composable
private fun LeadingIconButtonContent(
    @DrawableRes iconRes: Int,
    text: String,
    contentDescription: String?,
    iconModifier: Modifier,
    textModifier: Modifier,
) {
    Icon(
        painter = painterResource(iconRes),
        contentDescription = contentDescription,
        modifier = iconModifier,
    )
    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
    Text(
        text = text,
        modifier = textModifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun PrimaryEnabledPreview() {
    TonemoTheme {
        PrimaryLeadingIconButton(
            iconRes = R.drawable.ic_add,
            text = "Primary",
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryDisabledPreview() {
    TonemoTheme {
        PrimaryLeadingIconButton(
            iconRes = R.drawable.ic_add,
            text = "Primary",
            onClick = {},
            enabled = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OutlinedEnabledPreview() {
    TonemoTheme {
        OutlinedLeadingIconButton(
            iconRes = R.drawable.ic_add,
            text = "Outlined",
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OutlinedDisabledPreview() {
    TonemoTheme {
        OutlinedLeadingIconButton(
            iconRes = R.drawable.ic_add,
            text = "Outlined",
            onClick = {},
            enabled = false,
        )
    }
}

