package com.artrubadur.teno.ui.overlays.agent

import android.annotation.SuppressLint
import android.content.Context
import android.widget.FrameLayout
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.artrubadur.teno.ui.theme.AppTheme

@SuppressLint("ViewConstructor")
class OverlayHostView(
    context: Context,
    controller: OverlayController,
    onCollapsed: () -> Unit,
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

        composeView.setContent {
            val state by controller.state.collectAsState()
            SideEffect {
                composeView.importantForAccessibility = if (state.isWorking) {
                    IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                } else IMPORTANT_FOR_ACCESSIBILITY_AUTO
            }
            AppTheme {
                OverlayView(
                    state = state,
                    onInputChanged = controller::onInputChanged,
                    onSend = controller::onSend,
                    onStopWork = controller::stopWork,
                    onLaunchActiveConnection = controller::launchActiveConnection,
                    onApproveConfirmation = controller::approveConfirmation,
                    onRejectConfirmation = controller::rejectConfirmation,
                    onOutsideClick = controller::onOutsideClick,
                    onIslandHidden = controller::onIslandHidden,
                    onCollapsed = onCollapsed,
                )
            }
        }

        addView(
            composeView,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
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
