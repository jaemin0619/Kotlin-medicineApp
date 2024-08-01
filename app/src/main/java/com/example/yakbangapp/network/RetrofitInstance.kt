package com.example.yakbangapp.network

object RetrofitInstance {
    private const val BASE_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/"  >>"http://apis.data.go.kr/1471000/DrbEasyDrugInfoService/"
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }).build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val service: WeatherService by lazy { retrofit.create(WeatherService::class.java) } >> YakService로 이름 바꾸기
}