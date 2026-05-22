package com.artrubadur.tonemo.overlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.tonemo.R
import com.artrubadur.tonemo.ui.theme.TonemoTheme

@Composable
fun AssistantOverlayContent(onExpand: () -> Unit = {}) {
    Surface(
        modifier = Modifier.size(36.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
        shape = CircleShape
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(0.dp)
        ) {
            FilledIconButton(
                onClick = onExpand,
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_expand),
                    contentDescription = "Open",
                )
            }
        }
    }
}

@Preview
@Composable
private fun AssistantOverlayContentPreview() {
    TonemoTheme {
        AssistantOverlayContent()
    }
}