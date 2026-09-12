package com.artrubadur.teno.connection.runtime.llm.local

import android.content.Context
import android.os.Build
import com.artrubadur.teno.R
import com.google.ai.edge.litertlm.Backend
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

sealed class LiteRtBackend {

    data object Cpu : LiteRtBackend()

    data object Gpu : LiteRtBackend()

    data class Npu(
        val nativeLibraryDir: String? = null
    ) : LiteRtBackend()

    fun toLiteRtBackend(context: Context): Backend {
        return when (this) {
            Cpu -> Backend.CPU()

            Gpu -> Backend.GPU()

            is Npu -> Backend.NPU(
                nativeLibraryDir = nativeLibraryDir
                    ?: context.applicationInfo.nativeLibraryDir
            )
        }
    }
}

enum class LiteRtBackendOption(
    val title: String,
) {
    CPU("CPU"),
    GPU("GPU"),
    NPU("NPU");

    fun toBackend(): LiteRtBackend = when (this) {
        CPU -> LiteRtBackend.Cpu
        GPU -> LiteRtBackend.Gpu
        NPU -> LiteRtBackend.Npu()
    }

    companion object {
        fun fromStored(value: String?): LiteRtBackendOption =
            entries.firstOrNull { option -> option.name == value } ?: CPU
    }
}

object LiteRtNpuSupport {
    fun supportedSoCs(context: Context): List<SupportedSoC> =
        runCatching {
            context.resources.openRawResource(R.raw.litert_npu_supported_socs)
                .bufferedReader()
                .use { reader ->
                    json.decodeFromString(supportedSoCListSerializer, reader.readText())
                }
        }.getOrDefault(emptyList())

    fun isSupported(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
        if ("arm64-v8a" !in Build.SUPPORTED_ABIS) return false

        val deviceInfo = listOf(
            Build.SOC_MODEL,
            Build.SOC_MANUFACTURER,
            Build.HARDWARE,
            Build.BOARD,
            Build.DEVICE,
        ).joinToString(separator = " ").normalizedSoC()

        return supportedSoCs(context).any { soc -> soc.code.normalizedSoC() in deviceInfo }
    }

    private fun String.normalizedSoC(): String =
        uppercase().filter { char -> char.isLetterOrDigit() }

    private val json = Json { ignoreUnknownKeys = true }
    private val supportedSoCListSerializer = ListSerializer(SupportedSoC.serializer())
}

@Serializable
data class SupportedSoC(
    val vendor: String,
    val code: String,
    val name: String,
)
