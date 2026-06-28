package com.her.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.her.app.data.AzureAIRepository
import com.her.app.data.FirestoreRepository
import com.her.app.data.Message
import com.her.app.data.Personality
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel : ViewModel() {

    private val repository = AzureAIRepository()
    private val firestoreRepo = FirestoreRepository()

    private val _currentPersonality = MutableStateFlow<Personality?>(null)
    val currentPersonality: StateFlow<Personality?> = _currentPersonality.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun selectPersonality(personality: Personality) {
        if (_currentPersonality.value?.id != personality.id) {
            _currentPersonality.value = personality
            _messages.value = emptyList()
            _errorMessage.value = null
            viewModelScope.launch {
                val history = try {
                    firestoreRepo.loadMessages(personality.id)
                } catch (e: Exception) {
                    emptyList()
                }
                if (history.isNotEmpty()) {
                    _messages.value = history
                } else {
                    sendGreeting(personality)
                }
            }
        }
    }

    private fun sendGreeting(personality: Personality) {
        viewModelScope.launch {
            _isLoading.value = true
            val triggerMessage = Message(
                id = UUID.randomUUID().toString(),
                content = "Hi! I just opened the app and chose to talk to you. Say hello and introduce yourself briefly.",
                isFromUser = true
            )
            val result = repository.sendMessage(personality, listOf(triggerMessage))
            result.onSuccess { reply ->
                val aiMessage = Message(
                    id = UUID.randomUUID().toString(),
                    content = reply,
                    isFromUser = false
                )
                _messages.value = listOf(aiMessage)
                viewModelScope.launch {
                    try {
                        firestoreRepo.saveMessage(personality.id, aiMessage)
                    } catch (e: Exception) {
                        // fire-and-forget; don't block UI on Firestore errors
                    }
                }
            }.onFailure {
                _errorMessage.value = "Couldn't connect right now. Check your API key and try again."
            }
            _isLoading.value = false
        }
    }

    fun sendMessage(text: String) {
        val personality = _currentPersonality.value ?: return
        if (text.isBlank()) return

        val userMessage = Message(
            id = UUID.randomUUID().toString(),
            content = text.trim(),
            isFromUser = true
        )

        val updatedMessages = _messages.value + userMessage
        _messages.value = updatedMessages

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repository.sendMessage(personality, updatedMessages)
            result.onSuccess { reply ->
                val aiMessage = Message(
                    id = UUID.randomUUID().toString(),
                    content = reply,
                    isFromUser = false
                )
                _messages.value = _messages.value + aiMessage
                // Persist both the user message and AI reply to Firestore
                viewModelScope.launch {
                    try {
                        firestoreRepo.saveMessage(personality.id, userMessage)
                        firestoreRepo.saveMessage(personality.id, aiMessage)
                    } catch (e: Exception) {
                        // fire-and-forget; don't block UI on Firestore errors
                    }
                }
            }.onFailure {
                _errorMessage.value = "Message failed. Please try again."
            }

            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
