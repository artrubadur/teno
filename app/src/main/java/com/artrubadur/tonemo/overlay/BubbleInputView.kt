package com.artrubadur.tonemo.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import androidx.compose.runtime.State

@SuppressLint("ViewConstructor")
class BubbleInputView(
    context: Context,
    private val expandedState: State<Boolean>,
    private val onExpand: () -> Unit,
    private val onCollapse: () -> Unit,
) : View(context) {

    init {
        setBackgroundColor(Color.TRANSPARENT)
        isClickable = true
        isFocusable = false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            performClick()
        }

        return true
    }

    override fun performClick(): Boolean {
        super.performClick()

        if (expandedState.value) {
            onCollapse()
        } else {
            onExpand()
        }

        return true
    }
}
