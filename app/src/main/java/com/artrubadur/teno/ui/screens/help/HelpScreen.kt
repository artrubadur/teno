package com.artrubadur.teno.ui.screens.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.ui.components.AppLink
import com.artrubadur.teno.ui.components.ScreenHeader
import com.artrubadur.teno.ui.screens.help.components.GuideGroupList
import com.artrubadur.teno.ui.screens.help.components.GuideItem

@Composable
fun HelpScreen(onBack: () -> Unit, onRestartTour: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ScreenHeader("How it works", onBack)
            Button(
                onClick = onRestartTour,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Take a tour") }
            GuideGroupList("REMOTE MODELS") {
                GuideItem(
                    "What is a remote connection?",
                    "A remote connection sends your requests to a model hosted by a provider. You need a provider with an OpenAI-compatible Chat Completions API and tool calling. Create an API key in the provider's account dashboard, then copy its Base URL and model name from its documentation. In Teno, open Connections, tap +, choose Remote, and enter those values.\n\nUse the API base URL, not a chat website address. Teno adds /chat/completions automatically. The provider may charge for usage and handles the data sent to it."
                )
                GuideItem(
                    "Where can I get a remote connection?",
                    "Get it from a provider's developer platform. Create an account, enable API access or billing if required, and generate an API key. Then open the provider's setup guide to copy the Base URL and model ID. In Teno, open Connections, tap +, choose Remote, and enter these values. Select a model with tool calling support and check the provider's pricing before use.",
                    dividerAfter = false
                ) {
                    AppLink(
                        "OpenAI — Get started",
                        "https://platform.openai.com/docs/quickstart/make-your-first-api-request"
                    )
                    AppLink("OpenRouter — Get started", "https://openrouter.ai/docs/quickstart")
                    AppLink("DeepSeek — Get started", "https://api-docs.deepseek.com/")
                }
            }
            GuideGroupList("LOCAL MODELS") {
                GuideItem(
                    "What is a local connection?",
                    "A local connection runs the model directly on your device. It is free, independent of external providers, and works without an internet connection. Your data stays on the device and is not sent to anyone. You need a compatible .litertlm model file, enough storage, and enough memory to run it."
                )
                GuideItem(
                    "Where can I get a local model?",
                    "Local models run on your device after download. Browse LiteRT Community on Hugging Face and choose a model packaged as a .litertlm file. You need enough storage and memory to run the model."
                ) {
                    AppLink(
                        "Browse LiteRT Community",
                        "https://huggingface.co/litert-community/models"
                    )
                }
                GuideItem(
                    "Which model should I download?",
                    "Gemma 4 E2B is a balanced, medium-sized starting point suitable for most tasks. Choose gemma-4-E2B-it.litertlm from the links below. Performance depends on your device and the task; start with CPU and try another backend if supported. Device-specific NPU files are intended only for matching hardware."
                ) {
                    AppLink(
                        "Download page: Gemma 4 E2B",
                        "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/blob/main/gemma-4-E2B-it.litertlm"
                    )
                }
                GuideItem(
                    "How do I download from Hugging Face?",
                    "1. Open the model page and select \"Files and versions\".\n2. Find the file ending in .litertlm; ignore folders and documentation files.\n3. Tap its filename to open the file page, then use the download button. If the model requires access, sign in and accept its terms first.\n4. Wait for the download to finish in your browser. Download the model file, not the entire repository.\n5. Return to Teno, open Connections, tap +, choose Local, and select the downloaded file.",
                    dividerAfter = false
                ) {
                    AppLink(
                        "Open the Files tab",
                        "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/tree/main"
                    )
                    AppLink(
                        "Open the recommended file",
                        "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/blob/main/gemma-4-E2B-it.litertlm"
                    )
                }
            }
            GuideGroupList("USING TENO") {
                GuideItem(
                    "How do overlay and notifications work?",
                    "Turn on Overlay on Home. Allow display over other apps and notifications when Android asks. Once enabled, Teno's notification is available in the shade: swipe down from the top of the screen to access input and agent controls. The floating interface lets you use Teno while another app is open."
                )
                GuideItem(
                    "Why is an action unavailable?",
                    "Check Tools: the action must be enabled and its required permissions granted. Some apps limit what Android can read or interact with. You can stop a running task and approve or reject confirmation requests."
                )
                GuideItem(
                    "Where is my data processed?",
                    "With a local model, inference runs on your device. A remote connection uses the provider you choose. Teno has no servers of its own and does not collect your requests on Teno infrastructure. Connection details and preferences are saved on your device.",
                    dividerAfter = false
                )
            }
        }
    }
}
