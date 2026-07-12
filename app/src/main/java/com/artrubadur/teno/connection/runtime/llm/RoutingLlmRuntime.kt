package com.artrubadur.teno.connection.runtime.llm

import com.artrubadur.teno.connection.Connection
import com.artrubadur.teno.connection.LocalConnection
import com.artrubadur.teno.connection.ModelType
import com.artrubadur.teno.connection.RemoteConnection
import com.artrubadur.teno.connection.runtime.llm.remote.RemoteLlmRuntime

class RoutingLlmRuntime(
    private val remoteRuntime: RemoteLlmRuntime,
    private val localRuntimes: Map<ModelType, LlmRuntime>,
) : LlmRuntime {

    private var activeRuntime: LlmRuntime? = null

    override val isReady: Boolean
        get() = activeRuntime?.isReady == true

    override suspend fun connect(connection: Connection) {
        val runtime = resolveRuntime(connection)

        if (activeRuntime !== runtime) {
            activeRuntime?.close()
        }

        runtime.connect(connection)
        activeRuntime = runtime
    }

    override suspend fun generate(
        request: LlmRequest
    ): LlmResponse {
        return requireActiveRuntime().generate(request)
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
                    ?: throw LlmException.UnsupportedConnectionType()
            }
        }
    }

    private fun requireActiveRuntime(): LlmRuntime {
        return activeRuntime ?: throw LlmException.RuntimeIsNotReady()
    }
}