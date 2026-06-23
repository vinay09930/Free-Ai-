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

    // Option to return flow
    fun chatStream(messages: List<Pair<String, Boolean>>, model: String, systemPrompt: String? = null): kotlinx.coroutines.flow.Flow<Result<String>> = kotlinx.coroutines.flow.flow {
        val result = chat(messages, model, systemPrompt)
        if (result.isSuccess) {
            val text = result.getOrNull() ?: ""
            val chunks = text.split(Regex("(?<=\\s)|(?=[\\s.,!?])"))
            for (chunk in chunks) {
                if (chunk.isEmpty()) continue
                emit(Result.success(chunk))
                kotlinx.coroutines.delay((10..30).random().toLong()) // emulate typing speed for single-shot apis
            }
        } else {
            emit(result)
        }
    }
}
