package com.artrubadur.teno.connection.runtime.llm.local

import android.content.Context
import com.artrubadur.teno.agent.tools.ToolCall
import com.artrubadur.teno.agent.tools.ToolSpec
import com.artrubadur.teno.agent.tools.toJsonString
import com.artrubadur.teno.connection.Connection
import com.artrubadur.teno.connection.LocalConnection
import com.artrubadur.teno.connection.ModelType
import com.artrubadur.teno.connection.runtime.llm.LlmException
import com.artrubadur.teno.connection.runtime.llm.LlmMessage
import com.artrubadur.teno.connection.runtime.llm.LlmRequest
import com.artrubadur.teno.connection.runtime.llm.LlmResponse
import com.artrubadur.teno.connection.runtime.llm.LlmRuntime
import com.artrubadur.teno.data.model.ModelStore
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.LogSeverity
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.OpenApiTool
import com.google.ai.edge.litertlm.SamplerConfig
import com.google.ai.edge.litertlm.tool
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.cancellation.CancellationException

class LiteRtLlmRuntime(
    private val appContext: Context,
    private val modelStore: ModelStore,
    private val defaultBackend: LiteRtBackend = LiteRtBackend.Cpu,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val logSeverity: LogSeverity = LogSeverity.ERROR,
) : LlmRuntime {

    private val ready = AtomicBoolean(false)

    override val isReady: Boolean
        get() = ready.get()
    private val activeGenerationJob = AtomicReference<Job?>(null)
    private var engine: Engine? = null
    private val lock = Mutex()
    private var loadedModelPath: String? = null
    private var conversation: Conversation? = null
    private var conversationSessionId: String? = null
    private var syncedMessageCount: Int = 0


    override suspend fun connect(connection: Connection) {
        if (connection !is LocalConnection ||
            connection.config.modelType != ModelType.LITERTLM
        ) {
            throw LlmException.UnsupportedConnectionType()
        }
        withContext(dispatcher) {
            lock.withLock {
                val resolvedModelPath = try {
                    modelStore.getModel(connection.config.fileName).absolutePath
                } catch (_: Throwable) {
                    throw LlmException.ModelFileNotFound(connection.config.fileName)
                }

                stopGeneration()
                cleanup()

                try {
                    Engine.setNativeMinLogSeverity(logSeverity)

                    val engineConfig = EngineConfig(
                        modelPath = resolvedModelPath,
                        backend = defaultBackend.toLiteRtBackend(appContext),
                    )

                    val newEngine = Engine(engineConfig)
                    newEngine.initialize()

                    engine = newEngine
                    loadedModelPath = resolvedModelPath
                    ready.set(true)
                } catch (t: Throwable) {
                    ready.set(false)
                    cleanup()
                    throw LlmException.LoadingFailed(connection.config.fileName, t)
                }
            }
        }
    }

    override suspend fun generate(
        request: LlmRequest
    ): LlmResponse {
        val generationJob = currentCoroutineContext()[Job]
        activeGenerationJob.set(generationJob)

        try {
            return withContext(dispatcher) {
                lock.withLock {
                    val currentConversation = conversationFor(request)
                    val response = currentConversation.sendMessage(request.toLiteRtMessage())
                    syncedMessageCount = request.messages.size + 1
                    response.toLlmResponse()
                }
            }
        } catch (t: CancellationException) {
            throw t
        } catch (t: Throwable) {
            throw LlmException.GenerationFailed(
                message = "Failed to generate a response from the local model runtime.",
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

        runBlocking {
            withContext(dispatcher) {
                lock.withLock {
                    cleanup()
                }
            }
        }
    }

    private fun cleanup() {
        conversation?.close()
        conversation = null
        conversationSessionId = null
        syncedMessageCount = 0

        engine?.close()
        engine = null

        loadedModelPath = null
        ready.set(false)
    }

    private fun LlmRequest.buildConversationConfig(): ConversationConfig {
        return ConversationConfig(
            systemInstruction = Contents.of(instructions.render()),
            tools = tools.map { spec -> tool(spec.toOpenApiTool()) },
            samplerConfig = SamplerConfig(
                topK = options.topK,
                topP = options.topP,
                temperature = options.temperature
            ),
            automaticToolCalling = false
        )
    }

    private fun conversationFor(request: LlmRequest): Conversation {
        val currentEngine = engine ?: throw LlmException.RuntimeIsNotReady()

        if (conversation == null || conversationSessionId != request.sessionId) {
            conversation?.close()
            conversation = currentEngine.createConversation(request.buildConversationConfig())
            conversationSessionId = request.sessionId
            syncedMessageCount = 0
        }

        return conversation ?: throw LlmException.RuntimeIsNotReady()
    }

    private fun LlmRequest.toLiteRtMessage(): Message {
        val pendingMessages = messages.drop(syncedMessageCount)

        return when (val first = pendingMessages.first()) {
            is LlmMessage.User -> {
                Message.user(first.content)
            }

            is LlmMessage.Tool -> {
                Message.tool(
                    Contents.of(
                        pendingMessages.map { message ->
                            val result = (message as LlmMessage.Tool).result

                            Content.ToolResponse(
                                result.tool,
                                result.result
                            )
                        }
                    )
                )
            }

            is LlmMessage.AssistantToolCalls,
            is LlmMessage.AssistantFinal -> {
                error(
                    "Assistant messages are generated by LiteRT " +
                            "and must not be sent back"
                )
            }
        }
    }


    private fun Message.toLlmResponse(): LlmResponse {
        if (toolCalls.isNotEmpty()) {
            return LlmResponse.ToolCalls(
                calls = toolCalls.map { call ->
                    ToolCall(
                        id = UUID.randomUUID().toString(),
                        tool = call.name,
                        arguments = call.arguments.toJsonObject()
                    )
                }
            )
        }

        return LlmResponse.Final(
            contents.contents
                .asSequence()
                .mapNotNull { content ->
                    (content as? Content.Text)?.text
                }
                .joinToString(separator = "")
        )
    }

    private fun Map<String, Any?>.toJsonObject(): JsonObject =
        JsonObject(this.mapValues { (_, value) -> value.toJsonElement() })

    private fun Any?.toJsonElement(): JsonElement = when (this) {
        null -> JsonNull
        is JsonElement -> this
        is String -> JsonPrimitive(this)
        is Boolean -> JsonPrimitive(this)
        is Int -> JsonPrimitive(this)
        is Long -> JsonPrimitive(this)
        is Float -> JsonPrimitive(this)
        is Double -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is Map<*, *> -> JsonObject(
            this.entries.associate { (k, v) ->
                k.toString() to v.toJsonElement()
            }
        )

        is Iterable<*> -> JsonArray(this.map { it.toJsonElement() })
        is Array<*> -> JsonArray(this.map { it.toJsonElement() })
        else -> JsonPrimitive(this.toString())
    }

    private fun ToolSpec.toOpenApiTool(): OpenApiTool {
        val spec = this
        return object : OpenApiTool {
            override fun getToolDescriptionJsonString(): String {
                return """{"name":"${spec.name}","description":"${spec.description}","parameters":${spec.argsSchema.toJsonString()}}"""
            }

            override fun execute(paramsJsonString: String): String {
                throw UnsupportedOperationException("Tool execution is handled by AgentOrchestrator")
            }
        }
    }
}
