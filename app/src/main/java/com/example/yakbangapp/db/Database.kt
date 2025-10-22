// 파일 경로: app/src/main/java/com/example/yakbangapp/db/AppDatabase.kt
package com.example.yakbangapp.db

import android.content.Context
import android.content.Contextimport
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class, MedicineRecord::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun medicineRecordDao(): MedicineRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yakbang_database" // DB 파일 이름
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
