package com.artrubadur.teno.agent.controller

import kotlinx.serialization.json.Json

object AgentControllerMessengerContract {
    const val MSG_REGISTER = 1
    const val MSG_UNREGISTER = 2
    const val MSG_COMMAND = 3
    const val MSG_EVENT = 4
    const val KEY_PAYLOAD = "payload"

    val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }
}