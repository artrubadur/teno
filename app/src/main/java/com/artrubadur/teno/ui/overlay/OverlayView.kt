package com.artrubadur.teno.ui.overlay

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.ui.components.PromptInput
import com.artrubadur.teno.ui.overlay.components.AuraOverlay
import com.artrubadur.teno.ui.overlay.components.EventList
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
fun OverlayView(
    state: OverlayState,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onApproveConfirmation: (String) -> Unit,
    onRejectConfirmation: (String) -> Unit,
    onOutsideClick: () -> Unit,
    onIslandHidden: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    var islandHeightPx by remember { mutableIntStateOf(0) }
    val bottomOffsetPx by animateIntAsState(
        targetValue = if (state.isIslandVisible) 0 else islandHeightPx,
        animationSpec = tween(durationMillis = 200),
        label = "assistant_overlay_bottom_offset",
        finishedListener = { offset ->
            if (offset == islandHeightPx && !state.isIslandVisible) {
                onIslandHidden()
            }
        }
    )

    LaunchedEffect(state.focusInput, state.isIslandVisible, state.isWorking) {
        if (state.focusInput && state.isIslandVisible && !state.isWorking) {
            focusRequester.requestFocus()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = state.isWorking,
            enter = fadeIn(animationSpec = tween(durationMillis = 250)),
            exit = fadeOut(animationSpec = tween(durationMillis = 250)),
        ) {
            AuraOverlay()
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

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .imePadding()
                .padding(24.dp)
                .onSizeChanged { islandHeightPx = it.height }
                .offset { IntOffset(0, bottomOffsetPx) },
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.isWorking || state.controllerEvents.isNotEmpty()) {
                EventList(
                    events = state.controllerEvents,
                    isWorking = state.isWorking,
                    onApproveConfirmation = onApproveConfirmation,
                    onRejectConfirmation = onRejectConfirmation,
                )
            }

            PromptInput(
                value = state.input,
                onValueChange = onInputChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(28.dp)
                    ),
                inputFieldModifier = Modifier.focusRequester(focusRequester),
                onSend = onSend,
                onStopWork = onStop,
                isWorking = state.isWorking,
                canSend = state.canSend,
            )
        }
    }
}


@Preview(
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun OverlayViewOpenIslandPreview() {
    AppTheme {
        OverlayView(
            state = OverlayState(
                isOverlayVisible = true,
                isIslandVisible = true,
                input = "Open settings and do many many many many many many many other things",
                activeConnectionName = "Local",
                isReady = true,
            ),
            onInputChanged = { _ -> },
            onSend = {},
            onStop = {},
            onApproveConfirmation = {},
            onRejectConfirmation = {},
            onOutsideClick = {},
            onIslandHidden = {},
        )
    }
}

@Preview(
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun OverlayViewWorkingPreview() {
    AppTheme {
        OverlayView(
            state = OverlayState(
                isOverlayVisible = true,
                isIslandVisible = true,
                activeConnectionName = "Local",
                isReady = true,
                isWorking = true,
            ),
            onInputChanged = { _ -> },
            onSend = {},
            onStop = {},
            onApproveConfirmation = {},
            onRejectConfirmation = {},
            onOutsideClick = {},
            onIslandHidden = {},
        )
    }
}
