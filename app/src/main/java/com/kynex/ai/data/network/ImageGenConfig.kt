package com.kynex.ai.data.network

/**
 * CENTRAL image-generation configuration.
 * Uses Pollinations' keyless legacy endpoint (image.pollinations.ai/prompt/...),
 * which needs no API key. The pk_ App Key has no model permissions on
 * gen.pollinations.ai, so it is not used.
 */
object ImageGenConfig {

    const val BASE_URL = "https://image.pollinations.ai"

    // Free tier model on the legacy endpoint.
    const val DEFAULT_MODEL = "flux"

    const val DEFAULT_WIDTH = 1024

    const val DEFAULT_HEIGHT = 1024
}
