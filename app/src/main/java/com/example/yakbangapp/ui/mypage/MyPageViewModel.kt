package com.example.yakbangapp.ui.mypage

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.yakbangapp.auth.AuthState
import com.example.yakbangapp.auth.UserProfile
import com.example.yakbangapp.auth.UserSession
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyPageViewModel : ViewModel() {

    private var session: UserSession? = null

    private lateinit var _profile: LiveData<UserProfile>
    val profile: LiveData<UserProfile> get() = _profile

    private lateinit var _auth: LiveData<AuthState>
    val auth: LiveData<AuthState> get() = _auth

    fun bindSession(context: Context) {
        if (session != null) return
        val s = UserSession(context.applicationContext)
        session = s
        _profile = s.profileFlow.asLiveData()
        _auth = s.authStateFlow.asLiveData()
    }

    /**
     * 로그인 상태에 따라:
     *  - 비로그인 → 로그인 화면으로 이동 요청 (goLogin = true)
     *  - 로그인 상태 → (선택) Kakao 로그아웃 + 세션 정리 (goLogin = false)
     */
    fun onLoginOrLogout(context: Context, after: (goLogin: Boolean) -> Unit) {
        val s = session ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val loggedIn = s.isLoggedInOnce()
            if (loggedIn) {
                // 1) Kakao SDK 로그아웃 (토큰 무효화) — 선택사항이지만 보통 같이 해주는 게 안전
                try {
                    withContext(Dispatchers.Main) {
                        UserApiClient.instance.logout { /* err ->
                            // 필요 시 err 로깅
                        */ }
                    }
                } catch (_: Throwable) { /* SDK 미초기화 등 무시 */ }

                // 2) 로컬 세션 정리 (프로필 + 토큰 제거)
                s.signOut()

                after(false)
            } else {
                after(true)
            }
        }
    }
}
