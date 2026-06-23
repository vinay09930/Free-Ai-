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

    private val _availableModels = MutableStateFlow<List<Pair<String, String>>>(listOf(Pair("gemini-3.5-flash", "Gemini API")))
    val availableModels: StateFlow<List<Pair<String, String>>> = _availableModels.asStateFlow()
    
    private val _selectedModel = MutableStateFlow("gemini-3.5-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()
    
    private val _selectedProviderName = MutableStateFlow("Gemini API")
    val selectedProviderName: StateFlow<String> = _selectedProviderName.asStateFlow()

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
            if (loadedModels.isNotEmpty()) {
                _availableModels.value = loadedModels
                if (_availableModels.value.none { it.first == _selectedModel.value }) {
                    val first = loadedModels.first()
                    _selectedModel.value = first.first
                    _selectedProviderName.value = first.second
                } else {
                    _selectedProviderName.value = loadedModels.first { it.first == _selectedModel.value }.second
                }
            }
        }
    }

    fun setSystemPrompt(prompt: String) {
        _systemPrompt.value = prompt
    }

    fun selectModel(model: String, providerName: String) {
        _selectedModel.value = model
        _selectedProviderName.value = providerName
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
                if (provider != null) {
                    var isFirstToken = true
                    var accumulatedText = ""
                    val aiMessageId = java.util.UUID.randomUUID().toString()
                    
                    provider.chatStream(messagePairs, _selectedModel.value, _systemPrompt.value.takeIf { it.isNotBlank() })
                        .collect { result ->
                            if (result.isSuccess) {
                                accumulatedText += result.getOrNull() ?: ""
                                if (isFirstToken) {
                                    isFirstToken = false
                                    // Add the initial ai message to the UI list and set user message to DELIVERED
                                    _messages.value = _messages.value.map { if (it.id == userMessage.id) it.copy(status = MessageStatus.DELIVERED) else it } + ChatMessage(id = aiMessageId, text = accumulatedText, isUser = false)
                                } else {
                                    // Update existing ai message
                                    _messages.value = _messages.value.map { if (it.id == aiMessageId) it.copy(text = accumulatedText) else it }
                                }
                            } else {
                                val errorMsg = "Error: ${result.exceptionOrNull()?.message}"
                                accumulatedText += "\n$errorMsg"
                                if (isFirstToken) {
                                    isFirstToken = false
                                    _messages.value = _messages.value.map { if (it.id == userMessage.id) it.copy(status = MessageStatus.ERROR) else it } + ChatMessage(id = aiMessageId, text = errorMsg, isUser = false, status = MessageStatus.ERROR)
                                } else {
                                    _messages.value = _messages.value.map { if (it.id == aiMessageId) it.copy(text = accumulatedText, status = MessageStatus.ERROR) else it }
                                }
                            }
                        }
                    
                    // After collection completes, safe to save.
                    db.insertMessage(MessageEntity(chatId = cid, text = accumulatedText, isUser = false))
                } else {
                    val errorMsg = "Error: No provider found for model ${_selectedModel.value}."
                    db.insertMessage(MessageEntity(chatId = cid, text = errorMsg, isUser = false))
                    _messages.value = _messages.value.map { if (it.id == userMessage.id) it.copy(status = MessageStatus.ERROR) else it } + ChatMessage(text = errorMsg, isUser = false, status = MessageStatus.ERROR)
                }
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
