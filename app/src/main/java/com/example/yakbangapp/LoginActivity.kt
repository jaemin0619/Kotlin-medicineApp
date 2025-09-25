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
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import java.security.MessageDigest

/*class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        getAppKeyHash()
        KakaoSdk.init(this, "b5179c6395c6c6dc6dbc677be8a8a4db")  // 네이티브 앱 키

        val loginButton = findViewById<Button>(R.id.kakao_login_button)

        loginButton.setOnClickListener {
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                    handleLoginResult(token, error)
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
                    handleLoginResult(token, error)
                }
            }
        }
    }

    private fun getAppKeyHash() {
        try {
            val packageInfo = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            val signatures = packageInfo.signingInfo.apkContentsSigners
            for (signature in signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val keyHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                Log.d("KeyHash", keyHash)
            }
        } catch (e: Exception) {
            Log.e("KeyHash", "getAppKeyHash Error: ${e.message}")
        }
    }

    private fun handleLoginResult(token: OAuthToken?, error: Throwable?) {
        if (error != null) {
            Toast.makeText(this, "로그인 실패: ${error.message}", Toast.LENGTH_SHORT).show()
        } else if (token != null) {
            Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}*/
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ 실행 시 바로 SearchOptionActivity로 이동
        val intent = Intent(this, SearchOptionActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        finish()
    }
}