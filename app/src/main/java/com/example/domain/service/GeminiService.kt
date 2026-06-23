package com.example.domain.service

import com.example.BuildConfig
import com.example.data.Content
import com.example.data.GenerateContentRequest
import com.example.data.Part
import com.example.data.RetrofitClient
import com.example.domain.provider.ProviderAdapter

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

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
            var fullText = ""
            chatStream(messages, model, systemPrompt).collect { result ->
                if (result.isSuccess) fullText += result.getOrNull() ?: ""
                else if (fullText.isEmpty()) throw result.exceptionOrNull() ?: Exception("Unknown error")
            }
            Result.success(fullText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun chatStream(messages: List<Pair<String, Boolean>>, model: String, systemPrompt: String?): kotlinx.coroutines.flow.Flow<Result<String>> = kotlinx.coroutines.flow.flow {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty()) throw Exception("API Key is missing")

            val jsonReq = org.json.JSONObject()
            val contentsArray = org.json.JSONArray()
            messages.forEach { (text, isUser) ->
                val contentObj = org.json.JSONObject()
                contentObj.put("role", if (isUser) "user" else "model")
                val partsArray = org.json.JSONArray()
                val partObj = org.json.JSONObject()
                partObj.put("text", text)
                partsArray.put(partObj)
                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
            }
            jsonReq.put("contents", contentsArray)

            if (!systemPrompt.isNullOrBlank()) {
                val sysContentObj = org.json.JSONObject()
                sysContentObj.put("role", "system")
                val sysPartsArray = org.json.JSONArray()
                val sysPartObj = org.json.JSONObject()
                sysPartObj.put("text", systemPrompt)
                sysPartsArray.put(sysPartObj)
                sysContentObj.put("parts", sysPartsArray)
                jsonReq.put("systemInstruction", sysContentObj)
            }

            val requestBody = jsonReq.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = okhttp3.Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/$model:streamGenerateContent?alt=sse&key=$apiKey")
                .post(requestBody)
                .build()

            val client = okhttp3.OkHttpClient.Builder()
                .readTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS) // No timeout for streams
                .build()
            
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                emit(Result.failure(Exception("HTTP Error: ${response.code}")))
                return@flow
            }
            
            val source = response.body?.source()
            if (source == null) {
                emit(Result.failure(Exception("Empty Response")))
                return@flow
            }
            
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: break
                if (line.startsWith("data:")) {
                    var data = line.substring(5).trim()
                    if (data == "[DONE]") break
                    try {
                        val json = org.json.JSONObject(data)
                        val candidates = json.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val content = candidates.getJSONObject(0).optJSONObject("content")
                            if (content != null) {
                                val parts = content.optJSONArray("parts")
                                if (parts != null && parts.length() > 0) {
                                    val text = parts.getJSONObject(0).optString("text", "")
                                    if (text.isNotEmpty()) {
                                        emit(Result.success(text))
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // ignore malformed chunks
                    }
                }
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
