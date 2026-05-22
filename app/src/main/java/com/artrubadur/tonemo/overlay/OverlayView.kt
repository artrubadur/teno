package com.artrubadur.tonemo.overlay

import android.content.Context
import android.widget.FrameLayout
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
    private val onHide: () -> Unit = {}
) : FrameLayout(context), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val composeView = ComposeView(context)

    init {
        setViewTreeLifecycleOwner(this)
        setViewTreeSavedStateRegistryOwner(this)

        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        composeView.setContent {
            TonemoTheme { OverlayContent(onHide = onHide) }
        }

        addView(
            composeView,
            LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
        )
    }

    fun onAttachedToWindowManager() {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun onDetachedFromWindowManager() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }
}
