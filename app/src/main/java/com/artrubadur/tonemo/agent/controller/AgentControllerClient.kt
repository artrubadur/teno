package com.artrubadur.tonemo.agent.controller

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

class AgentControllerClient(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val clientMessenger = Messenger(IncomingHandler(this))
    private val pendingCommands = ArrayDeque<AgentControllerCommand>()

    private val _state = MutableStateFlow(AgentControllerState())
    val state: StateFlow<AgentControllerState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AgentControllerEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<AgentControllerEvent> = _events.asSharedFlow()

    private var serviceMessenger: Messenger? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            serviceMessenger = Messenger(service)
            isBound = true
            sendRaw(AgentControllerMessengerContract.MSG_REGISTER, replyTo = clientMessenger)
            while (pendingCommands.isNotEmpty()) {
                send(pendingCommands.removeFirst())
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceMessenger = null
            isBound = false
            _state.value = _state.value.copy(
                isReady = false,
                isLoading = false,
                isWorking = false,
            )
            _events.tryEmit(AgentControllerEvent.Message("Agent service disconnected"))
        }
    }

    init {
        bind()
    }

    fun send(command: AgentControllerCommand) {
        if (serviceMessenger == null) {
            pendingCommands += command
            return
        }

        val payload = AgentControllerMessengerContract.json.encodeToString(command)
        sendRaw(AgentControllerMessengerContract.MSG_COMMAND, payload = payload)
    }

    fun close() {
        pendingCommands.clear()
        if (!isBound) return
        sendRaw(AgentControllerMessengerContract.MSG_UNREGISTER, replyTo = clientMessenger)
        appContext.unbindService(connection)
        isBound = false
        serviceMessenger = null
    }

    private fun bind() {
        val intent = Intent(appContext, AgentControllerService::class.java)
        isBound = appContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun sendRaw(what: Int, payload: String? = null, replyTo: Messenger? = null) {
        val service = serviceMessenger ?: return
        try {
            val message = Message.obtain(null, what).apply {
                this.replyTo = replyTo
                if (payload != null) {
                    data = Bundle().apply {
                        putString(AgentControllerMessengerContract.KEY_PAYLOAD, payload)
                    }
                }
            }
            service.send(message)
        } catch (_: RemoteException) {
            _events.tryEmit(AgentControllerEvent.Message("Agent service unavailable"))
        }
    }

    private class IncomingHandler(client: AgentControllerClient) : Handler(Looper.getMainLooper()) {
        private val clientRef = WeakReference(client)

        override fun handleMessage(message: Message) {
            val client = clientRef.get() ?: return

            when (message.what) {
                AgentControllerMessengerContract.MSG_EVENT -> {
                    val payload =
                        message.data.getString(AgentControllerMessengerContract.KEY_PAYLOAD)
                            ?: return
                    val event =
                        AgentControllerMessengerContract.json.decodeFromString<AgentControllerEvent>(
                            payload
                        )
                    if (event is AgentControllerEvent.StateChanged) {
                        client._state.value = event.state
                    } else client._events.tryEmit(event)
                }

                else -> super.handleMessage(message)
            }
        }
    }
}
