package com.example.yakbangapp

import android.app.Application
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility // 이 import 문을 추가하거나 확인하세요.

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // 디버그 키 해시 확인용 임시 코드
        // 올바른 Utility 클래스를 사용하도록 수정합니다.
        val keyHash = Utility.getKeyHash(this)
        Log.d("Kakao-KeyHash", "KeyHash: $keyHash")

        // 카카오 SDK 초기화
        KakaoSdk.init(this, getString(R.string.kakao_native_app_key))
    }
}
