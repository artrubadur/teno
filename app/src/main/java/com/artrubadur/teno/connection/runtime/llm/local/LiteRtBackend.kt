package com.artrubadur.teno.connection.runtime.llm.local

import android.content.Context
import com.google.ai.edge.litertlm.Backend

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