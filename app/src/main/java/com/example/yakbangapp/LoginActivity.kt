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
        setContentView(R.layout.activity_login)   // ✅ 먼저 화면 세팅

        // 기존 세션 검증: 있으면 바로 다음 화면
        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            if (error == null && tokenInfo != null) {
                goNext()
            } else {
                logKeyHash()
                initLoginButton()
            }
        }
    }

    private fun initLoginButton() {
        // ✅ activity_login.xml 내부에 @+id/kakao_login_button 버튼이 있어야 합니다.
        findViewById<Button>(R.id.kakao_login_button).setOnClickListener {
            loginWithKakaoTalkOrFallback()
        }
    }

    /** 카카오톡 설치 시 톡 우선 → 실패 시 카카오계정(브라우저)로 폴백 */
    private fun loginWithKakaoTalkOrFallback() {
        val api = UserApiClient.instance

        val doAccount: () -> Unit = {
            api.loginWithKakaoAccount(this) { token, error ->
                when {
                    error != null -> {
                        Log.e(TAG, "[Account] ${error::class.java.name}: ${error.localizedMessage}", error)
                        Toast.makeText(this, "카카오계정 로그인 실패: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                    token != null -> onLoginSuccess(token)
                }
            }
        }

        if (api.isKakaoTalkLoginAvailable(this)) {
            api.loginWithKakaoTalk(this) { token, error ->
                when {
                    token != null -> onLoginSuccess(token)
                    error != null -> {
                        Log.w(TAG, "[Talk] ${error::class.java.name}: ${error.localizedMessage}", error)
                        // 사용자가 취소했는지, 다른 오류인지와 무관하게 계정 로그인 폴백
                        doAccount()
                    }
                }
            }
        } else {
            doAccount()
        }
    }

    private fun onLoginSuccess(token: OAuthToken) {
        toast("로그인 성공!")
        // 사용자 정보 확인(선택)
        UserApiClient.instance.me { user, err ->
            if (err != null) {
                Log.w(TAG, "me() error: ${err.localizedMessage}", err)
            } else {
                Log.d(TAG, "user id=${user?.id}, email=${user?.kakaoAccount?.email}")
            }
        }
        goNext()
    }

    private fun goNext() {
        startActivity(Intent(this, SearchOptionActivity::class.java))
        finish()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    /** 키해시 로그 (Kakao Developers 콘솔 등록용) */
    private fun logKeyHash() {
        val hash1 = Utility.getKeyHash(this)
        Log.d("KeyHash", "Utility.getKeyHash: $hash1")
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            info.signingInfo.apkContentsSigners.forEach { sig ->
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
