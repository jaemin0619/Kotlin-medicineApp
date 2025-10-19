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

private val Context.dataStore by preferencesDataStore("user_prefs")

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val provider: String = "" // "kakao" 등
)

class UserSession(private val context: Context) {

    // Profile keys
    private val KEY_ID: Preferences.Key<String> = stringPreferencesKey("id")
    private val KEY_NAME: Preferences.Key<String> = stringPreferencesKey("name")
    private val KEY_EMAIL: Preferences.Key<String> = stringPreferencesKey("email")
    private val KEY_AVATAR: Preferences.Key<String> = stringPreferencesKey("avatar")
    private val KEY_PROVIDER: Preferences.Key<String> = stringPreferencesKey("provider")

    // Kakao token keys
    private val KEY_KAKAO_ACCESS: Preferences.Key<String> = stringPreferencesKey("kakao_access_token")
    private val KEY_KAKAO_REFRESH: Preferences.Key<String> = stringPreferencesKey("kakao_refresh_token")
    private val KEY_KAKAO_EXPIRES_AT: Preferences.Key<Long> = longPreferencesKey("kakao_expires_at")

    /** 실시간 프로필 스트림 */
    val profileFlow: Flow<UserProfile> = context.dataStore.data.map { p ->
        UserProfile(
            id = p[KEY_ID].orEmpty(),
            name = p[KEY_NAME].orEmpty(),
            email = p[KEY_EMAIL].orEmpty(),
            avatarUrl = p[KEY_AVATAR].orEmpty(),
            provider = p[KEY_PROVIDER].orEmpty()
        )
    }

    /** 실시간 로그인 여부(내 세션 기준) */
    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { p ->
        !p[KEY_KAKAO_ACCESS].isNullOrBlank()
    }

    /** 프로필 전체 저장 */
    suspend fun save(profile: UserProfile) {
        context.dataStore.edit {
            it[KEY_ID] = profile.id
            it[KEY_NAME] = profile.name
            it[KEY_EMAIL] = profile.email
            it[KEY_AVATAR] = profile.avatarUrl
            it[KEY_PROVIDER] = profile.provider
        }
    }

    /** 부분 업데이트용 */
    suspend fun setProfile(
        id: String? = null,
        name: String? = null,
        email: String? = null,
        avatarUrl: String? = null,
        provider: String? = null
    ) {
        context.dataStore.edit { e ->
            if (id != null) e[KEY_ID] = id
            if (name != null) e[KEY_NAME] = name
            if (email != null) e[KEY_EMAIL] = email
            if (avatarUrl != null) e[KEY_AVATAR] = avatarUrl
            if (provider != null) e[KEY_PROVIDER] = provider
        }
    }

    /** 닉네임만 변경 */
    suspend fun setName(name: String) {
        context.dataStore.edit { it[KEY_NAME] = name }
    }

    /** 카카오 토큰 저장 */
    suspend fun saveKakaoTokens(accessToken: String, refreshToken: String, expiresAt: Long) {
        context.dataStore.edit {
            it[KEY_KAKAO_ACCESS] = accessToken
            it[KEY_KAKAO_REFRESH] = refreshToken
            it[KEY_KAKAO_EXPIRES_AT] = expiresAt
        }
    }

    /** 1회 조회용 헬퍼 */
    suspend fun kakaoAccessTokenOnce(): String? {
        val p = context.dataStore.data.first()
        return p[KEY_KAKAO_ACCESS]
    }

    /** 전체 초기화 */
    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
