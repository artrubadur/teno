package com.artrubadur.teno.agent.controller

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import com.artrubadur.teno.agent.orchestration.AgentOrchestrator
import com.artrubadur.teno.connection.Connection
import com.artrubadur.teno.connection.ConnectionManager
import com.artrubadur.teno.connection.ConnectionType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.ref.WeakReference

class AgentControllerService : Service(), KoinComponent {
    private val connectionManager: ConnectionManager by inject()
    private val agentOrchestrator: AgentOrchestrator by inject()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val clients = mutableSetOf<Messenger>()
    private val incoming = Messenger(IncomingHandler(this))

    private var state = AgentControllerState()
    private var activeConnection: Connection? = null
    private var workJob: Job? = null
    private var launchJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        connectionManager
            .observeActiveConnection(ConnectionType.LLM)
            .onEach { connection ->
                if (activeConnection != connection || connection == null) {
                    terminateConnection()
                }
                activeConnection = connection
                updateState {
                    it.copy(
                        activeConnectionId = connection?.id,
                        activeConnectionName = connection?.name,
                        activeConnectionKind = connection?.kind,
                    )
                }
            }
            .launchIn(scope)
    }

    override fun onBind(intent: Intent?): IBinder = incoming.binder

    override fun onDestroy() {
        terminateConnection()
        scope.cancel()
        super.onDestroy()
    }

    private class IncomingHandler(service: AgentControllerService) :
        Handler(Looper.getMainLooper()) {
        private val serviceRef = WeakReference(service)

        override fun handleMessage(message: Message) {
            val service = serviceRef.get() ?: return

            when (message.what) {
                AgentControllerMessengerContract.MSG_REGISTER -> {
                    val client = message.replyTo ?: return
                    service.clients += client
                    service.sendTo(client, AgentControllerEvent.StateChanged(service.state))
                }

                AgentControllerMessengerContract.MSG_UNREGISTER -> {
                    message.replyTo?.let(service.clients::remove)
                }

                AgentControllerMessengerContract.MSG_COMMAND -> {
                    val payload =
                        message.data.getString(AgentControllerMessengerContract.KEY_PAYLOAD)
                            ?: return
                    val command =
                        AgentControllerMessengerContract.json.decodeFromString<AgentControllerCommand>(
                            payload
                        )
                    service.handleCommand(command)
                }

                else -> super.handleMessage(message)
            }
        }
    }

    private fun handleCommand(command: AgentControllerCommand) {
        when (command) {
            AgentControllerCommand.LaunchActiveConnection -> launchActiveConnection()
            AgentControllerCommand.TerminateConnection -> terminateConnection()
            AgentControllerCommand.StopWork -> stopWork()
            is AgentControllerCommand.SendMessage -> sendMessage(command.prompt)
            is AgentControllerCommand.ApproveConfirmation -> respondToConfirmation(
                command.confirmationId,
                approve = true
            )

            is AgentControllerCommand.RejectConfirmation -> respondToConfirmation(
                command.confirmationId,
                approve = false
            )
        }
    }

    private fun launchActiveConnection() {
        val connection = activeConnection
        if (connection == null) {
            emitMessage("No active connection.")
            return
        }

        if (state.isLoading || agentOrchestrator.isReady) return

        stopWork()

        launchJob = scope.launch {
            updateState { it.copy(isLoading = true, isReady = false) }
            try {
                agentOrchestrator.connect(connection)
                updateState { it.copy(isLoading = false, isReady = true) }
            } catch (t: CancellationException) {
                updateState { it.copy(isLoading = false, isReady = false) }
                throw t
            } catch (t: Throwable) {
                updateState { it.copy(isLoading = false, isReady = false) }
                emitMessage("Failed to launch connection: ${t.message}: ${t.cause?.message}")
            } finally {
                launchJob = null
            }
        }
    }

    private fun terminateConnection() {
        stopWork()
        agentOrchestrator.terminateConnection()
        updateState {
            it.copy(
                isReady = false,
                isLoading = false,
                isWorking = false,
            )
        }
    }

    private fun sendMessage(prompt: String) {
        val text = prompt.trim()

        if (activeConnection == null) {
            emitMessage("No active connection.")
            return
        }

        if (text.isEmpty() || !state.isReady || state.isLoading || state.isWorking) return

        updateState { it.copy(isWorking = true) }

        workJob = scope.launch {
            val currentJob = coroutineContext[Job]
            try {
                agentOrchestrator.sendMessage(text).collect { event ->
                    emitEvent(AgentControllerEvent.Agent(event))
                }
            } catch (t: CancellationException) {
                emitMessage("Stopped")
                throw t
            } catch (t: Throwable) {
                emitMessage("Work failed: ${t.message}: ${t.cause?.message}")
            } finally {
                if (workJob == currentJob) {
                    workJob = null
                }
                updateState { it.copy(isWorking = false) }
            }
        }
    }

    private fun respondToConfirmation(confirmationId: String, approve: Boolean) {
        if (state.isWorking) return

        updateState { it.copy(isWorking = true) }
        workJob = scope.launch {
            val currentJob = coroutineContext[Job]
            try {
                val events = if (approve) {
                    agentOrchestrator.approveConfirmation(confirmationId)
                } else {
                    agentOrchestrator.rejectConfirmation(confirmationId)
                }
                events.collect { event ->
                    emitEvent(AgentControllerEvent.Agent(event))
                }
            } catch (t: CancellationException) {
                throw t
            } catch (t: Throwable) {
                emitMessage("Confirmation failed: ${t.message}: ${t.cause?.message}")
            } finally {
                if (workJob == currentJob) {
                    workJob = null
                }
                updateState { it.copy(isWorking = false) }
            }
        }
    }

    private fun stopWork() {
        launchJob?.cancel()
        launchJob = null
        workJob?.cancel()
        workJob = null
        agentOrchestrator.stopWork()
        updateState { it.copy(isLoading = false, isWorking = false) }
    }

    private fun updateState(reducer: (AgentControllerState) -> AgentControllerState) {
        state = reducer(state)
        emitEvent(AgentControllerEvent.StateChanged(state))
    }

    private fun emitMessage(message: String) {
        emitEvent(AgentControllerEvent.Message(message))
    }

    private fun emitEvent(event: AgentControllerEvent) {
        val deadClients = mutableListOf<Messenger>()
        clients.forEach { client ->
            if (!sendTo(client, event)) {
                deadClients += client
            }
        }
        clients -= deadClients.toSet()
    }

    private fun sendTo(client: Messenger, event: AgentControllerEvent): Boolean {
        return try {
            val payload = AgentControllerMessengerContract.json.encodeToString(event)
            val message = Message.obtain(null, AgentControllerMessengerContract.MSG_EVENT).apply {
                data = Bundle().apply {
                    putString(AgentControllerMessengerContract.KEY_PAYLOAD, payload)
                }
            }
            client.send(message)
            true
        } catch (_: RemoteException) {
            false
        }
    }
}
