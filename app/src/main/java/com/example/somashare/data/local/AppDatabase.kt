package com.example.somashare.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.somashare.data.local.dao.*
import com.example.somashare.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        LecturerEntity::class,
        UnitEntity::class,
        UnitLecturerEntity::class,
        PastPaperEntity::class,
        UserEnrollmentEntity::class,
        PaperViewEntity::class,
        PaperDownloadEntity::class,
        UserFavoriteEntity::class,
        PaperRatingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun lecturerDao(): LecturerDao
    abstract fun unitDao(): UnitDao
    abstract fun pastPaperDao(): PastPaperDao
    abstract fun paperViewDao(): PaperViewDao
    abstract fun paperDownloadDao(): PaperDownloadDao
    abstract fun userEnrollmentDao(): UserEnrollmentDao
    abstract fun userFavoriteDao(): UserFavoriteDao
    abstract fun paperRatingDao(): PaperRatingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "past_papers_database"
                )
                    .fallbackToDestructiveMigration() // Only for development
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
