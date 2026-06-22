package com.example.domain.provider

import com.example.domain.service.GeminiService
import com.example.domain.service.OpenRouterService
import com.example.domain.service.NvidiaService

class ProviderManager {
    val providers: List<ProviderAdapter> = listOf(
        GeminiService(),
        OpenRouterService(),
        NvidiaService()
    )

    fun getProviderForModel(model: String): ProviderAdapter? {
        // Simple logic for matching model to provider
        // In a real app this would be more robust
        val provider = providers.find { p ->
            // Try to find if the provider lists this model or we can do explicit rules
            when (p.id) {
                "provider_gemini" -> model.startsWith("gemini")
                "provider_openrouter" -> model.contains("/") && !model.startsWith("meta/")
                "provider_nvidia" -> model.startsWith("meta/") || model.startsWith("mistralai/")
                else -> false
            }
        }
        return provider ?: providers.firstOrNull { it.id == "provider_gemini" } // Default fallback
    }

    suspend fun listAllModels(): List<Pair<String, String>> {
        val allModels = mutableListOf<Pair<String, String>>()
        providers.forEach { provider ->
            if (provider.connect()) {
                val models = provider.listModels()
                allModels.addAll(models.map { Pair(it, provider.id) })
            }
        }
        return allModels
    }
}
