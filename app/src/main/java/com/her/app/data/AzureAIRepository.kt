package com.her.app.data

import com.her.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

// Azure OpenAI — compatible with DeepSeek and any OpenAI-format model
// Configure in local.properties:
//   AZURE_OPENAI_ENDPOINT=https://<your-resource>.openai.azure.com
//   AZURE_OPENAI_KEY=<your-api-key>
//   AZURE_OPENAI_DEPLOYMENT=<deployment-name>  e.g. deepseek-r1 or gpt-4o
class AzureAIRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    // Azure OpenAI uses: POST {endpoint}/openai/deployments/{deployment}/chat/completions?api-version=2024-02-01
    private val endpoint: String get() = BuildConfig.AZURE_OPENAI_ENDPOINT.trimEnd('/')
    private val deployment: String get() = BuildConfig.AZURE_OPENAI_DEPLOYMENT
    private val apiKey: String get() = BuildConfig.AZURE_OPENAI_KEY

    suspend fun sendMessage(
        personality: Personality,
        conversationHistory: List<Message>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val messagesArray = JSONArray()

            // System message first
            messagesArray.put(JSONObject().apply {
                put("role", "system")
                put("content", personality.systemPrompt)
            })

            // Full conversation history
            conversationHistory.forEach { msg ->
                messagesArray.put(JSONObject().apply {
                    put("role", if (msg.isFromUser) "user" else "assistant")
                    put("content", msg.content)
                })
            }

            val requestBody = JSONObject().apply {
                put("messages", messagesArray)
                put("max_tokens", 400)
                put("temperature", 0.85)   // slightly creative for personality expression
                put("top_p", 0.95)
            }.toString()

            val url = "$endpoint/openai/deployments/$deployment/chat/completions?api-version=2024-02-01"

            val request = Request.Builder()
                .url(url)
                .addHeader("api-key", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Azure OpenAI error ${response.code}: $responseBody")
                )
            }

            val json = JSONObject(responseBody)
            val content = json
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()

            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
