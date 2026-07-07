package com.artrubadur.tonemo.connection.runtime.llm.impl

import com.artrubadur.tonemo.connection.Connection
import com.artrubadur.tonemo.connection.RemoteConnection
import com.artrubadur.tonemo.connection.runtime.llm.LlmException
import com.artrubadur.tonemo.connection.runtime.llm.LlmRequest
import com.artrubadur.tonemo.connection.runtime.llm.LlmResponse
import com.artrubadur.tonemo.connection.runtime.llm.LlmRuntime
import java.util.concurrent.atomic.AtomicBoolean

class RemoteLlmRuntime : LlmRuntime {

    private val loaded = AtomicBoolean(false)

    override val isLoaded: Boolean
        get() = loaded.get()

    override suspend fun connect(connection: Connection) {
        if (connection !is RemoteConnection) {
            throw LlmException.UnsupportedConnectionType()
        }
        TODO("Not yet implemented")
    }

    override suspend fun generate(request: LlmRequest): LlmResponse {
        TODO("Not yet implemented")
    }

    override fun stopGeneration() {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}