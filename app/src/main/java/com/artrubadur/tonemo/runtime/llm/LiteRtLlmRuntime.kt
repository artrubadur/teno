package com.artrubadur.tonemo.runtime.llm

import android.content.Context
import com.artrubadur.tonemo.data.model.ModelService
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.LogSeverity
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.SamplerConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class LiteRtLlmRuntime(
    private val appContext: Context,
    private val modelService: ModelService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val defaultBackend: LiteRtBackend = LiteRtBackend.Cpu,
    private val logSeverity: LogSeverity = LogSeverity.ERROR
) : LlmRuntime {

    private val lock = Mutex()

    private var engine: Engine? = null
    private var conversation: Conversation? = null
    private var loadedModelPath: String? = null
    private var lastOptions: LlmGenerationOptions? = null

    private var activeGenerationJob: Job? = null
    private val loaded = AtomicBoolean(false)

    override val isLoaded: Boolean
        get() = loaded.get()

    override suspend fun load(modelPath: String) {
        withContext(dispatcher) {
            lock.withLock {
                val resolvedModelPath = try {
                    modelService.getModel(modelPath).modelFile.absolutePath
                } catch (_: Throwable) {
                    throw LlmRuntimeException.ModelFileNotFound(modelPath)
                }

                close()

                try {
                    Engine.setNativeMinLogSeverity(logSeverity)

                    val engineConfig = EngineConfig(
                        modelPath = resolvedModelPath,
                        backend = defaultBackend.toLiteRtBackend(appContext),
                        cacheDir = appContext.cacheDir.resolve("litert_lm_cache").apply {
                            mkdirs()
                        }.absolutePath
                    )

                    val newEngine = Engine(engineConfig)
                    newEngine.initialize()

                    engine = newEngine
                    loadedModelPath = resolvedModelPath
                    loaded.set(true)

                    createConversation(LlmGenerationOptions())
                } catch (t: Throwable) {
                    loaded.set(false)
                    close()
                    throw LlmRuntimeException.LoadFailed(modelPath, t)
                }
            }
        }
    }

    override suspend fun generate(
        prompt: String,
        options: LlmGenerationOptions
    ): String {
        val builder = StringBuilder()

        generateStream(prompt, options).collect { chunk ->
            builder.append(chunk)
        }

        return builder.toString()
    }

    override fun generateStream(
        prompt: String,
        options: LlmGenerationOptions
    ): Flow<String> = channelFlow {
        val generationJob = currentCoroutineContext()[Job]
        activeGenerationJob = generationJob

        try {
            withContext(dispatcher) {
                lock.withLock {
                    val currentEngine = engine ?: throw LlmRuntimeException.ModelNotLoaded()

                    if (conversation == null || lastOptions != options) {
                        conversation?.close()
                        conversation = null
                        createConversation(options)
                    }

                    val currentConversation =
                        conversation ?: currentEngine.createConversation(
                            options.toConversationConfig()
                        ).also { conversation = it }

                    currentConversation
                        .sendMessageAsync(prompt)
                        .collect { message ->
                            currentCoroutineContext().ensureActive()

                            val textChunk = message.toTextChunk()
                            if (textChunk.isNotBlank()) {
                                send(textChunk)
                            }
                        }
                }
            }
        } catch (t: Throwable) {
            if (t is CancellationException) {
                throw t
            }
            throw LlmRuntimeException.GenerationFailed(t)
        } finally {
            if (activeGenerationJob == generationJob) {
                activeGenerationJob = null
            }
        }
    }.flowOn(dispatcher)

    override suspend fun resetConversation() {
        withContext(dispatcher) {
            lock.withLock {
                conversation?.close()
                conversation = null
                lastOptions = null

                if (engine != null) {
                    createConversation(LlmGenerationOptions())
                }
            }
        }
    }

    override fun stopGeneration() {
        activeGenerationJob?.cancel()
        activeGenerationJob = null
    }

    override fun close() {
        activeGenerationJob?.cancel()
        activeGenerationJob = null

        conversation?.close()
        conversation = null

        engine?.close()
        engine = null

        loadedModelPath = null
        lastOptions = null
        loaded.set(false)
    }

    private fun createConversation(options: LlmGenerationOptions) {
        val currentEngine = engine ?: throw LlmRuntimeException.ModelNotLoaded()

        conversation?.close()
        conversation = currentEngine.createConversation(options.toConversationConfig())
        lastOptions = options
    }

    private fun LlmGenerationOptions.toConversationConfig(): ConversationConfig {
        return ConversationConfig(
            systemInstruction = Contents.of(systemPrompt),
            samplerConfig = SamplerConfig(
                topK = topK,
                topP = topP,
                temperature = temperature
            )
        )
    }

    private fun Message.toTextChunk(): String {
        return contents.contents
            .asSequence()
            .mapNotNull { content ->
                (content as? Content.Text)?.text
            }
            .joinToString(separator = "")
    }
}
