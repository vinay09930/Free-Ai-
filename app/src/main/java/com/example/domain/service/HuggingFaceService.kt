package com.example.domain.service

import com.example.domain.provider.ProviderAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.json.JSONArray

class HuggingFaceService : ProviderAdapter {
    override val id: String = "provider_hf"
    override val name: String = "Hugging Face Inference"
    private val client = OkHttpClient()
    
    // Some popular HF models for text generation
    override suspend fun listModels(): List<String> = listOf(
        "tiiuae/falcon-7b-instruct",
        "mistralai/Mistral-7B-Instruct-v0.2",
        "microsoft/DialoGPT-medium"
    )

    override suspend fun connect(): Boolean = true
    override suspend fun disconnect() {}
    override suspend fun health(): Boolean = true

    override suspend fun chat(messages: List<Pair<String, Boolean>>, model: String, systemPrompt: String?): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Formatting conversation as a prompt
                val promptText = buildString {
                    systemPrompt?.let { append("$it\n") }
                    messages.forEach { (text, isUser) ->
                        append(if (isUser) "User: $text\n" else "Bot: $text\n")
                    }
                    append("Bot:")
                }

                val jsonStr = JSONObject().put("inputs", promptText).toString()
                val body = jsonStr.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                
                val request = Request.Builder()
                    .url("https://api-inference.huggingface.co/models/$model")
                    .post(body)
                    // We don't have HF_TOKEN in BuildConfig by default, relying on free unauth API.
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(Exception("HF API Error: ${response.code}"))
                    }
                    val resBody = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body"))
                    // Response is usually array: [{"generated_text": "..."}]
                    
                    var generatedText = ""
                    try {
                        val jsonArray = JSONArray(resBody)
                        generatedText = jsonArray.getJSONObject(0).optString("generated_text", "")
                        
                        if (generatedText.isEmpty() && jsonArray.getJSONObject(0).has("error")) {
                             return@withContext Result.failure(Exception("HF API Error: ${jsonArray.getJSONObject(0).getString("error")}"))
                        }
                    } catch (e: Exception) {
                        try {
                           // Sometimes it's a JSON Object with an error
                           val jsonObj = JSONObject(resBody)
                           if (jsonObj.has("error")) {
                               return@withContext Result.failure(Exception("HF API Error: ${jsonObj.getString("error")}"))
                           }
                        } catch (e2: Exception) {}
                        return@withContext Result.failure(Exception("Failed to parse HF response"))
                    }
                    
                    // The HF endpoint often includes the prompt in the output, so we need to trim it.
                    val reply = if (generatedText.contains("Bot:")) {
                        val parts = generatedText.split("Bot:")
                        parts.last().trim() // the very last output
                    } else generatedText.trim()
                    
                    Result.success(reply)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
