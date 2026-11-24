package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MastreyDao {

    // --- Profile ---
    @Query("SELECT * FROM user_profile WHERE ownerKey = :ownerKey LIMIT 1")
    fun observeProfile(ownerKey: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE ownerKey = :ownerKey LIMIT 1")
    suspend fun getProfile(ownerKey: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)

    // --- Attempts ---
    @Query("SELECT * FROM question_attempts WHERE ownerKey = :ownerKey ORDER BY createdAtMs DESC")
    fun observeAttempts(ownerKey: String): Flow<List<QuestionAttemptEntity>>

    @Query("SELECT * FROM question_attempts WHERE ownerKey = :ownerKey ORDER BY createdAtMs DESC")
    suspend fun getAttemptsOnce(ownerKey: String): List<QuestionAttemptEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuestionAttemptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempts(attempts: List<QuestionAttemptEntity>)

    // --- Tests ---
    @Query("SELECT * FROM test_results WHERE ownerKey = :ownerKey ORDER BY createdAtMs DESC")
    fun observeTestResults(ownerKey: String): Flow<List<TestResultEntity>>

    @Query("SELECT * FROM test_results WHERE ownerKey = :ownerKey ORDER BY createdAtMs DESC")
    suspend fun getTestResultsOnce(ownerKey: String): List<TestResultEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestResult(result: TestResultEntity)

    // --- Bookmarks ---
    @Query("SELECT * FROM bookmarks WHERE ownerKey = :ownerKey ORDER BY createdAtMs DESC")
    fun observeBookmarks(ownerKey: String): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE ownerKey = :ownerKey")
    suspend fun getBookmarksOnce(ownerKey: String): List<BookmarkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: String)

    @Query("DELETE FROM bookmarks WHERE ownerKey = :ownerKey AND targetId = :targetId")
    suspend fun deleteBookmarkByTarget(ownerKey: String, targetId: String)

    // --- Personal Notes ---
    @Query("SELECT * FROM personal_notes WHERE ownerKey = :ownerKey ORDER BY updatedAtMs DESC")
    fun observeNotes(ownerKey: String): Flow<List<PersonalNoteEntity>>

    @Query("SELECT * FROM personal_notes WHERE ownerKey = :ownerKey")
    suspend fun getNotesOnce(ownerKey: String): List<PersonalNoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: PersonalNoteEntity)

    @Query("DELETE FROM personal_notes WHERE id = :id")
    suspend fun deleteNote(id: String)

    // --- Topic Revisions ---
    @Query("SELECT * FROM topic_revisions WHERE ownerKey = :ownerKey ORDER BY nextReviewEpochMs ASC")
    fun observeRevisions(ownerKey: String): Flow<List<TopicRevisionEntity>>

    @Query("SELECT * FROM topic_revisions WHERE ownerKey = :ownerKey")
    suspend fun getRevisionsOnce(ownerKey: String): List<TopicRevisionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRevision(revision: TopicRevisionEntity)

    // --- Study Tasks ---
    @Query("SELECT * FROM study_tasks WHERE ownerKey = :ownerKey ORDER BY scheduledDate ASC, createdAtMs ASC")
    fun observeStudyTasks(ownerKey: String): Flow<List<StudyTaskEntity>>

    @Query("SELECT * FROM study_tasks WHERE ownerKey = :ownerKey")
    suspend fun getStudyTasksOnce(ownerKey: String): List<StudyTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudyTask(task: StudyTaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudyTasks(tasks: List<StudyTaskEntity>)

    @Query("DELETE FROM study_tasks WHERE id = :id")
    suspend fun deleteStudyTask(id: String)

    @Query("DELETE FROM study_tasks WHERE ownerKey = :ownerKey AND examId = :examId AND completed = 0")
    suspend fun clearIncompleteTasksForExam(ownerKey: String, examId: String)

    // --- AI Conversations & Messages ---
    @Query("SELECT * FROM ai_conversations WHERE ownerKey = :ownerKey AND examProfile = :examProfile ORDER BY updatedAtMs DESC")
    fun observeConversations(ownerKey: String, examProfile: String): Flow<List<AiConversationEntity>>

    @Query("SELECT * FROM ai_conversations WHERE ownerKey = :ownerKey ORDER BY updatedAtMs DESC")
    fun observeAllConversations(ownerKey: String): Flow<List<AiConversationEntity>>

    @Query("SELECT * FROM ai_messages WHERE conversationId = :conversationId ORDER BY createdAtMs ASC")
    fun observeMessages(conversationId: String): Flow<List<AiMessageEntity>>

    @Query("SELECT * FROM ai_messages WHERE conversationId = :conversationId ORDER BY createdAtMs ASC")
    suspend fun getMessagesOnce(conversationId: String): List<AiMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversation(conversation: AiConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessage(message: AiMessageEntity)

    @Query("DELETE FROM ai_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)

    @Query("DELETE FROM ai_conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: String)

    @Query("DELETE FROM ai_messages WHERE ownerKey = :ownerKey")
    suspend fun clearAllAiMessages(ownerKey: String)

    @Query("DELETE FROM ai_conversations WHERE ownerKey = :ownerKey")
    suspend fun clearAllAiConversations(ownerKey: String)

    // --- Cached AI Questions ---
    @Query("SELECT * FROM cached_ai_questions WHERE examId = :examId ORDER BY createdAtMs DESC")
    fun observeAiQuestions(examId: String): Flow<List<CachedAiQuestionEntity>>

    @Query("SELECT * FROM cached_ai_questions")
    suspend fun getAllAiQuestionsOnce(): List<CachedAiQuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiQuestions(questions: List<CachedAiQuestionEntity>)

    // --- Reports ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ContentReportEntity)

    // --- Recent Searches ---
    @Query("SELECT * FROM recent_searches ORDER BY searchedAtMs DESC LIMIT 10")
    fun observeRecentSearches(): Flow<List<RecentSearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecentSearch(search: RecentSearchEntity)

    @Query("DELETE FROM recent_searches")
    suspend fun clearRecentSearches()

    // --- Account Cleanup ---
    @Query("DELETE FROM user_profile WHERE ownerKey = :ownerKey")
    suspend fun clearProfile(ownerKey: String)

    @Query("DELETE FROM question_attempts WHERE ownerKey = :ownerKey")
    suspend fun clearAttempts(ownerKey: String)

    @Query("DELETE FROM test_results WHERE ownerKey = :ownerKey")
    suspend fun clearTests(ownerKey: String)

    @Query("DELETE FROM bookmarks WHERE ownerKey = :ownerKey")
    suspend fun clearBookmarks(ownerKey: String)

    @Query("DELETE FROM personal_notes WHERE ownerKey = :ownerKey")
    suspend fun clearNotes(ownerKey: String)

    @Query("DELETE FROM topic_revisions WHERE ownerKey = :ownerKey")
    suspend fun clearRevisions(ownerKey: String)

    @Query("DELETE FROM study_tasks WHERE ownerKey = :ownerKey")
    suspend fun clearStudyTasks(ownerKey: String)
}
