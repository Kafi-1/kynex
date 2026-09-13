package com.kynex.ai.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Pollinations image generation via the keyless legacy endpoint
 * GET image.pollinations.ai/prompt/{prompt}. The image is returned
 * directly as bytes.
 */
class PollinationsImageService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Generates an image via the keyless legacy endpoint
     * GET image.pollinations.ai/prompt/{prompt}. The image is returned
     * directly as bytes.
     */
    suspend fun generate(prompt: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        val encoded = java.net.URLEncoder.encode(prompt, "UTF-8")
        val url = "${ImageGenConfig.BASE_URL}/prompt/$encoded" +
            "?width=${ImageGenConfig.DEFAULT_WIDTH}" +
            "&height=${ImageGenConfig.DEFAULT_HEIGHT}" +
            "&model=${ImageGenConfig.DEFAULT_MODEL}" +
            "&nologo=true"
        downloadImage(url)
    }

    private fun downloadImage(url: String): Result<ByteArray> = try {
        client.newCall(Request.Builder().url(url).build()).execute().use { response ->
            if (!response.isSuccessful) {
                Result.failure(mapHttpError(response.code, ""))
            } else {
                val bytes = response.body?.bytes()
                if (bytes == null || bytes.isEmpty()) {
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
