package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val ownerKey: String, // "guest" or Firebase UID
    val displayName: String,
    val avatarIndex: Int = 0,
    val selectedExam: String = "JEE_MAIN",
    val targetYear: Int = 2027,
    val prepLevel: String = "INTERMEDIATE",
    val dailyStudyMinutes: Int = 120,
    val preferredLanguage: String = "English",
    val xp: Int = 0,
    val streakDays: Int = 0,
    val lastStudyDate: String = "",
    val totalStudyMinutes: Int = 0,
    val lastChapterId: String = "",
    val updatedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "question_attempts")
data class QuestionAttemptEntity(
    @PrimaryKey val id: String,
    val ownerKey: String,
    val examId: String,
    val subjectId: String,
    val chapterId: String,
    val topicId: String,
    val questionId: String,
    val questionFingerprint: String,
    val selectedAnswer: String,
    val correct: Boolean,
    val skipped: Boolean,
    val difficulty: String,
    val timeSpentSec: Int,
    val createdAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "test_results")
data class TestResultEntity(
    @PrimaryKey val id: String,
    val ownerKey: String,
    val examId: String,
    val title: String,
    val mode: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val unattemptedCount: Int,
    val score: Double,
    val maxScore: Double,
    val accuracyPercent: Double,
    val durationMinutes: Int,
    val timeTakenSec: Int,
    val subjectSummaryJoined: String, // newline-separated lines
    val weakTopicsJoined: String,     // newline-separated titles
    val createdAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val ownerKey: String,
    val examId: String,
    val subjectId: String,
    val chapterId: String,
    val itemType: String,
    val targetId: String,
    val title: String,
    val snippet: String,
    val collectionName: String = "Saved",
    val createdAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "personal_notes")
data class PersonalNoteEntity(
    @PrimaryKey val id: String,
    val ownerKey: String,
    val examId: String,
    val subjectId: String,
    val chapterId: String,
    val title: String,
    val content: String,
    val highlighted: Boolean = false,
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "topic_revisions")
data class TopicRevisionEntity(
    @PrimaryKey val id: String, // "${ownerKey}_${topicId}"
    val ownerKey: String,
    val examId: String,
    val subjectId: String,
    val chapterId: String,
    val topicId: String,
    val topicTitle: String,
    val confidence: String,
    val reviewCount: Int = 1,
    val nextReviewEpochMs: Long,
    val updatedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_tasks")
data class StudyTaskEntity(
    @PrimaryKey val id: String,
    val ownerKey: String,
    val examId: String,
    val title: String,
    val subjectName: String,
    val chapterId: String,
    val scheduledDate: String,
    val durationMinutes: Int,
    val taskType: String,
    val completed: Boolean = false,
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_conversations")
data class AiConversationEntity(
    @PrimaryKey val id: String,
    val ownerKey: String,
    val examProfile: String, // JEE, NEET, CA
    val title: String,
    val responseStyle: String,
    val messageCount: Int = 0,
    val lastMessagePreview: String = "",
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_messages")
data class AiMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val ownerKey: String,
    val role: String, // USER or MODEL
    val content: String,
    val bookmarked: Boolean = false,
    val flagged: Boolean = false,
    val createdAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_ai_questions")
data class CachedAiQuestionEntity(
    @PrimaryKey val id: String,
    val fingerprint: String,
    val examId: String,
    val subjectId: String,
    val subjectName: String,
    val chapterId: String,
    val chapterName: String,
    val topicId: String,
    val topicName: String,
    val difficulty: String,
    val questionType: String,
    val questionText: String,
    val optionsJoined: String, // "|||" delimiter
    val correctAnswer: String,
    val solutionStepsJoined: String, // "|||" delimiter
    val explanation: String,
    val relatedFormula: String,
    val createdAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "content_reports")
data class ContentReportEntity(
    @PrimaryKey val id: String,
    val ownerKey: String,
    val examId: String,
    val targetType: String,
    val targetId: String,
    val reason: String,
    val createdAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey val query: String,
    val searchedAtMs: Long = System.currentTimeMillis()
)
