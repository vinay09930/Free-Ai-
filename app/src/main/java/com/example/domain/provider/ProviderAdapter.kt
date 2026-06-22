package com.example.domain.provider

interface ProviderAdapter {
    val id: String
    val name: String
    
    suspend fun connect(): Boolean
    suspend fun disconnect()
    // Returns response text
    suspend fun chat(messages: List<Pair<String, Boolean>>, model: String, systemPrompt: String? = null): Result<String>
    suspend fun listModels(): List<String>
    suspend fun health(): Boolean
}
