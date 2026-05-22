package com.artrubadur.tonemo.overlay

import android.content.Context
import android.widget.FrameLayout
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.artrubadur.tonemo.ui.theme.TonemoTheme

class OverlayView(
    context: Context,
) : FrameLayout(context), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)

    private val savedStateRegistryController =
        SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val expandedState = mutableStateOf(false)
    private val composeView = ComposeView(context)

    init {
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)

        setViewTreeLifecycleOwner(this)
        setViewTreeSavedStateRegistryOwner(this)

        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        composeView.setContent {
            TonemoTheme {
                OverlayRootContent(
                    expanded = expandedState.value,
                    onExpand = {
                        expandedState.value = true
                    },
                    onCollapse = {
                        expandedState.value = false
                    },
                )
            }
        }

        addView(
            composeView,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        )
    }

    fun onAttachedToWindowManager() {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun onDetachedFromWindowManager() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED

        composeView.disposeComposition()
    }
}