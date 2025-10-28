package com.example.yakbangapp.auth

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * 앱 내 경량 세션 저장소(DataStore).
 * - 소셜(Kakao) 로그인 후 최소 프로필/토큰을 저장
 * - 화면은 profileFlow / authStateFlow 를 observe 하여 UI 갱신
 *
 * 확장 데이터(즐겨찾기, 복약 기록 등)는 Room 사용 권장.
 */
private val Context.dataStore by preferencesDataStore(name = "user_prefs")

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val provider: String = "",      // "kakao" 등
    val lastUpdatedAt: Long = 0L
)

sealed class AuthState {
    data object LoggedOut : AuthState()
    data class LoggedIn(val profile: UserProfile) : AuthState()
}

class UserSession(private val context: Context) {

    // ---------- Keys ----------
    private object Keys {
        // Profile
        val ID: Preferences.Key<String>        = stringPreferencesKey("id")
        val NAME: Preferences.Key<String>      = stringPreferencesKey("name")
        val EMAIL: Preferences.Key<String>     = stringPreferencesKey("email")
        val AVATAR: Preferences.Key<String>    = stringPreferencesKey("avatar")
        val PROVIDER: Preferences.Key<String>  = stringPreferencesKey("provider")
        val UPDATED_AT: Preferences.Key<Long>  = longPreferencesKey("updated_at")

        // Kakao tokens
        val K_ACCESS: Preferences.Key<String>  = stringPreferencesKey("kakao_access_token")
        val K_REFRESH: Preferences.Key<String> = stringPreferencesKey("kakao_refresh_token")
        val K_EXPIRES_AT: Preferences.Key<Long> = longPreferencesKey("kakao_expires_at")
    }

    // ---------- Flows ----------
    /** 실시간 프로필 스트림 */
    val profileFlow: Flow<UserProfile> = context.dataStore.data.map { p ->
        UserProfile(
            id         = p[Keys.ID].orEmpty(),
            name       = p[Keys.NAME].orEmpty(),
            email      = p[Keys.EMAIL].orEmpty(),
            avatarUrl  = p[Keys.AVATAR].orEmpty(),
            provider   = p[Keys.PROVIDER].orEmpty(),
            lastUpdatedAt = p[Keys.UPDATED_AT] ?: 0L
        )
    }

    /** 내 세션 기준 로그인 여부 (액세스 토큰 존재 여부) */
    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { p ->
        !p[Keys.K_ACCESS].isNullOrBlank()
    }

    /** 로그인/로그아웃 까지 반영된 상태 Flow (UI에서 분기하기 쉬움) */
    val authStateFlow: Flow<AuthState> = context.dataStore.data.map { p ->
        val token = p[Keys.K_ACCESS].orEmpty()
        if (token.isBlank()) {
            AuthState.LoggedOut
        } else {
            AuthState.LoggedIn(
                UserProfile(
                    id = p[Keys.ID].orEmpty(),
                    name = p[Keys.NAME].orEmpty(),
                    email = p[Keys.EMAIL].orEmpty(),
                    avatarUrl = p[Keys.AVATAR].orEmpty(),
                    provider = p[Keys.PROVIDER].orEmpty(),
                    lastUpdatedAt = p[Keys.UPDATED_AT] ?: 0L
                )
            )
        }
    }

    // ---------- One-shot getters ----------
    /** 현재 프로필 스냅샷 1회 조회 */
    suspend fun getProfileOnce(): UserProfile = profileFlow.first()

    /** 액세스 토큰 1회 조회 */
    suspend fun kakaoAccessTokenOnce(): String? = context.dataStore.data.first()[Keys.K_ACCESS]

    /** 현재 로그인 여부 1회 조회 */
    suspend fun isLoggedInOnce(): Boolean = !kakaoAccessTokenOnce().isNullOrBlank()

    // ---------- Save / Update ----------
    /** 전체 프로필 저장(원자적 업데이트) */
    suspend fun save(profile: UserProfile) {
        context.dataStore.edit { e ->
            e[Keys.ID] = profile.id
            e[Keys.NAME] = profile.name
            e[Keys.EMAIL] = profile.email
            e[Keys.AVATAR] = profile.avatarUrl
            e[Keys.PROVIDER] = profile.provider
            e[Keys.UPDATED_AT] = System.currentTimeMillis()
        }
    }

    /** 부분 업데이트(넘긴 값만 갱신) */
    suspend fun setProfile(
        id: String? = null,
        name: String? = null,
        email: String? = null,
        avatarUrl: String? = null,
        provider: String? = null
    ) {
        context.dataStore.edit { e ->
            id?.let       { e[Keys.ID] = it }
            name?.let     { e[Keys.NAME] = it }
            email?.let    { e[Keys.EMAIL] = it }
            avatarUrl?.let{ e[Keys.AVATAR] = it }
            provider?.let { e[Keys.PROVIDER] = it }
            e[Keys.UPDATED_AT] = System.currentTimeMillis()
        }
    }

    /** 닉네임만 변경 */
    suspend fun setName(name: String) {
        context.dataStore.edit { e ->
            e[Keys.NAME] = name
            e[Keys.UPDATED_AT] = System.currentTimeMillis()
        }
    }

    /** 카카오 토큰 저장(만료 epoch millis 함께 보관) */
    suspend fun saveKakaoTokens(
        accessToken: String,
        refreshToken: String,
        expiresAtEpochMillis: Long
    ) {
        context.dataStore.edit { e ->
            e[Keys.K_ACCESS] = accessToken
            e[Keys.K_REFRESH] = refreshToken
            e[Keys.K_EXPIRES_AT] = expiresAtEpochMillis
        }
    }

    /** 카카오 토큰만 제거 (강제 로그아웃 시) */
    suspend fun clearKakaoTokens() {
        context.dataStore.edit { e ->
            e.remove(Keys.K_ACCESS)
            e.remove(Keys.K_REFRESH)
            e.remove(Keys.K_EXPIRES_AT)
        }
    }

    /** 전체 초기화(프로필+토큰 전부 삭제) */
    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    /** 안전 로그아웃: 토큰/프로필 모두 정리 */
    suspend fun signOut() {
        context.dataStore.edit { e ->
            e.clear()
        }
    }

    // ---------- Convenience: Kakao 콜백에서 한 번에 저장 ----------
    /**
     * Kakao 로그인 성공 시 한 번에 저장하는 헬퍼.
     * @param kakaoId      Long? → 문자열로 저장
     * @param nickname     nullable → blank 허용
     * @param email        nullable
     * @param avatarUrl    nullable
     * @param accessToken  필수
     * @param refreshToken nullable
     * @param expiresAtEpochMillis 만료 시각(epoch millis)
     */
    suspend fun saveFromKakao(
        kakaoId: Long?,
        nickname: String?,
        email: String?,
        avatarUrl: String?,
        accessToken: String,
        refreshToken: String?,
        expiresAtEpochMillis: Long
    ) {
        context.dataStore.edit { e ->
            e[Keys.ID]        = (kakaoId ?: 0L).toString()
            e[Keys.NAME]      = nickname.orEmpty()
            e[Keys.EMAIL]     = email.orEmpty()
            e[Keys.AVATAR]    = avatarUrl.orEmpty()
            e[Keys.PROVIDER]  = "kakao"
            e[Keys.UPDATED_AT]= System.currentTimeMillis()

            e[Keys.K_ACCESS]  = accessToken
            refreshToken?.let { e[Keys.K_REFRESH] = it }
            e[Keys.K_EXPIRES_AT] = expiresAtEpochMillis
        }
    }
}
