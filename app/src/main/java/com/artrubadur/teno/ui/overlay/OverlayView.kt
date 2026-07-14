package com.artrubadur.teno.ui.overlay

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.R
import com.artrubadur.teno.ui.components.buttons.ErrorIconButton
import com.artrubadur.teno.ui.components.buttons.OutlinedIconButton

@Composable
fun OverlayView(
    state: OverlayState,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onOutsideClick: () -> Unit,
    onIslandHidden: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val bottomOffset = animateDpAsState(
        targetValue = if (state.isIslandVisible) 0.dp else 112.dp,
        animationSpec = tween(durationMillis = 200),
        label = "assistant_overlay_bottom_offset",
        finishedListener = { offset ->
            if (offset == 112.dp) {
                onIslandHidden()
            }
        }
    )
    val statusText = when {
        !state.latestEvent.isNullOrBlank() -> state.latestEvent
        state.isWorking -> "Agent is working..."
        !state.isActivated -> "No active connection."
        else -> null
    }

    LaunchedEffect(state.focusInput, state.isIslandVisible, state.isWorking) {
        if (state.focusInput && state.isIslandVisible && !state.isWorking) {
            focusRequester.requestFocus()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isWorking) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(4.dp, MaterialTheme.colorScheme.primary)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onOutsideClick
                )
        )

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .imePadding()
                .padding(12.dp)
                .offset { IntOffset(0, bottomOffset.value.roundToPx()) },
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
        ) {
            Column {
                if (statusText != null)
                    Text(
                        text = statusText,
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    BasicTextField(
                        value = state.input,
                        enabled = !state.isWorking && !state.isLoading,
                        onValueChange = onInputChanged,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp)
                            .focusRequester(focusRequester)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        maxLines = 3,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (state.input.isEmpty()) {
                                    Text(
                                        text = "Input",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    if (state.isWorking || state.isLoading) {
                        ErrorIconButton(
                            iconRes = R.drawable.ic_stop,
                            contentDescription = "Stop work",
                            onClick = onStop,
                            modifier = Modifier.size(44.dp),
                        )
                    } else {
                        OutlinedIconButton(
                            iconRes = R.drawable.ic_arrow,
                            contentDescription = "Send",
                            onClick = onSend,
                            modifier = Modifier.size(44.dp),
                            enabled = state.canSend,
                            iconModifier = Modifier
                                .size(ButtonDefaults.IconSize)
                                .rotate(90f)
                        )
                    }
                }
            }
        }
    }
}
