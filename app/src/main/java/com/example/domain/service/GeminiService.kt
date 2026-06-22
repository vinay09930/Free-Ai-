package com.example.domain.service

import com.example.BuildConfig
import com.example.data.Content
import com.example.data.GenerateContentRequest
import com.example.data.Part
import com.example.data.RetrofitClient
import com.example.domain.provider.ProviderAdapter

class GeminiService : ProviderAdapter {
    override val id: String = "provider_gemini"
    override val name: String = "Gemini API"

    override suspend fun connect(): Boolean {
        // Implementation check
        return BuildConfig.GEMINI_API_KEY.isNotEmpty()
    }

    override suspend fun disconnect() {
        // No persistent connection setup required
    }

    override suspend fun listModels(): List<String> {
        return listOf("gemini-3.5-flash", "gemini-3.5-pro", "gemini-1.5-flash", "gemini-1.5-pro")
    }

    override suspend fun health(): Boolean {
        return connect()
    }

    override suspend fun chat(messages: List<Pair<String, Boolean>>, model: String, systemPrompt: String?): Result<String> {
        return try {
            val contents = messages.map { (text, isUser) ->
                Content(
                    parts = listOf(Part(text = text)),
                    role = if (isUser) "user" else "model"
                )
            }
            val sysInstruction = if (!systemPrompt.isNullOrBlank()) {
                Content(parts = listOf(Part(text = systemPrompt)), role = "system")
            } else null
            val request = GenerateContentRequest(contents = contents, systemInstruction = sysInstruction)
            val apiKey = BuildConfig.GEMINI_API_KEY
            
            val response = RetrofitClient.service.generateContent(model, apiKey, request)
            val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: return Result.failure(Exception("No response from AI."))
                
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
