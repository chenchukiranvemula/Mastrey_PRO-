package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        QuestionAttemptEntity::class,
        TestResultEntity::class,
        BookmarkEntity::class,
        PersonalNoteEntity::class,
        TopicRevisionEntity::class,
        StudyTaskEntity::class,
        AiConversationEntity::class,
        AiMessageEntity::class,
        CachedAiQuestionEntity::class,
        ContentReportEntity::class,
        RecentSearchEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MastreyDatabase : RoomDatabase() {
    abstract fun mastreyDao(): MastreyDao

    companion object {
        @Volatile
        private var INSTANCE: MastreyDatabase? = null

        fun getInstance(context: Context): MastreyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MastreyDatabase::class.java,
                    "mastrey_pro_db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}
