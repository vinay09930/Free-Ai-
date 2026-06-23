package com.example.domain.provider

import com.example.domain.service.GeminiService
import com.example.domain.service.OpenRouterService
import com.example.domain.service.NvidiaService
import com.example.domain.service.HuggingFaceService
import com.example.domain.service.LocalModelService

class ProviderManager {
    val localModelService = LocalModelService()
    
    val providers: List<ProviderAdapter> = listOf(
        GeminiService(),
        OpenRouterService(),
        NvidiaService(),
        HuggingFaceService(),
        localModelService
    )

    fun getProviderForModel(model: String): ProviderAdapter? {
        val provider = providers.find { p ->
            when (p.id) {
                "provider_gemini" -> model.startsWith("gemini")
                "provider_openrouter" -> model.contains("/") && !model.startsWith("meta/") && p.id == "provider_openrouter" // A bit fragile but OK
                "provider_nvidia" -> model.startsWith("meta/") || model.startsWith("mistralai/")
                "provider_hf" -> model.startsWith("hf:") || model.contains("tiiuae") || model.contains("Dialo") || model.contains("Mistral")
                "provider_local" -> localModelService.localModels.contains(model) || model == "No Local Models Found"
                else -> false
            }
        }
        return provider ?: providers.firstOrNull { it.id == "provider_gemini" }
    }

    suspend fun listAllModels(): List<Pair<String, String>> {
        val allModels = mutableListOf<Pair<String, String>>()
        providers.forEach { provider ->
            if (provider.connect()) {
                val models = provider.listModels()
                allModels.addAll(models.map { Pair(it, provider.name) })
            }
        }
        return allModels
    }
}
