package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import com.example.BuildConfig
import com.example.FreeAiApplication

enum class MessageStatus { SENDING, DELIVERED, ERROR }

data class ChatMessage(val id: String = java.util.UUID.randomUUID().toString(), val text: String, val isUser: Boolean, val status: MessageStatus = MessageStatus.DELIVERED)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as FreeAiApplication).database.chatDao()

    val availableModels = listOf("gemini-3.5-flash", "gemini-3.1-pro-preview", "gemini-3.1-flash-lite")
    
    private val _selectedModel = MutableStateFlow(availableModels[0])
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage(text = "Welcome to FreeAI Chat", isUser = false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _sessions = MutableStateFlow<List<ChatEntity>>(emptyList())
    val sessions: StateFlow<List<ChatEntity>> = _sessions.asStateFlow()
    
    private var currentChatId: Long? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            db.getAllChats().collect { chatList ->
                _sessions.value = chatList
            }
        }
    }

    fun selectModel(model: String) {
        _selectedModel.value = model
    }

    fun createNewSession() {
        currentChatId = null
        _messages.value = listOf(ChatMessage(text = "Welcome to FreeAI Chat", isUser = false))
    }

    fun loadSession(chatId: Long) {
        currentChatId = chatId
        viewModelScope.launch(Dispatchers.IO) {
            db.getMessagesForChat(chatId).collect { msgs ->
                _messages.value = msgs.map { ChatMessage(text = it.text, isUser = it.isUser) }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(text = text, isUser = true, status = MessageStatus.SENDING)
        _messages.value = _messages.value + userMessage
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            if (currentChatId == null) {
                val title = text.take(20) + "..."
                currentChatId = db.insertChat(ChatEntity(title = title))
            }
            val cid = currentChatId!!
            
            db.insertMessage(MessageEntity(chatId = cid, text = text, isUser = true))

            try {
                // Prepare context history for Gemini
                val contents = _messages.value.map { msg ->
                    Content(parts = listOf(Part(text = msg.text)))
                }

                val request = GenerateContentRequest(contents = contents)
                val apiKey = BuildConfig.GEMINI_API_KEY

                val response = RetrofitClient.service.generateContent(_selectedModel.value, apiKey, request)
                val replyText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response from AI."
                
                db.insertMessage(MessageEntity(chatId = cid, text = replyText, isUser = false))
                
                // Update live state with delivered status
                _messages.value = _messages.value.map { if (it.id == userMessage.id) it.copy(status = MessageStatus.DELIVERED) else it } + ChatMessage(text = replyText, isUser = false)
            } catch (e: Exception) {
                val errorMsg = "Error: ${e.localizedMessage}"
                db.insertMessage(MessageEntity(chatId = cid, text = errorMsg, isUser = false))
                _messages.value = _messages.value.map { if (it.id == userMessage.id) it.copy(status = MessageStatus.ERROR) else it } + ChatMessage(text = errorMsg, isUser = false)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
