package com.example.yakbangapp

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import java.security.MessageDigest

class LoginActivity : AppCompatActivity() {

    private val TAG = "KakaoLogin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) 토큰 존재 시 바로 다음 화면
        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            if (error == null && tokenInfo != null) {
                goNext()
            } else {
                // 2) 로그인 화면 표시
                setContentView(R.layout.activity_login)
                logKeyHash()        // 디버깅용(콘솔 등록 확인)
                initLoginButton()   // 로그인 버튼 핸들러
            }
        }
    }

    private fun initLoginButton() {
        val loginButton = findViewById<Button>(R.id.kakao_login_button)

        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Toast.makeText(this, "로그인 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "login error", error)
            } else if (token != null) {
                Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                // (선택) 사용자 정보 로깅
                UserApiClient.instance.me { user, _ ->
                    Log.d(TAG, "user id=${user?.id}, email=${user?.kakaoAccount?.email}")
                }
                goNext()
            }
        }

        loginButton.setOnClickListener {
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
            } else {
                UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
            }
        }
    }

    private fun goNext() {
        startActivity(Intent(this, SearchOptionActivity::class.java))
        finish()
    }

    private fun logKeyHash() {
        // 방법 1) Kakao SDK 유틸
        val hash1 = Utility.getKeyHash(this)
        Log.d("KeyHash", "Utility.getKeyHash: $hash1")

        // 방법 2) 수동 계산
        try {
            val info = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            val signs = info.signingInfo.apkContentsSigners
            for (sig in signs) {
                val md = MessageDigest.getInstance("SHA")
                md.update(sig.toByteArray())
                val keyHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                Log.d("KeyHash", "manual: $keyHash")
            }
        } catch (e: Exception) {
            Log.e("KeyHash", "calc error: ${e.message}")
        }
    }
}
