package com.artrubadur.teno.connection.runtime.llm.remote

import com.artrubadur.teno.connection.Connection
import com.artrubadur.teno.connection.RemoteConnection
import com.artrubadur.teno.connection.runtime.llm.LlmException
import com.artrubadur.teno.connection.runtime.llm.LlmRequest
import com.artrubadur.teno.connection.runtime.llm.LlmResponse
import com.artrubadur.teno.connection.runtime.llm.LlmRuntime
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.cancellation.CancellationException

class RemoteLlmRuntime : LlmRuntime {

    private val ready = AtomicBoolean(false)

    override val isReady: Boolean
        get() = ready.get()

    private val activeGenerationJob = AtomicReference<Job?>(null)
    private var client: RemoteClient? = null

    override suspend fun connect(connection: Connection) {
        if (connection !is RemoteConnection) {
            throw LlmException.UnsupportedConnectionType()
        }

        close()

        val httpClient = HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                        encodeDefaults = true
                    }
                )
            }
        }

        client = RemoteClient(
            config = connection.config,
            httpClient = httpClient
        )
        ready.set(true)
    }

    override suspend fun generate(request: LlmRequest): LlmResponse {
        val generationJob = currentCoroutineContext()[Job]
        activeGenerationJob.set(generationJob)

        try {
            return client
                ?.generate(request)
                ?: throw LlmException.RuntimeIsNotReady()
        } catch (t: CancellationException) {
            throw t
        } catch (t: Throwable) {
            throw LlmException.GenerationFailed(
                message = "Failed to generate a response from the remote model provider",
                cause = t
            )
        } finally {
            activeGenerationJob.compareAndSet(generationJob, null)
        }

    }

    override fun stopGeneration() {
        activeGenerationJob.getAndSet(null)?.cancel()
    }

    override fun close() {
        stopGeneration()

        client?.close()
        client = null
        ready.set(false)
    }
}