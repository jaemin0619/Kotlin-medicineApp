// app/src/main/java/com/example/yakbangapp/ui/aichat/net/ChatApi.kt
package com.example.yakbangapp.ui.aichat.net

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class ChatRequest(val message: String)
data class ChatResponse(val response: String)

interface ChatApi {
    @POST("api/chat")
    suspend fun chat(@Body body: ChatRequest): Response<ChatResponse>
}
