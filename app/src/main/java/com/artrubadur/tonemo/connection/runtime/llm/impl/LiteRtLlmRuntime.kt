package com.artrubadur.tonemo.connection.runtime.llm.impl

import android.content.Context
import com.artrubadur.tonemo.agent.tools.ToolSpec
import com.artrubadur.tonemo.connection.Connection
import com.artrubadur.tonemo.connection.LocalConnection
import com.artrubadur.tonemo.connection.ModelType
import com.artrubadur.tonemo.connection.runtime.llm.LlmException
import com.artrubadur.tonemo.connection.runtime.llm.LlmRequest
import com.artrubadur.tonemo.connection.runtime.llm.LlmResponse
import com.artrubadur.tonemo.connection.runtime.llm.LlmRuntime
import com.artrubadur.tonemo.data.model.ModelStore
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
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class LiteRtLlmRuntime(
    private val appContext: Context,
    private val modelStore: ModelStore,
    private val defaultBackend: LiteRtBackend = LiteRtBackend.Cpu,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val logSeverity: LogSeverity = LogSeverity.ERROR,
) : LlmRuntime {

    private val lock = Mutex()

    private var engine: Engine? = null
    private var loadedModelPath: String? = null
    private var conversation: Conversation? = null
    private var conversationSessionId: String? = null
    private var sentToolResultCount: Int = 0

    private val activeGenerationJob = AtomicReference<Job?>(null)
    private val loaded = AtomicBoolean(false)

    override val isLoaded: Boolean
        get() = loaded.get()

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

                activeGenerationJob.getAndSet(null)?.cancel()
                closeLocked()

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
                    loaded.set(true)
                } catch (t: Throwable) {
                    loaded.set(false)
                    closeLocked()
                    throw LlmException.LoadFailed(connection.config.fileName, t)
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
                    val response = currentConversation.sendMessage(request.toMessage())
                    response.toLlmResponse()
                }
            }
        } catch (t: Throwable) {
            throw LlmException.GenerationFailed(t)
        } finally {
            activeGenerationJob.compareAndSet(generationJob, null)
        }
    }

    override fun stopGeneration() {
        activeGenerationJob.getAndSet(null)?.cancel()
    }

    override fun close() {
        activeGenerationJob.getAndSet(null)?.cancel()

        runBlocking {
            withContext(dispatcher) {
                lock.withLock {
                    closeLocked()
                }
            }
        }
    }

    private fun closeLocked() {
        conversation?.close()
        conversation = null
        conversationSessionId = null
        sentToolResultCount = 0

        engine?.close()
        engine = null

        loadedModelPath = null
        loaded.set(false)
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
        val currentEngine = engine ?: throw LlmException.RuntimeIsNotLoaded()

        if (conversation == null || conversationSessionId != request.sessionId) {
            conversation?.close()
            conversation = currentEngine.createConversation(request.buildConversationConfig())
            conversationSessionId = request.sessionId
            sentToolResultCount = 0
        }

        return conversation ?: throw LlmException.RuntimeIsNotLoaded()
    }

    private fun LlmRequest.toMessage(): Message {
        val pendingResults = toolResults.drop(sentToolResultCount)

        return if (pendingResults.isNotEmpty()) {
            sentToolResultCount = toolResults.size
            Message.tool(
                Contents.of(
                    pendingResults.map { result ->
                        Content.ToolResponse(result.tool, result.result)
                    }
                )
            )
        } else {
            Message.user(userRequest)
        }
    }

    private fun Message.toLlmResponse(): LlmResponse {
        if (toolCalls.isNotEmpty()) {
            return LlmResponse.ToolCalls(
                calls = toolCalls.map { call ->
                    com.artrubadur.tonemo.agent.tools.ToolCall(
                        tool = call.name,
                        arguments = call.arguments.mapValues { (_, value) -> value }
                    )
                }
            )
        }

        return LlmResponse.Final(toTextChunk())
    }

    private fun Message.toTextChunk(): String {
        return contents.contents
            .asSequence()
            .mapNotNull { content ->
                (content as? Content.Text)?.text
            }
            .joinToString(separator = "")
    }

    private fun ToolSpec.toOpenApiTool(): OpenApiTool {
        val spec = this
        return object : OpenApiTool {
            override fun getToolDescriptionJsonString(): String {
                return """{"name":"${spec.name}","description":"${spec.description}","parameters":${spec.argsSchema}}"""
            }

            override fun execute(paramsJsonString: String): String {
                throw UnsupportedOperationException("Tool execution is handled by AgentOrchestrator")
            }
        }
    }
}
