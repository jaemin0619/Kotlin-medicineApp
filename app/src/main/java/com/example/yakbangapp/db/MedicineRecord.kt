package com.example.yakbangapp.db
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicine_records")
data class MedicineRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // 자동으로 ID 생성
    val userId: Long, // 어떤 사용자의 기록인지 연결하기 위함
    val medicineName: String, // 약 이름
    val dose: String, // 복용량 (예: 1정)
    val time: String, // 복용 시간 (예: 아침 식후)
    val addedDate: Long = System.currentTimeMillis() // 기록 추가된 시간
)
