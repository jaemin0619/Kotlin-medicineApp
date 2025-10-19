package com.example.yakbangapp.ui.mypage

import android.content.Context
import androidx.lifecycle.*
import com.example.yakbangapp.auth.UserProfile
import com.example.yakbangapp.auth.UserSession
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch

class MyPageViewModel : ViewModel() {

    private val _profile = MutableLiveData<UserProfile>()
    val profile: LiveData<UserProfile> = _profile

    /** Activity에서 호출해 DataStore를 observe하도록 초기화 */
    fun bindSession(context: Context) {
        val session = UserSession(context)
        // 프로필
        viewModelScope.launch {
            session.profileFlow.collect { p -> _profile.postValue(p) }
        }
    }

    /**
     * 버튼 클릭 시 동작:
     * - 로그인 상태면 → 로그아웃 시도(Kakao + 세션 clear)
     * - 로그아웃 상태면 → 로그인 화면으로 이동 요청 콜백(goLogin = true)
     */
    fun onLoginOrLogout(context: Context, goLoginCallback: (Boolean) -> Unit) {
        val session = UserSession(context)
        // 현재 로그인 여부(한 번 조회)
        viewModelScope.launch {
            val token = session.kakaoAccessTokenOnce()
            val loggedIn = !token.isNullOrBlank()
            if (loggedIn) {
                // Kakao 로그아웃 + 세션 clear
                UserApiClient.instance.logout { _ ->
                    viewModelScope.launch {
                        session.clear()
                    }
                }
            } else {
                goLoginCallback(true)
            }
        }
    }
}
