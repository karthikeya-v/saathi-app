package com.her.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun userId() = auth.currentUser?.uid ?: throw IllegalStateException("Not signed in")

    suspend fun saveProfile(profile: UserProfile) {
        db.collection("users")
            .document(userId())
            .set(mapOf(
                "name" to profile.name,
                "age" to profile.age,
                "gender" to profile.gender,
                "interestedIn" to profile.interestedIn,
                "vibe" to profile.vibe
            ))
            .await()
    }

    suspend fun loadProfile(): UserProfile? {
        val snapshot = db.collection("users")
            .document(userId())
            .get()
            .await()
        if (!snapshot.exists()) return null
        val name = snapshot.getString("name") ?: return null
        if (name.isBlank()) return null
        return UserProfile(
            name = name,
            age = (snapshot.getLong("age") ?: 0L).toInt(),
            gender = snapshot.getString("gender") ?: "",
            interestedIn = snapshot.getString("interestedIn") ?: "",
            vibe = snapshot.getString("vibe") ?: ""
        )
    }

    suspend fun saveMessage(personalityId: String, message: Message) {
        val docRef = db.collection("users")
            .document(userId())
            .collection("chats")
            .document(personalityId)

        db.runTransaction { tx ->
            val snapshot = tx.get(docRef)
            val existing = if (snapshot.exists()) {
                @Suppress("UNCHECKED_CAST")
                (snapshot.get("messages") as? List<Map<String, Any>>) ?: emptyList()
            } else emptyList()

            val newMessage = mapOf(
                "id" to message.id,
                "content" to message.content,
                "isFromUser" to message.isFromUser,
                "timestamp" to System.currentTimeMillis()
            )

            tx.set(docRef, mapOf(
                "messages" to existing + newMessage,
                "lastUpdated" to System.currentTimeMillis(),
                "personalityId" to personalityId
            ))
        }.await()
    }

    suspend fun loadMessages(personalityId: String): List<Message> {
        val docRef = db.collection("users")
            .document(userId())
            .collection("chats")
            .document(personalityId)

        val snapshot = docRef.get().await()
        if (!snapshot.exists()) return emptyList()

        @Suppress("UNCHECKED_CAST")
        val rawMessages = (snapshot.get("messages") as? List<Map<String, Any>>) ?: return emptyList()

        return rawMessages.map { m ->
            Message(
                id = m["id"] as? String ?: "",
                content = m["content"] as? String ?: "",
                isFromUser = m["isFromUser"] as? Boolean ?: true
            )
        }
    }

    suspend fun clearChat(personalityId: String) {
        db.collection("users")
            .document(userId())
            .collection("chats")
            .document(personalityId)
            .delete()
            .await()
    }

    suspend fun deleteAllUserData() {
        val chats = db.collection("users")
            .document(userId())
            .collection("chats")
            .get()
            .await()

        db.runBatch { batch ->
            chats.documents.forEach { batch.delete(it.reference) }
            batch.delete(db.collection("users").document(userId()))
        }.await()
    }
}
