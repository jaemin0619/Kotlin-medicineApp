package com.example.yakbangapp.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MedicineRecordDao {
    @Insert
    suspend fun insertRecord(record: MedicineRecord)

    // 특정 사용자의 모든 복약 기록 가져오기
    @Query("SELECT * FROM medicine_records WHERE userId = :userId ORDER BY addedDate DESC")
    fun getRecordsForUser(userId: Long): LiveData<List<MedicineRecord>>

    @Delete
    suspend fun deleteRecord(record: MedicineRecord)
}
