package com.kynex.ai.data.network

import com.kynex.ai.BuildConfig

/**
 * CENTRAL image-generation configuration.
 * The App Key is injected from local.properties at build time
 * (BuildConfig.POLLINATIONS_APP_KEY) and is never committed to git.
 *
 * Auth flow per official Pollinations OpenAPI spec (gen.pollinations.ai):
 * "Include your API key as Authorization: Bearer YOUR_API_KEY".
 */
object ImageGenConfig {

    const val BASE_URL = "https://gen.pollinations.ai"

    // Change this to any Pollinations-supported image model later.
    const val DEFAULT_MODEL = "nanobanana-2"

    const val DEFAULT_SIZE = "1024x1024"

    // b64_json is the documented default; we decode it directly.
    const val RESPONSE_FORMAT = "b64_json"

    val appKey: String get() = BuildConfig.POLLINATIONS_APP_KEY
}
