package com.example.yakbangapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.yakbangapp.auth.UserProfile
import com.example.yakbangapp.auth.UserSession
import com.example.yakbangapp.databinding.ActivityLoginBinding
import com.example.yakbangapp.SearchOptionActivity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: UserSession
    private var alreadyNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = UserSession(this)

        binding.btnKakaoLogin.setOnClickListener { loginWithKakao() }
    }

    private fun loginWithKakao() {
        val userApi = UserApiClient.instance
        if (userApi.isKakaoTalkLoginAvailable(this)) {
            userApi.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    loginWithKakaoAccount()
                } else if (token != null) {
                    fetchMeAndProceed(token)
                }
            }
        } else {
            loginWithKakaoAccount()
        }
    }

    private fun loginWithKakaoAccount() {
        UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
            if (error != null) {
                Toast.makeText(this, "로그인 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            } else if (token != null) {
                fetchMeAndProceed(token)
            }
        }
    }

    private fun fetchMeAndProceed(oauth: OAuthToken) {
        UserApiClient.instance.me { user, error ->
            if (error != null || user == null) {
                Toast.makeText(this, "사용자 정보 조회 실패", Toast.LENGTH_SHORT).show()
                return@me
            }

            val account = user.kakaoAccount
            val profile = account?.profile
            val p = UserProfile(
                id = user.id?.toString().orEmpty(),
                name = profile?.nickname.orEmpty(),
                email = account?.email.orEmpty(),
                avatarUrl = profile?.thumbnailImageUrl.orEmpty(),
                provider = "kakao"
            )

            lifecycleScope.launch {
                // 1) 세션 저장(프로필 + 토큰)
                session.save(p)
                session.saveKakaoTokens(
                    accessToken = oauth.accessToken,
                    refreshToken = oauth.refreshToken.orEmpty(),
                    expiresAtEpochMillis = oauth.accessTokenExpiresAt?.time ?: 0L  // ← 이름 맞춤
                )

                // 2) 중복 네비 차단
                if (alreadyNavigated) return@launch
                alreadyNavigated = true

                // 3) SearchOptionActivity로 이동(스택 클리어)
                startActivity(Intent(this@LoginActivity, SearchOptionActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
        }
    }

}
