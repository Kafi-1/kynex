package com.kynex.ai.data.firestore

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kynex.ai.domain.model.Chat
import com.kynex.ai.domain.model.ChatMessage
import com.kynex.ai.domain.model.Role
import com.kynex.ai.domain.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firestore data source. All access is scoped under users/{uid} so
 * security rules can enforce per-user isolation.
 */
class ChatRepository(private val uid: String) {

    private val db = FirebaseFirestore.getInstance()
    private val userDoc get() = db.collection("users").document(uid)
    private val chats get() = userDoc.collection("chats")

    // ---------- Chats ----------

    fun observeChats(): Flow<List<Chat>> = callbackFlow {
        val reg = chats
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .limit(200)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { it.toChat() }.orEmpty()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun createChat(title: String, modelId: String): Chat {
        val now = System.currentTimeMillis()
        val ref = chats
            .add(
                mapOf(
                    "title" to title,
                    "modelId" to modelId,
                    "createdAt" to now,
                    "updatedAt" to now,
                    "isSaved" to false,
                    "messageCount" to 0L,
                    "lastMessage" to ""
                )
            )
            .await()
        return Chat(
            id = ref.id,
            title = title,
            modelId = modelId,
            createdAt = now,
            updatedAt = now,
            messageCount = 0
        )
    }

    suspend fun renameChat(chatId: String, newTitle: String) {
        chats.document(chatId)
            .update(mapOf("title" to newTitle, "updatedAt" to System.currentTimeMillis()))
            .await()
    }

    suspend fun setSaved(chatId: String, saved: Boolean) {
        chats.document(chatId).update(mapOf("isSaved" to saved)).await()
    }

    suspend fun updateChatModel(chatId: String, modelId: String) {
        chats.document(chatId).update(mapOf("modelId" to modelId)).await()
    }

    suspend fun deleteChat(chatId: String) {
        val msgRefs = chats.document(chatId).collection("messages").get().await()
        db.runBatch { batch ->
            msgRefs.documents.forEach { batch.delete(it.reference) }
            batch.delete(chats.document(chatId))
        }.await()
    }

    // ---------- Messages ----------

    fun observeMessages(chatId: String, limit: Long = 60): Flow<List<ChatMessage>> = callbackFlow {
        val reg = chats.document(chatId).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .limitToLast(limit)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { it.toMessage() }.orEmpty()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun appendMessage(chatId: String, message: ChatMessage) {
        chats.document(chatId).collection("messages").document(message.id).set(
            mapOf(
                "role" to message.role.name,
                "content" to message.content,
                "modelId" to (message.modelId ?: ""),
                "createdAt" to message.createdAt,
                "isError" to message.isError
            )
        ).await()
    }

    suspend fun deleteMessage(chatId: String, messageId: String) {
        chats.document(chatId).collection("messages").document(messageId).delete().await()
    }

    suspend fun touchChat(
        chatId: String,
        modelId: String? = null,
        title: String? = null,
        lastMessage: String,
        countDelta: Long
    ) {
        val updates = mutableMapOf<String, Any>(
            "updatedAt" to System.currentTimeMillis(),
            "lastMessage" to lastMessage.take(120),
            "messageCount" to FieldValue.increment(countDelta)
        )
        modelId?.let { updates["modelId"] = it }
        title?.let { updates["title"] = it }
        chats.document(chatId).update(updates).await()
    }

    // ---------- Profile & settings ----------

    suspend fun loadProfile(): UserProfile? {
        val snap = userDoc.get().await()
        if (!snap.exists()) return null
        return UserProfile(
            name = snap.getString("name") ?: "",
            email = snap.getString("email") ?: "",
            photoUrl = snap.getString("photoUrl") ?: "",
            provider = snap.getString("provider") ?: "",
            createdAt = snap.getLong("createdAt") ?: 0L
        )
    }

    suspend fun updateDisplayName(name: String) {
        userDoc.update(mapOf("name" to name, "updatedAt" to System.currentTimeMillis())).await()
    }

    suspend fun saveSettings(selectedModel: String, theme: String) {
        userDoc.collection("settings").document("main").set(
            mapOf("selectedModel" to selectedModel, "theme" to theme, "updatedAt" to System.currentTimeMillis())
        ).await()
    }

    // ---------- Mapping ----------

    private fun DocumentSnapshot.toChat(): Chat? = runCatching {
        Chat(
            id = id,
            title = getString("title") ?: "Untitled",
            modelId = getString("modelId") ?: "",
            createdAt = getLong("createdAt") ?: 0L,
            updatedAt = getLong("updatedAt") ?: 0L,
            isSaved = getBoolean("isSaved") ?: false,
            messageCount = getLong("messageCount") ?: 0L,
            lastMessage = getString("lastMessage") ?: ""
        )
    }.getOrNull()

    private fun DocumentSnapshot.toMessage(): ChatMessage? = runCatching {
        ChatMessage(
            id = id,
            role = if (getString("role") == "USER") Role.USER else Role.ASSISTANT,
            content = getString("content") ?: "",
            modelId = getString("modelId"),
            createdAt = getLong("createdAt") ?: 0L,
            isError = getBoolean("isError") ?: false
        )
    }.getOrNull()
}
