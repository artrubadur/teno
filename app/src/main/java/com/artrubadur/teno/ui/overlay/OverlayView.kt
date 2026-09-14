package com.artrubadur.teno.ui.overlay

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.ui.overlay.components.AuraOverlay
import com.artrubadur.teno.ui.overlay.components.OverlayAgentTimeline
import com.artrubadur.teno.ui.overlay.components.OverlayPromptInput
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
fun OverlayView(
    state: OverlayState,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onLaunchActiveConnection: () -> Unit,
    onApproveConfirmation: (String) -> Unit,
    onRejectConfirmation: (String) -> Unit,
    onOutsideClick: () -> Unit,
    onIslandHidden: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val islandVisibility = remember { MutableTransitionState(false) }

    LaunchedEffect(state.isIslandVisible) {
        islandVisibility.targetState = state.isIslandVisible
    }

    LaunchedEffect(
        islandVisibility.isIdle,
        islandVisibility.currentState,
        islandVisibility.targetState,
    ) {
        if (islandVisibility.isIdle && !islandVisibility.currentState && !islandVisibility.targetState) {
            onIslandHidden()
        }
    }

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
                    onClick = onOutsideClick,
                )
        )

        AnimatedVisibility(
            visibleState = islandVisibility,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .imePadding()
                .padding(24.dp),
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 200),
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 200),
            ),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OverlayTimelineIsland(
                    state = state,
                    onApproveConfirmation = onApproveConfirmation,
                    onRejectConfirmation = onRejectConfirmation,
                )

                OverlayPromptInput(
                    value = state.input,
                    onValueChange = onInputChanged,
                    modifier = Modifier.fillMaxWidth(),
                    inputFieldModifier = Modifier.focusRequester(focusRequester),
                    onSend = onSend,
                    onStopWork = onStop,
                    onLaunchActiveConnection = onLaunchActiveConnection,
                    isWorking = state.isWorking,
                    isReady = state.isReady,
                    isLoading = state.isLoading,
                    canSend = state.canSend,
                    isActivated = state.isActivated,
                )
            }
        }
    }
}

@Composable
private fun OverlayTimelineIsland(
    state: OverlayState,
    onApproveConfirmation: (String) -> Unit,
    onRejectConfirmation: (String) -> Unit,
) {
    var visibleEvents by remember {
        mutableStateOf(
            if (state.isWorking) emptyList() else state.controllerEvents
        )
    }

    LaunchedEffect(state.isWorking, state.controllerEvents) {
        if (!state.isWorking && state.controllerEvents.isNotEmpty()) {
            visibleEvents = state.controllerEvents
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (!state.isWorking && visibleEvents.isNotEmpty()) 1f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "overlay_timeline_scale",
        finishedListener = { value ->
            if (value == 0f && state.isWorking) {
                visibleEvents = emptyList()
            }
        }
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomEnd,
    ) {
        if (visibleEvents.isNotEmpty()) {
            OverlayAgentTimeline(
                events = visibleEvents,
                isWorking = false,
                onApproveConfirmation = onApproveConfirmation,
                onRejectConfirmation = onRejectConfirmation,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    alpha = scale
                    transformOrigin = TransformOrigin(1f, 1f)
                },
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
            onLaunchActiveConnection = {},
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
            onLaunchActiveConnection = {},
            onApproveConfirmation = {},
            onRejectConfirmation = {},
            onOutsideClick = {},
            onIslandHidden = {},
        )
    }
}
