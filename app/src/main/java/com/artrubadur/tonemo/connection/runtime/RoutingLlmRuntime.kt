package com.artrubadur.tonemo.connection.runtime

import com.artrubadur.tonemo.connection.Connection
import com.artrubadur.tonemo.connection.LocalConnection
import com.artrubadur.tonemo.connection.ModelType
import com.artrubadur.tonemo.connection.RemoteConnection
import com.artrubadur.tonemo.connection.runtime.llm.LlmGenerationOptions
import com.artrubadur.tonemo.connection.runtime.llm.LlmRuntime
import com.artrubadur.tonemo.connection.runtime.llm.LlmRuntimeException
import com.artrubadur.tonemo.connection.runtime.llm.RemoteLlmRuntime

class RoutingLlmRuntime(
    private val remoteRuntime: RemoteLlmRuntime,
    private val localRuntimes: Map<ModelType, LlmRuntime>,
) : LlmRuntime {

    private var activeRuntime: LlmRuntime? = null

    override val isLoaded: Boolean
        get() = activeRuntime?.isLoaded == true

    override suspend fun load(connection: Connection) {
        val runtime = resolveRuntime(connection)

        if (activeRuntime !== runtime) {
            activeRuntime?.close()
        }

        runtime.load(connection)
        activeRuntime = runtime
    }

    override suspend fun generate(
        prompt: String,
        options: LlmGenerationOptions
    ): String {
        return requireActiveRuntime().generate(prompt, options)
    }

    override fun stopGeneration() {
        activeRuntime?.stopGeneration()
    }

    override fun close() {
        activeRuntime?.close()
        activeRuntime = null
    }

    private fun resolveRuntime(connection: Connection): LlmRuntime {
        return when (connection) {
            is RemoteConnection -> remoteRuntime

            is LocalConnection -> {
                localRuntimes[connection.config.modelType]
                    ?: throw LlmRuntimeException.UnsupportedConnectionType()
            }
        }
    }

    private fun requireActiveRuntime(): LlmRuntime {
        return activeRuntime ?: throw LlmRuntimeException.RuntimeIsNotLoaded()
    }
}