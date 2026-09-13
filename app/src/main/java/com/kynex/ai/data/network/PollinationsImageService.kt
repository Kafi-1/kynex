package com.kynex.ai.data.network

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Pollinations image generation via the official OpenAI-compatible endpoint
 * POST {BASE_URL}/v1/images/generations.
 *
 * Request:  { prompt, model, n, size, response_format }
 * Response: { created, data: [{ url | b64_json, media_type, revised_prompt }], usage }
 *
 * Auth (per official spec): Authorization: Bearer <pk_ or sk_ key>.
 * The key is read from ImageGenConfig and NEVER logged.
 */
class PollinationsImageService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Generates an image and returns the decoded image bytes.
     * Handles both b64_json and url response formats.
     */
    suspend fun generate(prompt: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject()
                .put("prompt", prompt)
                .put("model", ImageGenConfig.DEFAULT_MODEL)
                .put("n", 1)
                .put("size", ImageGenConfig.DEFAULT_SIZE)
                .put("response_format", ImageGenConfig.RESPONSE_FORMAT)
                .toString()

            val request = Request.Builder()
                .url("${ImageGenConfig.BASE_URL}/v1/images/generations")
                .header("Authorization", "Bearer ${ImageGenConfig.appKey}")
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val text = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(mapHttpError(response.code, text))
                }
                parseImage(text)
            }
        } catch (e: Exception) {
            Result.failure(friendly(e))
        }
    }

    private fun parseImage(text: String): Result<ByteArray> {
        val json = try {
            JSONObject(text)
        } catch (e: Exception) {
            return Result.failure(ImageGenException("The server returned an invalid response. Please try again."))
        }
        val data = json.optJSONArray("data")
            ?: return Result.failure(ImageGenException("Empty response from the image service. Please try again."))
        val first = data.optJSONObject(0)
            ?: return Result.failure(ImageGenException("No image was returned. Please try again."))

        // b64_json path (documented default)
        val b64 = first.optString("b64_json", "")
        if (b64.isNotBlank()) {
            return try {
                Result.success(Base64.decode(b64, Base64.DEFAULT))
            } catch (e: Exception) {
                Result.failure(ImageGenException("The returned image data could not be decoded."))
            }
        }

        // url path fallback
        val url = first.optString("url", "")
        if (url.isNotBlank()) {
            return downloadImage(url)
        }

        return Result.failure(ImageGenException("The response contained no image. Please try again."))
    }

    private fun downloadImage(url: String): Result<ByteArray> = try {
        client.newCall(Request.Builder().url(url).build()).execute().use { response ->
            if (!response.isSuccessful) {
                Result.failure(mapHttpError(response.code, ""))
            } else {
                val bytes = response.body?.bytes()
                if (bytes.isNullOrEmpty()) {
                    Result.failure(ImageGenException("The image could not be downloaded. Please try again."))
                } else {
                    Result.success(bytes)
                }
            }
        }
    } catch (e: Exception) {
        Result.failure(friendly(e))
    }

    private fun mapHttpError(code: Int, body: String): ImageGenException {
        // Never include the raw body in user messages — it may echo request details.
        return when (code) {
            401 -> ImageGenException("Authentication failed — check the Pollinations App Key.")
            402 -> ImageGenException("Insufficient Pollen balance or key budget exhausted.")
            403 -> ImageGenException("This App Key does not have permission for that model.")
            404 -> ImageGenException("Model not found — check the model name in the configuration.")
            429 -> ImageGenException("Rate limit reached. Please wait a moment and try again.")
            in 500..599 -> ImageGenException("Pollinations server error. Please try again later.")
            else -> ImageGenException("Image generation failed (HTTP $code). Please try again.")
        }
    }

    private fun friendly(e: Exception): Exception = when {
        e is ImageGenException -> e
        e.message?.contains("timeout", ignoreCase = true) == true ->
            ImageGenException("The request timed out. Please try again.")
        e.message?.contains("Unable to resolve", ignoreCase = true) == true ||
            e.message?.contains("network", ignoreCase = true) == true ->
            ImageGenException("No internet connection. Please check and try again.")
        else -> ImageGenException("Something went wrong. Please try again.")
    }
}

class ImageGenException(message: String) : Exception(message)
