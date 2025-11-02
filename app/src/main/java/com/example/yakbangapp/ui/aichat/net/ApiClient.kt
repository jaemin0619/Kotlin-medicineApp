// app/src/main/java/com/example/yakbangapp/ui/aichat/net/ApiClient.kt
package com.example.yakbangapp.ui.aichat.net

// ApiClient.kt
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://marcia-unfissile-glenna.ngrok-free.dev/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: ChatApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)              // 반드시 / 로 끝나야 함
            .client(client)
            .addConverterFactory(GsonConverterFactory.create()) // ✅ Gson만
            .build()
            .create(ChatApi::class.java)
    }
}
