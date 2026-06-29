package com.artrubadur.tonemo.runtime.llm

import android.content.Context
import android.util.Log
import com.artrubadur.tonemo.data.model.ModelStore
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class LiteRtLlmRuntime(
    private val appContext: Context,
    private val modelService: ModelStore,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val defaultBackend: LiteRtBackend = LiteRtBackend.Cpu,
    private val logSeverity: LogSeverity = LogSeverity.ERROR
) : LlmRuntime {

    private val lock = Mutex()

    private var engine: Engine? = null
    private var loadedModelPath: String? = null

    private val activeGenerationJob = AtomicReference<Job?>(null)
    private val loaded = AtomicBoolean(false)

    override val isLoaded: Boolean
        get() = loaded.get()

    override suspend fun load(modelPath: String) {
        withContext(dispatcher) {
            lock.withLock {
                val resolvedModelPath = try {
                    modelService.getModel(modelPath).absolutePath
                } catch (_: Throwable) {
                    throw LlmRuntimeException.ModelFileNotFound(modelPath)
                }

                activeGenerationJob.getAndSet(null)?.cancel()
                closeLocked()

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
                } catch (t: Throwable) {
                    loaded.set(false)
                    closeLocked()
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
        activeGenerationJob.set(generationJob)

        try {
            withContext(dispatcher) {
                lock.withLock {
                    val currentEngine = engine ?: throw LlmRuntimeException.ModelNotLoaded()
                    val currentConversation = currentEngine.createConversation(
                        options.toConversationConfig()
                    )

                    currentConversation.use { currentConversation ->
                        currentConversation
                            .sendMessageAsync(prompt)
                            .collect { message ->
                                currentCoroutineContext().ensureActive()

                                val textChunk = message.toTextChunk()
                                if (textChunk.isNotBlank()) {
                                    send(textChunk)
                                    Log.d("LlmRuntime", textChunk)
                                }
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
            activeGenerationJob.compareAndSet(generationJob, null)
        }
    }.flowOn(dispatcher)

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
        engine?.close()
        engine = null

        loadedModelPath = null
        loaded.set(false)
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
