package com.example.yakbangapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users") // 테이블 이름 지정
data class User(
    @PrimaryKey val id: Long, // Kakao에서 받은 고유 ID
    val nickname: String,
    val profileImageUrl: String?,
    val email: String?
)