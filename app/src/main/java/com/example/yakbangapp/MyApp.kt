package com.example.yakbangapp

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // ✅ 네이티브 앱 키로 명시 초기화 (가장 안전)
        KakaoSdk.init(this, "b5179c6395c6c6dc6dbc677be8a8a4db")
    }
}
