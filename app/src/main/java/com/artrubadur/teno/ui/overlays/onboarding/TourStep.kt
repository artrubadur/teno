package com.artrubadur.teno.ui.overlays.onboarding

data class TourStep(
    val title: String,
    val text: String,
    val route: String = "home",
    val target: String? = null,
    val notification: Boolean = false,
)

val tourSteps = listOf(
    TourStep(
        "Welcome to Teno",
        "Teno lets you work with an AI agent on your device. This short tour shows the main parts of the app."
    ),
    TourStep(
        "Choose your model",
        "Connections is where you add and select a model. Teno supports local models and remote providers.",
        target = "connections"
    ),
    TourStep(
        "Connections",
        "Add a local model or a remote provider here. Local models run on your device. Remote connections use an OpenAI-compatible API.",
        route = "connections",
        target = "add-connection"
    ),
    TourStep(
        "Talk to the agent",
        "Chat is where you send requests to the agent and follow its work.",
        target = "chat"
    ),
    TourStep(
        "Chat",
        "Select a connection, launch it, and send a request. The timeline shows progress, results, and confirmation requests. Chat remembers context only within the current request.",
        route = "chat",
        target = "chat-connection"
    ),
    TourStep(
        "Control available actions",
        "Tools controls what the agent can do on your device.",
        target = "tools"
    ),
    TourStep(
        "Tools and permissions",
        "Expand an action to read its description and required permissions. Enable the actions you want and grant permissions when needed.",
        route = "settings/tools",
        target = "tool-details-click_screen_node"
    ),
    TourStep(
        "Make Teno yours",
        "Settings contains the agent's instructions and generation preferences.",
        target = "settings"
    ),
    TourStep(
        "Settings",
        "Identity and Rules shape the agent's behavior. Settings also includes model generation parameters.",
        route = "settings",
        target = "identity"
    ),
    TourStep(
        "Keep Teno within reach",
        "Overlay keeps Teno available above other apps. It can be opened from the notification shade.",
        target = "overlay"
    ),
    TourStep(
        "Find Teno in the notification shade",
        "Swipe down from the top of the screen to find Teno's notification and open the overlay controls.",
        notification = true
    ),
    TourStep(
        "You're ready",
        "Add a connection, start a chat, and use Tools to choose what the agent can do. Open How it works for more guides."
    ),
)
