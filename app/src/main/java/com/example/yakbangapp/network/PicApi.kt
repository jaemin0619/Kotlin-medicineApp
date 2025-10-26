package com.example.yakbangapp.network

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.concurrent.TimeUnit

interface PicApi{
    @Multipart
    @POST("api/getPic")
    suspend fun uploadPic(@Part file: MultipartBody.Part): Response<ResponseBody>
    // 서버가 "file"을 요구하면 createFormData("file", ...)로만 바꾸면 됨.
}

object ApiClient {
    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }
    private val ok = OkHttpClient.Builder()
        .addInterceptor(logger)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://marcia-unfissile-glenna.ngrok-free.dev/") // 끝에 /
        .client(ok)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val pic: PicApi = retrofit.create(PicApi::class.java)
}


