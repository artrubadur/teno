package com.artrubadur.tonemo.agent.orchestration

import com.artrubadur.tonemo.agent.policy.ConfirmationManager
import com.artrubadur.tonemo.agent.tools.BrokerResult
import com.artrubadur.tonemo.agent.tools.ToolBroker
import com.artrubadur.tonemo.agent.tools.ToolResult
import com.artrubadur.tonemo.connection.Connection
import com.artrubadur.tonemo.connection.runtime.llm.LlmResponse
import com.artrubadur.tonemo.connection.runtime.llm.LlmRuntime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import java.util.UUID

class AgentOrchestrator(
    private val llmRuntime: LlmRuntime,
    private val toolBroker: ToolBroker,
    private val confirmationManager: ConfirmationManager,
) {
    val isModelLoaded: Boolean
        get() = llmRuntime.isLoaded

    suspend fun connect(connection: Connection) {
        llmRuntime.connect(connection)
    }

    fun terminateModel() {
        llmRuntime.close()
    }

    fun stopGeneration() {
        llmRuntime.stopGeneration()
    }

    fun handleUserMessage(userMessage: String): Flow<AgentEvent> {
        val session = AgentSession(
            id = UUID.randomUUID().toString(),
            userRequest = userMessage,
            tools = toolBroker.listToolSpecs()
        )

        return flow {
            runAgentLoop(session)
        }
    }

    fun approveConfirmation(confirmationId: String): Flow<AgentEvent> {
        return flow {
            val confirmation = confirmationManager.consume(confirmationId) ?: run {
                emit(AgentEvent.Failed("Unknown or expired confirmation: $confirmationId"))
                return@flow
            }

            val session = confirmation.session

            when (val result = toolBroker.executeApproved(confirmation.call)) {
                is BrokerResult.Executed -> {
                    session.addToolResult(result.result)
                    emit(AgentEvent.ToolExecuted(result.result))
                }

                is BrokerResult.Failed -> {
                    session.addToolResult(result.result)
                    emit(AgentEvent.ToolFailed(result.result))
                }

                is BrokerResult.Blocked -> {
                    session.addToolResult(result.result)
                    emit(AgentEvent.ToolBlocked(result.result))
                }

                is BrokerResult.NeedsConfirmation -> {
                    emit(AgentEvent.Failed("Approved '${result.call.tool}' tool unexpectedly requested confirmation"))
                    return@flow
                }
            }

            if (executePendingToolCalls(session)) {
                runAgentLoop(session)
            }
        }
    }

    fun rejectConfirmation(confirmationId: String): Flow<AgentEvent> {
        return flow {
            val confirmation = confirmationManager.consume(confirmationId) ?: run {
                emit(AgentEvent.Failed("Unknown or expired confirmation: $confirmationId"))
                return@flow
            }

            val result = ToolResult(
                toolCallId = confirmation.call.id,
                tool = confirmation.call.tool,
                result = mapOf("message" to "Confirmation rejected")
            )

            val session = confirmation.session
            session.addToolResult(result)
            emit(AgentEvent.ToolBlocked(result))

            if (executePendingToolCalls(session)) {
                runAgentLoop(session)
            }
        }
    }

    private suspend fun FlowCollector<AgentEvent>.runAgentLoop(session: AgentSession) {
        while (session.stepCount < AgentDefaults.options.maxSteps) {
            when (val response = llmRuntime.generate(session.toLlmRequest())) {
                is LlmResponse.Final -> {
                    emit(AgentEvent.FinalAnswer(response.content))
                    return
                }

                is LlmResponse.ToolCalls -> {
                    if (response.calls.isEmpty()) {
                        emit(AgentEvent.Failed("LLM requested tool calls but returned an empty call list"))
                        return
                    }

                    session.addToolCalls(response.calls)

                    if (!executePendingToolCalls(session)) {
                        return
                    }
                }
            }
        }

        emit(AgentEvent.Failed("Tool step limit reached"))
    }

    private suspend fun FlowCollector<AgentEvent>.executePendingToolCalls(session: AgentSession): Boolean {
        while (true) {
            val call = session.consumeToolCall() ?: return true
            emit(AgentEvent.ToolStarted(call))

            when (val brokerResult = toolBroker.execute(call, session)) {
                is BrokerResult.Executed -> {
                    session.addToolResult(brokerResult.result)
                    emit(AgentEvent.ToolExecuted(brokerResult.result))
                }

                is BrokerResult.Failed -> {
                    session.addToolResult(brokerResult.result)
                    emit(AgentEvent.ToolFailed(brokerResult.result))
                }

                is BrokerResult.Blocked -> {
                    session.addToolResult(brokerResult.result)
                    emit(AgentEvent.ToolBlocked(brokerResult.result))
                }

                is BrokerResult.NeedsConfirmation -> {
                    val confirmation = confirmationManager.create(
                        session = session,
                        call = brokerResult.call,
                        title = brokerResult.title,
                        description = brokerResult.description
                    )
                    emit(
                        AgentEvent.ConfirmationRequired(
                            confirmationId = confirmation.id,
                            call = brokerResult.call,
                            title = brokerResult.title,
                            description = brokerResult.description
                        )
                    )
                    return false
                }
            }
        }
    }
}
