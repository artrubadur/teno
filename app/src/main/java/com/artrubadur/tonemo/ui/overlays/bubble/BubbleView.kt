package com.artrubadur.tonemo.ui.overlays.bubble

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.State
import com.artrubadur.tonemo.ui.theme.TonemoTheme

@SuppressLint("ViewConstructor")
class BubbleView(
    context: Context,
    expandedState: State<Boolean>,
    onCollapseAnimationEnd: () -> Unit,
) : LifecycleComposeHostView(
    context = context,
    composeLayoutParams = LayoutParams(
        LayoutParams.MATCH_PARENT,
        LayoutParams.MATCH_PARENT
    ),
    content = {
        TonemoTheme {
            BubbleContent(
                expanded = expandedState.value,
                onCollapseAnimationEnd = onCollapseAnimationEnd
            )
        }
    }
)
