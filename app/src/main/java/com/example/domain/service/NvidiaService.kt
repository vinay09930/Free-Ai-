package com.example.domain.service

import com.example.BuildConfig
import com.example.data.OpenAIChatRequest
import com.example.data.OpenAIMessage
import com.example.data.RetrofitClient
import com.example.domain.provider.ProviderAdapter

class NvidiaService : ProviderAdapter {
    override val id: String = "provider_nvidia"
    override val name: String = "Nvidia API"
    
    // Nvidia NIM uses an OpenAI compliant endpoint format
    private val openAIService = RetrofitClient.createOpenAIService("https://integrate.api.nvidia.com/")

    override suspend fun connect(): Boolean {
        // Implementation check
        return BuildConfig.NVIDIA_API_KEY.isNotEmpty()
    }

    override suspend fun disconnect() {
        // No persistent connection setup required
    }

    override suspend fun listModels(): List<String> {
        return listOf("meta/llama3-70b-instruct", "meta/llama3-8b-instruct", "mistralai/mistral-large-latest")
    }

    override suspend fun health(): Boolean {
        return connect()
    }

    override suspend fun chat(messages: List<Pair<String, Boolean>>, model: String, systemPrompt: String?): Result<String> {
        return try {
            val apiMessages = mutableListOf<OpenAIMessage>()
            if (!systemPrompt.isNullOrBlank()) {
                apiMessages.add(OpenAIMessage(role = "system", content = systemPrompt))
            }
            apiMessages.addAll(messages.map { (text, isUser) ->
                OpenAIMessage(
                    content = text,
                    role = if (isUser) "user" else "assistant"
                )
            })
            val request = OpenAIChatRequest(model = model, messages = apiMessages)
            val apiKey = BuildConfig.NVIDIA_API_KEY
            val authHeader = "Bearer $apiKey"
            
            val response = openAIService.chatCompletions(authHeader, request)
            val text = response.choices?.firstOrNull()?.message?.content 
                ?: return Result.failure(Exception("No response from AI."))
                
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
