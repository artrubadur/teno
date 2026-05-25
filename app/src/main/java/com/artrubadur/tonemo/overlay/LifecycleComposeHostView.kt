package com.artrubadur.tonemo.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

@SuppressLint("ViewConstructor")
open class LifecycleComposeHostView(
    context: Context,
    composeLayoutParams: LayoutParams,
    content: @Composable () -> Unit,
) : FrameLayout(context), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val composeView = ComposeView(context)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    init {
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)

        setViewTreeLifecycleOwner(this)
        setViewTreeSavedStateRegistryOwner(this)

        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        composeView.setContent(content)
        addView(composeView, composeLayoutParams)
    }

    fun onAttachedToWindowManager() {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun onDetachedFromWindowManager() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        composeView.disposeComposition()
    }
}
