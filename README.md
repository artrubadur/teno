# Teno: Yours, by design.

> Teno is an Android application with an agent that completes user tasks using a language model and
> the device’s system capabilities.

Teno is distributed for free as an open-source project under the GPLv3 license. It is not tied to a
single model provider and does not require a cloud service: the agent can run locally with a
LiteRT-LM model or connect to an OpenAI-compatible API chosen by the user. This keeps the choice of
model, provider, and data-processing location in the user’s hands. Teno has no own servers: the
application does not send data to Teno infrastructure or store it outside the device. Access to
device actions is controlled by settings, Android permissions, and confirmation policies.

## Features and usage

You can send requests in the chat or through a floating overlay displayed over other applications.
The agent selects an available action, shows its progress, and returns the result.

Remote model connections are configured with a Base URL, model name, and API key. For local
operation, import a compatible `.litertlm` file. On supported devices, you can choose CPU, GPU, or
NPU execution.

Using the screen and Android system functions may require granting permissions manually. The
available actions depend on the permissions that have been granted.

The main application sections are Home, Chat, Connections, Tools, and Settings. Settings also
contains the agent instructions and generation parameters.

## Remote connections

The remote runtime uses an OpenAI-compatible API: it appends `/chat/completions` to the configured
Base URL and sends the request with a Bearer API key. The provider must support chat completions and
function/tool calling in a compatible format.

## Architecture

The application is split into the UI, data layer, language model runtime, and agent loop.
`RoutingLlmRuntime` selects the local or remote runtime. `AgentOrchestrator` manages the session: it
sends requests to the model, handles action calls, returns action results to the model, and
publishes events for the UI.

Before an action is executed, `ToolBroker` checks its availability, permissions, and risk level.
`SafetyPolicy` allows safe actions, requests confirmation, or blocks the action. Connection settings
are stored with Room; user preferences are stored with DataStore.

## Limitations

- Local operation requires a compatible `.litertlm` model and sufficient device resources.
- Screen-control capabilities depend on Android Accessibility Service and restrictions imposed by
  the target application.
- NPU support depends on the device SoC and the LiteRT-LM version.

## License

The project is distributed under the [GPL-3.0](LICENSE) license.
