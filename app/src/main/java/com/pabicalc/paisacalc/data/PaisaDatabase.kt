package com.pabicalc.paisacalc.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SavedLoanEntity::class, GstHistoryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class PaisaDatabase : RoomDatabase() {

    abstract fun savedLoanDao(): SavedLoanDao

    abstract fun gstHistoryDao(): GstHistoryDao

    companion object {
        private const val DATABASE_NAME = "paisacalc-local.db"

        @Volatile
        private var instance: PaisaDatabase? = null

        fun getInstance(context: Context): PaisaDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PaisaDatabase::class.java,
                    DATABASE_NAME,
                ).build().also { instance = it }
            }
    }
}
