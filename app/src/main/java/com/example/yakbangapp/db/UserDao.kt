package com.example.yakbangapp.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserDao {
    // 사용자가 있으면 업데이트, 없으면 삽입 (카카오 로그인 시 사용)
    @Upsert
    suspend fun upsertUser(user: User)

    // ID로 사용자 정보 가져오기 (LiveData 사용으로 자동 UI 갱신)
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: Long): LiveData<User?>

    // 모든 사용자 삭제 (로그아웃 시 사용)
    @Query("DELETE FROM users")
    suspend fun clearAllUsers()
}
