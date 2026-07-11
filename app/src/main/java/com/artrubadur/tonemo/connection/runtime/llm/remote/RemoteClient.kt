package com.artrubadur.tonemo.connection.runtime.llm.remote

import android.util.Log
import com.artrubadur.tonemo.connection.RemoteConnectionConfig
import com.artrubadur.tonemo.connection.runtime.llm.LlmException
import com.artrubadur.tonemo.connection.runtime.llm.LlmRequest
import com.artrubadur.tonemo.connection.runtime.llm.LlmResponse
import com.artrubadur.tonemo.connection.runtime.llm.remote.dto.ApiResponse
import com.artrubadur.tonemo.connection.runtime.llm.remote.mappers.toApiRequest
import com.artrubadur.tonemo.connection.runtime.llm.remote.mappers.toLlmResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class RemoteClient(
    private val config: RemoteConnectionConfig,
    private val httpClient: HttpClient
) {
    suspend fun generate(
        request: LlmRequest
    ): LlmResponse {
        val apiRequest = request.toApiRequest(
            model = config.model
        )

        val response = httpClient.post(
            urlString = buildUrl(config.baseUrl)
        ) {
            contentType(ContentType.Application.Json)
            bearerAuth(config.apiKey)
            setBody(apiRequest)
        }

        if (!response.status.isSuccess()) {
            val body = response.bodyAsText()

            throw LlmException.GenerationFailed(
                message = "${response.status.value} - $body",
                cause = null,
            )
        }

        val apiResponse: ApiResponse = response.body()

        Log.d(
            TAG,
            "usage=" +
                    "prompt=${apiResponse.usage.promptTokens}, " +
                    "cached=${apiResponse.usage.promptTokensDetails?.cachedTokens ?: 0}, " +
                    "completion=${apiResponse.usage.completionTokens}, " +
                    "total=${apiResponse.usage.totalTokens}"
        )

        return apiResponse.toLlmResponse()
    }

    fun close() {
        httpClient.close()
    }

    private fun buildUrl(baseUrl: String): String {
        return "${baseUrl.trimEnd('/')}/chat/completions"
    }

    private companion object {
        const val TAG = "RemoteClient"
    }
}