package com.example.domain.service

import com.example.domain.provider.ProviderAdapter
import kotlinx.coroutines.delay

class LocalModelService : ProviderAdapter {
    override val id: String = "provider_local"
    override val name: String = "Internal Storage (Local)"

    val localModels = mutableListOf<String>()

    fun addModel(name: String) {
        if (!localModels.contains(name)) localModels.add(name)
    }

    override suspend fun connect(): Boolean = true
    override suspend fun disconnect() {}

    override suspend fun listModels(): List<String> {
        return localModels.ifEmpty { listOf("No Local Models Found") }
    }

    override suspend fun health(): Boolean = true

    override suspend fun chat(messages: List<Pair<String, Boolean>>, model: String, systemPrompt: String?): Result<String> {
        if (localModels.isEmpty() || model == "No Local Models Found") {
            return Result.failure(Exception("Please import or download a model first from the Model Manager."))
        }
        delay(1500) // Simulate local processing latency
        return Result.success("[Simulation Mode] This is a simulated response generated locally by the mocked backend for '$model'. In a full production app, this would route through MediaPipe or Llama.cpp.")
    }

    override fun chatStream(messages: List<Pair<String, Boolean>>, model: String, systemPrompt: String?): kotlinx.coroutines.flow.Flow<Result<String>> = kotlinx.coroutines.flow.flow {
        if (localModels.isEmpty() || model == "No Local Models Found") {
            emit(Result.failure(Exception("Please import or download a model first.")))
            return@flow
        }
        delay(500)
        val text = "[Simulation Mode] This is a simulated response generated locally by the mocked backend for '$model'. In a full production app, this would route through MediaPipe or Llama.cpp."
        val chunks = text.split(Regex("(?<=\\s)|(?=[\\s.,!?])"))
        for (chunk in chunks) {
            emit(Result.success(chunk))
            delay((20..80).random().toLong()) // emulate typing speed
        }
    }
}
