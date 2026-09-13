package com.kynex.ai.domain.model

enum class Role { USER, ASSISTANT }

data class AiModel(
    val id: String,
    val displayName: String,
    val provider: String
)

data class ChatMessage(
    val id: String = "",
    val role: Role = Role.ASSISTANT,
    val content: String = "",
    val modelId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)

data class Chat(
    val id: String = "",
    val title: String = "",
    val modelId: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val isSaved: Boolean = false,
    val messageCount: Long = 0L,
    val lastMessage: String = ""
) {
    val model: AiModel? get() = ModelRegistry.byId(modelId)
}

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val provider: String = "",
    val createdAt: Long = 0L
)

object ModelRegistry {
    fun byId(id: String): AiModel? = ALL_MODELS.firstOrNull { it.id == id }
}
