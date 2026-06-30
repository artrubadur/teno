package com.artrubadur.tonemo.connection.runtime.llm

import com.artrubadur.tonemo.connection.Connection
import com.artrubadur.tonemo.connection.RemoteConnection
import java.util.concurrent.atomic.AtomicBoolean

class RemoteLlmRuntime : LlmRuntime {

    private val loaded = AtomicBoolean(false)

    override val isLoaded: Boolean
        get() = loaded.get()

    override suspend fun load(connection: Connection) {
        if (connection !is RemoteConnection) {
            throw LlmRuntimeException.UnsupportedConnectionType()
        }
        TODO("Not yet implemented")
    }

    override suspend fun generate(
        prompt: String,
        options: LlmGenerationOptions
    ): String {
        TODO("Not yet implemented")
    }

    override fun stopGeneration() {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}