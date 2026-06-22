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
    private val providerManager = (application as FreeAiApplication).providerManager

    private val _availableModels = MutableStateFlow<List<String>>(listOf("gemini-3.5-flash"))
    val availableModels: StateFlow<List<String>> = _availableModels.asStateFlow()
    
    private val _selectedModel = MutableStateFlow("gemini-3.5-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _systemPrompt = MutableStateFlow("")
    val systemPrompt: StateFlow<String> = _systemPrompt.asStateFlow()

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
        viewModelScope.launch(Dispatchers.IO) {
            val loadedModels = providerManager.listAllModels()
            val models = loadedModels.map { pair -> pair.first }
            if (models.isNotEmpty()) {
                _availableModels.value = models
                if (!_availableModels.value.contains(_selectedModel.value)) {
                    _selectedModel.value = models.first()
                }
            }
        }
    }

    fun setSystemPrompt(prompt: String) {
        _systemPrompt.value = prompt
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
                // Prepare context history for Gemini using the service
                val messagePairs = _messages.value.map { msg ->
                    Pair(msg.text, msg.isUser)
                }

                val providerManager = (getApplication<Application>() as FreeAiApplication).providerManager
                val provider = providerManager.getProviderForModel(_selectedModel.value)
                val replyText = if (provider != null) {
                    val result = provider.chat(messagePairs, _selectedModel.value, _systemPrompt.value.takeIf { it.isNotBlank() })
                    result.getOrElse { "Error: ${it.message}" }
                } else {
                    "Error: No provider found for model ${_selectedModel.value}."
                }
                
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
