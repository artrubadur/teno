package com.artrubadur.tonemo.ui.overlays.bubble

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.State
import com.artrubadur.tonemo.ui.theme.TonemoTheme

@SuppressLint("ViewConstructor")
class OverlayView(
    context: Context,
    expandedState: State<Boolean>,
    onCollapseEnd: () -> Unit,
    onStop: () -> Unit = {},
) : LifecycleComposeHostView(
    context = context,
    composeLayoutParams = LayoutParams(
        LayoutParams.MATCH_PARENT,
        LayoutParams.WRAP_CONTENT
    ),
    content = {
        TonemoTheme {
            OverlayRootContent(
                expanded = expandedState.value,
                onCollapseEnd = onCollapseEnd,
                onStop = onStop
            )
        }
    }
)
