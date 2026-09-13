package com.kynex.ai.data.network

import com.kynex.ai.domain.model.ChatMessage
import com.kynex.ai.domain.model.Role
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Talks to AgentRouter (OpenAI-compatible chat completions API).
 * Streaming is primary; [chatOnce] is the non-streaming fallback.
 */
class AgentRouterService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun buildBody(modelId: String, history: List<ChatMessage>, stream: Boolean): String {
        val messages = JSONArray()
        messages.put(
            JSONObject()
                .put("role", "system")
                .put("content", AiConfig.SYSTEM_PROMPT)
        )
        history
            .filter { !it.isError && it.content.isNotBlank() }
            .forEach { m ->
                messages.put(
                    JSONObject()
                        .put("role", if (m.role == Role.USER) "user" else "assistant")
                        .put("content", m.content)
                )
            }
        return JSONObject()
            .put("model", modelId)
            .put("messages", messages)
            .put("stream", stream)
            .toString()
    }

    private fun request(body: String): Request =
        Request.Builder()
            .url(AiConfig.BASE_URL + "chat/completions")
            .header("Authorization", "Bearer ${AiConfig.API_KEY}")
            .header("User-Agent", AiConfig.CLIENT_USER_AGENT)
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

    /** Emits incremental text deltas. Cancelling collection cancels the HTTP call. */
    fun streamChat(modelId: String, history: List<ChatMessage>): Flow<String> = channelFlow {
        val body = withContext(Dispatchers.IO) { buildBody(modelId, history, stream = true) }
        val call = client.newCall(request(body))

        launch(Dispatchers.IO) {
            try {
                call.execute().use { response ->
                    if (!response.isSuccessful) {
                        val err = response.body?.string()?.take(500).orEmpty()
                        close(IOException("HTTP ${response.code}${if (err.isNotBlank()) ": $err" else ""}"))
                        return@launch
                    }
                    val source = response.body?.source()
                    if (source == null) {
                        close(IOException("Empty response body"))
                        return@launch
                    }
                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: break
                        if (!line.startsWith("data:")) continue
                        val payload = line.removePrefix("data:").trim()
                        if (payload == "[DONE]") break
                        val delta = extractDelta(payload) ?: continue
                        if (delta.isNotEmpty()) send(delta)
                    }
                    close()
                }
            } catch (e: Exception) {
                close(e)
            }
        }

        awaitClose { call.cancel() }
    }

    private fun extractDelta(payload: String): String? = try {
        val json = JSONObject(payload)
        val choice = json.optJSONArray("choices")?.optJSONObject(0) ?: return null
        val delta = choice.optJSONObject("delta") ?: return null
        if (delta.has("content")) delta.optString("content") else ""
    } catch (e: Exception) {
        null
    }

    /** Non-streaming request/response. */
    suspend fun chatOnce(modelId: String, history: List<ChatMessage>): String =
        withContext(Dispatchers.IO) {
            val body = buildBody(modelId, history, stream = false)
            client.newCall(request(body)).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (!response.isSuccessful) throw IOException("HTTP ${response.code}: ${text.take(300)}")
                JSONObject(text)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
            }
        }
}
