package com.artrubadur.teno.ui.screens.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artrubadur.teno.connection.runtime.llm.local.LiteRtBackendOption
import com.artrubadur.teno.connection.runtime.llm.local.LiteRtNpuSupport
import com.artrubadur.teno.data.agent.AgentInstructionKind
import com.artrubadur.teno.data.agent.AgentSettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val application: Application,
    private val store: AgentSettingsStore,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    private val npuSupported: Boolean
        get() = LiteRtNpuSupport.isSupported(application)
    private val supportedNpuSoCs = LiteRtNpuSupport.supportedSoCs(application)

    init {
        store.settings
            .onEach { settings ->
                if (
                    settings.liteRtBackend == LiteRtBackendOption.NPU &&
                    !npuSupported
                ) {
                    store.setLiteRtBackend(LiteRtBackendOption.CPU)
                    return@onEach
                }

                _state.value = SettingsState.from(
                    settings = settings,
                    npuSupported = npuSupported,
                    supportedNpuSoCs = supportedNpuSoCs,
                )
            }
            .launchIn(viewModelScope)
    }

    fun setMaxSteps(text: String) {
        val value = text.toIntOrNull()
        val error = value == null || value < 1
        _state.update { it.copy(maxStepsText = text, maxStepsError = error) }
        if (value != null && !error) viewModelScope.launch { store.setMaxSteps(value) }
    }

    fun setUnlimitedMaxSteps(value: Boolean) {
        _state.update { it.copy(unlimitedMaxSteps = value) }
        viewModelScope.launch { store.setUnlimitedMaxSteps(value) }
    }

    fun setTemperature(text: String) {
        val value = text.toDoubleOrNull()
        val error = value == null || value !in 0.0..2.0
        _state.update { it.copy(temperatureText = text, temperatureError = error) }
        if (value != null && !error) viewModelScope.launch { store.setTemperature(value) }
    }

    fun setTopK(text: String) {
        val value = text.toIntOrNull()
        val error = value == null || value !in 1..1000
        _state.update { it.copy(topKText = text, topKError = error) }
        if (value != null && !error) viewModelScope.launch { store.setTopK(value) }
    }

    fun setTopP(text: String) {
        val value = text.toDoubleOrNull()
        val error = value == null || value !in 0.0..1.0
        _state.update { it.copy(topPText = text, topPError = error) }
        if (value != null && !error) viewModelScope.launch { store.setTopP(value) }
    }

    fun setMaxTokens(text: String) {
        val value = text.toIntOrNull()
        val error = value == null || value !in 1..100000
        _state.update { it.copy(maxTokensText = text, maxTokensError = error) }
        if (value != null && !error) viewModelScope.launch { store.setMaxTokens(value) }
    }

    fun setLiteRtBackend(value: LiteRtBackendOption) {
        if (value == LiteRtBackendOption.NPU && !npuSupported) return

        _state.update { it.copy(liteRtBackend = value) }
        viewModelScope.launch {
            store.setLiteRtBackend(value)
        }
    }

    fun setInstructionEnabled(kind: AgentInstructionKind, id: String, enabled: Boolean) {
        viewModelScope.launch {
            store.setInstructionEnabled(kind, id, enabled)
        }
    }

    fun addInstruction(kind: AgentInstructionKind, text: String) {
        viewModelScope.launch {
            store.addInstruction(kind, text)
        }
    }

    fun deleteInstruction(kind: AgentInstructionKind, id: String) {
        viewModelScope.launch {
            store.deleteInstruction(kind, id)
        }
    }
}
