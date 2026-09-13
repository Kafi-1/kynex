package com.kynex.ai.data.network

/**
 * CENTRAL image-generation configuration.
 * Uses Pollinations' keyless legacy endpoint (image.pollinations.ai/prompt/...),
 * which needs no API key. The pk_ App Key has no model permissions on
 * gen.pollinations.ai, so it is not used.
 */
object ImageGenConfig {

    const val BASE_URL = "https://image.pollinations.ai"

    // gptimage follows prompts much better than the free flux model.
    const val DEFAULT_MODEL = "gptimage"

    const val DEFAULT_WIDTH = 1024

    const val DEFAULT_HEIGHT = 1024
}
