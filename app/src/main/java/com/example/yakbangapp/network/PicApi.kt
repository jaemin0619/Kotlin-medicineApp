package com.example.yakbangapp.network

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.concurrent.TimeUnit

interface PicApi {
    @Multipart
    @POST("api/getPic")
    suspend fun uploadPic(
        @Part file: MultipartBody.Part
    ): Response<ResponseBody>
    // 서버가 "file" 키를 요구하면 createFormData("file", ...)로 생성하면 됩니다.
}

object ApiClient {

    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // 요청/응답 전체 로그 보기
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(logger)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://marcia-unfissile-glenna.ngrok-free.dev/") // 반드시 / 로 끝나야 함
        .client(okHttp)
        // ✅ MoshiConverterFactory → GsonConverterFactory로 교체
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // PicApi 인터페이스 인스턴스 생성
    val pic: PicApi = retrofit.create(PicApi::class.java)
}
