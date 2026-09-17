package com.artrubadur.teno.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun AppLink(
    text: String,
    url: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    val uriHandler = LocalUriHandler.current
    Text(
        text = text,
        modifier = modifier.clickable(role = Role.Button) { uriHandler.openUri(url) },
        color = MaterialTheme.colorScheme.primary,
        style = style,
        textDecoration = TextDecoration.Underline,
    )
}
