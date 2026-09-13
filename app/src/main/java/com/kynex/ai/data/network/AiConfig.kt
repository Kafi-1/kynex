package com.kynex.ai.data.network

import com.kynex.ai.domain.model.AiModel

/**
 * CENTRAL API CONFIGURATION — the only place where AgentRouter credentials
 * and model IDs live. Change values here, rebuild the app.
 */
object AiConfig {

    const val BASE_URL = "https://agentrouter.org/v1/"

    // AgentRouter API key. Owner understands keys embedded in an APK
    // cannot be considered fully secret (personal-use app).
    // WARNING: if this repository is public, move this to local.properties.
    const val API_KEY = "sk-fFxMDIz4hePBvOGcQ68fY8sNOTKWAuULefwoJME2aMM64RtG"

    const val SYSTEM_PROMPT =
        "You are Kynex AI, a helpful, accurate and friendly assistant. " +
            "Use Markdown formatting when helpful, and fenced code blocks with a language tag for code."

    /**
     * displayName is what the user sees.
     * id is the actual API model ID sent to AgentRouter.
     * VERIFY these IDs against your AgentRouter account's model list —
     * adjust here only, never elsewhere in the app.
     */
    val MODELS: List<AiModel> = listOf(
        AiModel(id = "gpt-5.6-sol", displayName = "GPT-5.6-Sol", provider = "OpenAI"),
        AiModel(id = "claude-opus-4.8", displayName = "Claude Opus 4.8", provider = "Anthropic"),
        AiModel(id = "claude-opus-5", displayName = "Claude Opus 5", provider = "Anthropic"),
        AiModel(id = "deepseek-v4-flash", displayName = "DeepSeek V4 Flash", provider = "DeepSeek"),
        AiModel(id = "glm-5.3", displayName = "GLM 5.3", provider = "Zhipu")
    )

    val DEFAULT_MODEL: AiModel get() = MODELS.first()
}
