package com.example.data.repository

import com.example.data.ai.AiHistoryTurn
import com.example.data.ai.AiTutorContext
import com.example.data.ai.FirebaseMastreyAiService
import com.example.data.ai.MastreyAiProvider
import com.example.data.content.SyllabusCatalog
import com.example.data.local.AiConversationEntity
import com.example.data.local.AiMessageEntity
import com.example.data.local.AppSettingsState
import com.example.data.local.BookmarkEntity
import com.example.data.local.CachedAiQuestionEntity
import com.example.data.local.ContentReportEntity
import com.example.data.local.MastreyDao
import com.example.data.local.PersonalNoteEntity
import com.example.data.local.QuestionAttemptEntity
import com.example.data.local.RecentSearchEntity
import com.example.data.local.SettingsPreferences
import com.example.data.local.StudyTaskEntity
import com.example.data.local.TestResultEntity
import com.example.data.local.TopicRevisionEntity
import com.example.data.local.UserProfileEntity
import com.example.data.model.AchievementItem
import com.example.data.model.BookmarkType
import com.example.data.model.ExamSystem
import com.example.data.model.PrepLevel
import com.example.data.model.QuestionDifficulty
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionType
import com.example.data.model.RevisionConfidence
import com.example.data.model.VerificationStatus
import com.example.data.remote.FirestoreSyncRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class RepeatPolicy(val label: String) {
    UNSEEN_ONLY("New Unseen Questions"),
    WRONG_ONLY("Retry Wrong Questions"),
    BOOKMARKED_ONLY("Bookmarked Questions"),
    ALL_REVISION("All / Revision Mode")
}

data class QuestionPoolSelection(
    val questions: List<QuestionItem>,
    val poolWarning: String?
)

class MastreyRepository(
    private val dao: MastreyDao,
    private val settingsPrefs: SettingsPreferences,
    private val firestoreSync: FirestoreSyncRepository,
    val aiProvider: MastreyAiProvider = FirebaseMastreyAiService()
) {
    val settingsFlow: Flow<AppSettingsState> = settingsPrefs.settingsFlow

    private fun isSignedInCloud(ownerKey: String): Boolean =
        ownerKey != GUEST_KEY && Firebase.auth.currentUser?.uid == ownerKey

    fun observeProfile(ownerKey: String): Flow<UserProfileEntity?> = dao.observeProfile(ownerKey)
    fun observeAttempts(ownerKey: String): Flow<List<QuestionAttemptEntity>> = dao.observeAttempts(ownerKey)
    fun observeTestResults(ownerKey: String): Flow<List<TestResultEntity>> = dao.observeTestResults(ownerKey)
    fun observeBookmarks(ownerKey: String): Flow<List<BookmarkEntity>> = dao.observeBookmarks(ownerKey)
    fun observeNotes(ownerKey: String): Flow<List<PersonalNoteEntity>> = dao.observeNotes(ownerKey)
    fun observeRevisions(ownerKey: String): Flow<List<TopicRevisionEntity>> = dao.observeRevisions(ownerKey)
    fun observeStudyTasks(ownerKey: String): Flow<List<StudyTaskEntity>> = dao.observeStudyTasks(ownerKey)
    fun observeConversations(ownerKey: String, examProfile: String): Flow<List<AiConversationEntity>> =
        dao.observeConversations(ownerKey, examProfile)
    fun observeAllConversations(ownerKey: String): Flow<List<AiConversationEntity>> =
        dao.observeAllConversations(ownerKey)
    fun observeMessages(conversationId: String): Flow<List<AiMessageEntity>> =
        dao.observeMessages(conversationId)
    fun observeRecentSearches(): Flow<List<RecentSearchEntity>> = dao.observeRecentSearches()

    fun observeAiQuestions(exam: ExamSystem): Flow<List<QuestionItem>> =
        dao.observeAiQuestions(exam.id).map { list -> list.map { it.toDomain() } }

    suspend fun ensureProfileInitialized(ownerKey: String, defaultName: String): UserProfileEntity {
        val existing = dao.getProfile(ownerKey)
        if (existing != null) return existing
        if (isSignedInCloud(ownerKey)) {
            val remote = firestoreSync.fetchProfile().getOrNull()
            if (remote != null) {
                dao.upsertProfile(remote)
                return remote
            }
        }
        val created = UserProfileEntity(
            ownerKey = ownerKey,
            displayName = defaultName.ifBlank { if (ownerKey == GUEST_KEY) "Guest Learner" else "Student" },
            selectedExam = ExamSystem.JEE_MAIN.id,
            targetYear = 2027,
            prepLevel = PrepLevel.INTERMEDIATE.name,
            dailyStudyMinutes = 120,
            preferredLanguage = "English"
        )
        dao.upsertProfile(created)
        if (isSignedInCloud(ownerKey)) {
            firestoreSync.upsertProfile(created)
        }
        return created
    }

    suspend fun saveProfile(profile: UserProfileEntity) {
        val updated = profile.copy(updatedAtMs = System.currentTimeMillis())
        dao.upsertProfile(updated)
        settingsPrefs.setProfileSetupCompleted(true)
        if (isSignedInCloud(profile.ownerKey)) {
            firestoreSync.upsertProfile(updated)
        }
    }

    /**
     * Migrates local Guest progress (profile, attempts, tests, bookmarks, notes, revisions, study tasks)
     * into the newly authenticated Google account and syncs to Firestore.
     */
    suspend fun migrateGuestToRegistered(newUid: String, googleDisplayName: String?): String {
        val guestProfile = dao.getProfile(GUEST_KEY)
        val existingCloud = firestoreSync.fetchProfile().getOrNull()
        val mergedProfile = when {
            existingCloud != null -> existingCloud.copy(
                xp = maxOf(existingCloud.xp, guestProfile?.xp ?: 0),
                streakDays = maxOf(existingCloud.streakDays, guestProfile?.streakDays ?: 0),
                totalStudyMinutes = maxOf(existingCloud.totalStudyMinutes, guestProfile?.totalStudyMinutes ?: 0)
            )
            guestProfile != null -> guestProfile.copy(
                ownerKey = newUid,
                displayName = googleDisplayName?.takeIf { it.isNotBlank() } ?: guestProfile.displayName
            )
            else -> UserProfileEntity(
                ownerKey = newUid,
                displayName = googleDisplayName?.takeIf { it.isNotBlank() } ?: "Student"
            )
        }
        dao.upsertProfile(mergedProfile)
        firestoreSync.upsertProfile(mergedProfile)

        var migratedCount = 0
        dao.getAttemptsOnce(GUEST_KEY).forEach { att ->
            val moved = att.copy(ownerKey = newUid)
            dao.insertAttempt(moved)
            firestoreSync.uploadAttempt(moved)
            migratedCount++
        }
        dao.getTestResultsOnce(GUEST_KEY).forEach { tr ->
            val moved = tr.copy(ownerKey = newUid)
            dao.insertTestResult(moved)
            firestoreSync.uploadTestResult(moved)
            migratedCount++
        }
        dao.getBookmarksOnce(GUEST_KEY).forEach { bm ->
            val moved = bm.copy(ownerKey = newUid)
            dao.insertBookmark(moved)
            firestoreSync.uploadBookmark(moved)
            migratedCount++
        }
        dao.getNotesOnce(GUEST_KEY).forEach { note ->
            val moved = note.copy(ownerKey = newUid)
            dao.upsertNote(moved)
            firestoreSync.upsertNote(moved)
            migratedCount++
        }
        dao.getRevisionsOnce(GUEST_KEY).forEach { rev ->
            val moved = rev.copy(id = "${newUid}_${rev.topicId}", ownerKey = newUid)
            dao.upsertRevision(moved)
            firestoreSync.upsertRevision(moved)
            migratedCount++
        }
        dao.getStudyTasksOnce(GUEST_KEY).forEach { task ->
            val moved = task.copy(ownerKey = newUid)
            dao.upsertStudyTask(moved)
            firestoreSync.upsertStudyTask(moved)
            migratedCount++
        }

        settingsPrefs.setGuestModeActive(false)
        return if (migratedCount > 0) "Synced $migratedCount guest records to your Google account." else "Signed in & synced with cloud."
    }

    suspend fun recordChapterOpened(ownerKey: String, chapterId: String) {
        val current = dao.getProfile(ownerKey) ?: return
        val today = todayIso()
        val newStreak = computeNextStreak(current.lastStudyDate, current.streakDays, today)
        val updated = current.copy(
            lastChapterId = chapterId,
            lastStudyDate = today,
            streakDays = newStreak,
            xp = current.xp + 5,
            totalStudyMinutes = current.totalStudyMinutes + 2,
            updatedAtMs = System.currentTimeMillis()
        )
        dao.upsertProfile(updated)
        if (isSignedInCloud(ownerKey)) firestoreSync.upsertProfile(updated)
    }

    suspend fun recordQuestionAttempt(
        ownerKey: String,
        question: QuestionItem,
        selectedAnswer: String,
        correct: Boolean,
        skipped: Boolean,
        timeSpentSec: Int
    ) {
        val attempt = QuestionAttemptEntity(
            id = "att_${UUID.randomUUID().toString().replace("-", "").take(14)}",
            ownerKey = ownerKey,
            examId = question.examId,
            subjectId = question.subjectId,
            chapterId = question.chapterId,
            topicId = question.topicId,
            questionId = question.id,
            questionFingerprint = question.fingerprint,
            selectedAnswer = selectedAnswer,
            correct = correct,
            skipped = skipped,
            difficulty = question.difficulty.name,
            timeSpentSec = timeSpentSec.coerceAtLeast(1)
        )
        dao.insertAttempt(attempt)
        if (isSignedInCloud(ownerKey)) firestoreSync.uploadAttempt(attempt)

        val current = dao.getProfile(ownerKey)
        if (current != null) {
            val today = todayIso()
            val updated = current.copy(
                xp = current.xp + if (correct) 15 else 5,
                streakDays = computeNextStreak(current.lastStudyDate, current.streakDays, today),
                lastStudyDate = today,
                totalStudyMinutes = current.totalStudyMinutes + maxOf(1, timeSpentSec / 60),
                updatedAtMs = System.currentTimeMillis()
            )
            dao.upsertProfile(updated)
            if (isSignedInCloud(ownerKey)) firestoreSync.upsertProfile(updated)
        }
    }

    suspend fun saveTestResult(ownerKey: String, result: TestResultEntity) {
        dao.insertTestResult(result)
        if (isSignedInCloud(ownerKey)) firestoreSync.uploadTestResult(result)

        val current = dao.getProfile(ownerKey)
        if (current != null) {
            val today = todayIso()
            val xpGain = (result.correctCount * 15) + 30
            val updated = current.copy(
                xp = current.xp + xpGain,
                streakDays = computeNextStreak(current.lastStudyDate, current.streakDays, today),
                lastStudyDate = today,
                totalStudyMinutes = current.totalStudyMinutes + maxOf(1, result.timeTakenSec / 60),
                updatedAtMs = System.currentTimeMillis()
            )
            dao.upsertProfile(updated)
            if (isSignedInCloud(ownerKey)) firestoreSync.upsertProfile(updated)
        }
    }

    /**
     * Non-Repeating Question Engine with similarity fingerprint dedupe & explicit Revision Mode override.
     */
    fun selectQuestions(
        exam: ExamSystem,
        allExamQuestions: List<QuestionItem>,
        attempts: List<QuestionAttemptEntity>,
        bookmarks: List<BookmarkEntity>,
        subjectId: String?,
        chapterId: String?,
        difficulty: QuestionDifficulty?,
        policy: RepeatPolicy,
        requestedCount: Int
    ): QuestionPoolSelection {
        val dedupedByFingerprint = allExamQuestions.distinctBy { it.fingerprint }
        val filteredScope = dedupedByFingerprint.filter { q ->
            q.examId == exam.id &&
                (subjectId.isNullOrBlank() || q.subjectId == subjectId) &&
                (chapterId.isNullOrBlank() || q.chapterId == chapterId) &&
                (difficulty == null || q.difficulty == difficulty)
        }

        val seenFingerprints = attempts.filter { it.examId == exam.id }.map { it.questionFingerprint }.toSet()
        val wrongIds = attempts.filter { it.examId == exam.id && !it.correct && !it.skipped }.map { it.questionId }.toSet()
        val bookmarkedIds = bookmarks.filter { it.itemType == BookmarkType.QUESTION.name }.map { it.targetId }.toSet()

        val selectedPool = when (policy) {
            RepeatPolicy.UNSEEN_ONLY -> filteredScope.filter { it.fingerprint !in seenFingerprints }
            RepeatPolicy.WRONG_ONLY -> filteredScope.filter { it.id in wrongIds }
            RepeatPolicy.BOOKMARKED_ONLY -> filteredScope.filter { it.id in bookmarkedIds }
            RepeatPolicy.ALL_REVISION -> filteredScope
        }

        val finalList = selectedPool.shuffled().take(requestedCount)
        val warning = when {
            finalList.isEmpty() && policy == RepeatPolicy.UNSEEN_ONLY && filteredScope.isNotEmpty() ->
                "You have already seen all ${filteredScope.size} verified questions in this filter. Generate new AI-validated questions or switch to Revision Mode."
            finalList.isEmpty() ->
                "No questions match this filter yet. Try broadening your selection or generating AI-validated practice questions."
            finalList.size < requestedCount ->
                "Available unseen pool has ${finalList.size} of $requestedCount requested questions. You can generate additional AI-validated questions."
            else -> null
        }

        return QuestionPoolSelection(finalList, warning)
    }

    suspend fun generateAndCacheAiQuestions(
        exam: ExamSystem,
        subjectId: String,
        subjectName: String,
        chapterId: String,
        chapterName: String,
        difficulty: QuestionDifficulty,
        count: Int
    ): Result<List<QuestionItem>> {
        val existingStatic = SyllabusCatalog.questionsFor(exam).map { it.fingerprint }
        val existingCached = dao.getAllAiQuestionsOnce().map { it.fingerprint }
        val existingFingerprints = (existingStatic + existingCached).toSet()

        val result = aiProvider.generateValidatedQuestions(
            exam = exam,
            subjectId = subjectId,
            subjectName = subjectName,
            chapterId = chapterId,
            chapterName = chapterName,
            difficulty = difficulty,
            count = count,
            existingFingerprints = existingFingerprints
        )
        result.onSuccess { generated ->
            dao.insertAiQuestions(generated.map { it.toCacheEntity() })
        }
        return result
    }

    suspend fun toggleBookmark(
        ownerKey: String,
        examId: String,
        subjectId: String,
        chapterId: String,
        type: BookmarkType,
        targetId: String,
        title: String,
        snippet: String,
        collectionName: String = "Saved"
    ): Boolean {
        val existing = dao.getBookmarksOnce(ownerKey).firstOrNull { it.targetId == targetId }
        return if (existing != null) {
            dao.deleteBookmark(existing.id)
            if (isSignedInCloud(ownerKey)) firestoreSync.deleteBookmark(existing.id)
            false
        } else {
            val bm = BookmarkEntity(
                id = "bm_${UUID.randomUUID().toString().replace("-", "").take(14)}",
                ownerKey = ownerKey,
                examId = examId,
                subjectId = subjectId,
                chapterId = chapterId,
                itemType = type.name,
                targetId = targetId,
                title = title,
                snippet = snippet,
                collectionName = collectionName
            )
            dao.insertBookmark(bm)
            if (isSignedInCloud(ownerKey)) firestoreSync.uploadBookmark(bm)
            true
        }
    }

    suspend fun removeBookmark(ownerKey: String, bookmarkId: String) {
        dao.deleteBookmark(bookmarkId)
        if (isSignedInCloud(ownerKey)) firestoreSync.deleteBookmark(bookmarkId)
    }

    suspend fun savePersonalNote(
        ownerKey: String,
        examId: String,
        subjectId: String,
        chapterId: String,
        title: String,
        content: String,
        highlighted: Boolean,
        existingId: String? = null
    ) {
        val now = System.currentTimeMillis()
        val note = PersonalNoteEntity(
            id = existingId ?: "note_${UUID.randomUUID().toString().replace("-", "").take(14)}",
            ownerKey = ownerKey,
            examId = examId,
            subjectId = subjectId,
            chapterId = chapterId,
            title = title.trim().ifBlank { "Study Note" },
            content = content.trim(),
            highlighted = highlighted,
            createdAtMs = now,
            updatedAtMs = now
        )
        dao.upsertNote(note)
        if (isSignedInCloud(ownerKey)) firestoreSync.upsertNote(note)
    }

    suspend fun deletePersonalNote(ownerKey: String, noteId: String) {
        dao.deleteNote(noteId)
        if (isSignedInCloud(ownerKey)) firestoreSync.deleteNote(noteId)
    }

    suspend fun markTopicRevision(
        ownerKey: String,
        examId: String,
        subjectId: String,
        chapterId: String,
        topicId: String,
        topicTitle: String,
        confidence: RevisionConfidence
    ) {
        val existing = dao.getRevisionsOnce(ownerKey).firstOrNull { it.topicId == topicId }
        val nextReviewMs = System.currentTimeMillis() + confidence.nextIntervalHours * 3600_000L
        val entity = TopicRevisionEntity(
            id = "${ownerKey}_$topicId",
            ownerKey = ownerKey,
            examId = examId,
            subjectId = subjectId,
            chapterId = chapterId,
            topicId = topicId,
            topicTitle = topicTitle,
            confidence = confidence.name,
            reviewCount = (existing?.reviewCount ?: 0) + 1,
            nextReviewEpochMs = nextReviewMs,
            updatedAtMs = System.currentTimeMillis()
        )
        dao.upsertRevision(entity)
        if (isSignedInCloud(ownerKey)) firestoreSync.upsertRevision(entity)
    }

    suspend fun generateSmartStudyPlan(
        ownerKey: String,
        exam: ExamSystem,
        dailyMinutes: Int,
        weakTopicTitles: List<String>
    ) {
        dao.clearIncompleteTasksForExam(ownerKey, exam.id)
        val chapters = SyllabusCatalog.chaptersFor(exam)
        val subjectsById = SyllabusCatalog.subjectsFor(exam).associateBy { it.id }
        val tasks = mutableListOf<StudyTaskEntity>()
        val cal = Calendar.getInstance()
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        for (dayOffset in 0..6) {
            val dateStr = fmt.format(cal.time)
            val ch = chapters[dayOffset % chapters.size]
            val subName = subjectsById[ch.subjectId]?.name ?: "Core"
            val learnMins = (dailyMinutes * 0.45).toInt().coerceIn(20, 120)
            val practiceMins = (dailyMinutes * 0.35).toInt().coerceIn(15, 90)
            val revMins = (dailyMinutes - learnMins - practiceMins).coerceIn(15, 60)

            tasks.add(
                StudyTaskEntity(
                    id = "task_${exam.id}_${dayOffset}_learn_${UUID.randomUUID().toString().take(6)}",
                    ownerKey = ownerKey,
                    examId = exam.id,
                    title = "Learn: ${ch.title}",
                    subjectName = subName,
                    chapterId = ch.id,
                    scheduledDate = dateStr,
                    durationMinutes = learnMins,
                    taskType = "LEARN"
                )
            )
            tasks.add(
                StudyTaskEntity(
                    id = "task_${exam.id}_${dayOffset}_prac_${UUID.randomUUID().toString().take(6)}",
                    ownerKey = ownerKey,
                    examId = exam.id,
                    title = if (dayOffset == 6) "Weekly Timed Mock Test (${exam.shortTitle})" else "Practice MCQs: ${ch.title}",
                    subjectName = subName,
                    chapterId = ch.id,
                    scheduledDate = dateStr,
                    durationMinutes = practiceMins,
                    taskType = if (dayOffset == 6) "MOCK_TEST" else "PRACTICE"
                )
            )
            val weakLabel = weakTopicTitles.getOrNull(dayOffset % weakTopicTitles.size.coerceAtLeast(1))
            tasks.add(
                StudyTaskEntity(
                    id = "task_${exam.id}_${dayOffset}_rev_${UUID.randomUUID().toString().take(6)}",
                    ownerKey = ownerKey,
                    examId = exam.id,
                    title = if (weakLabel != null) "Revise Weak Topic: $weakLabel" else "Flashcards & Formula Review ($subName)",
                    subjectName = subName,
                    chapterId = ch.id,
                    scheduledDate = dateStr,
                    durationMinutes = revMins,
                    taskType = "REVISION"
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        dao.upsertStudyTasks(tasks)
        if (isSignedInCloud(ownerKey)) {
            tasks.take(9).forEach { firestoreSync.upsertStudyTask(it) }
        }
    }

    suspend fun toggleTaskCompleted(ownerKey: String, task: StudyTaskEntity) {
        val updated = task.copy(completed = !task.completed, updatedAtMs = System.currentTimeMillis())
        dao.upsertStudyTask(updated)
        if (isSignedInCloud(ownerKey)) firestoreSync.upsertStudyTask(updated)
    }

    suspend fun addCustomStudyTask(
        ownerKey: String,
        examId: String,
        title: String,
        subjectName: String,
        durationMinutes: Int,
        taskType: String
    ) {
        val task = StudyTaskEntity(
            id = "task_${UUID.randomUUID().toString().replace("-", "").take(12)}",
            ownerKey = ownerKey,
            examId = examId,
            title = title.trim().ifBlank { "Study Session" },
            subjectName = subjectName,
            chapterId = "",
            scheduledDate = todayIso(),
            durationMinutes = durationMinutes.coerceIn(10, 240),
            taskType = taskType
        )
        dao.upsertStudyTask(task)
        if (isSignedInCloud(ownerKey)) firestoreSync.upsertStudyTask(task)
    }

    suspend fun deleteStudyTask(taskId: String) = dao.deleteStudyTask(taskId)

    // --- AI Conversations ---
    suspend fun createAiConversation(ownerKey: String, examProfile: String, title: String, style: String): AiConversationEntity {
        val conv = AiConversationEntity(
            id = "conv_${UUID.randomUUID().toString().replace("-", "").take(14)}",
            ownerKey = ownerKey,
            examProfile = examProfile,
            title = title.take(120).ifBlank { "New $examProfile Session" },
            responseStyle = style
        )
        dao.upsertConversation(conv)
        if (isSignedInCloud(ownerKey)) firestoreSync.upsertAiConversation(conv)
        return conv
    }

    suspend fun appendAiMessage(
        ownerKey: String,
        conversation: AiConversationEntity,
        role: String,
        content: String
    ): AiMessageEntity {
        val msg = AiMessageEntity(
            id = "msg_${UUID.randomUUID().toString().replace("-", "").take(14)}",
            conversationId = conversation.id,
            ownerKey = ownerKey,
            role = role,
            content = content
        )
        dao.upsertMessage(msg)
        val updatedConv = conversation.copy(
            title = if (conversation.messageCount == 0 && role == "USER") content.take(55) else conversation.title,
            messageCount = conversation.messageCount + 1,
            lastMessagePreview = content.take(140),
            updatedAtMs = System.currentTimeMillis()
        )
        dao.upsertConversation(updatedConv)
        if (isSignedInCloud(ownerKey)) {
            firestoreSync.upsertAiConversation(updatedConv)
            firestoreSync.uploadAiMessage(msg)
        }
        return msg
    }

    suspend fun clearConversation(conversationId: String) {
        dao.deleteMessagesForConversation(conversationId)
        dao.deleteConversation(conversationId)
    }

    suspend fun clearAllAiHistory(ownerKey: String) {
        dao.clearAllAiMessages(ownerKey)
        dao.clearAllAiConversations(ownerKey)
    }

    suspend fun submitReport(
        ownerKey: String,
        examId: String,
        targetType: String,
        targetId: String,
        reason: String
    ) {
        val report = ContentReportEntity(
            id = "rep_${UUID.randomUUID().toString().replace("-", "").take(14)}",
            ownerKey = ownerKey,
            examId = examId,
            targetType = targetType,
            targetId = targetId,
            reason = reason.trim().take(500).ifBlank { "General feedback" }
        )
        dao.insertReport(report)
        if (isSignedInCloud(ownerKey)) firestoreSync.submitReport(report)
    }

    suspend fun saveRecentSearch(query: String) {
        if (query.trim().length >= 2) {
            dao.upsertRecentSearch(RecentSearchEntity(query = query.trim()))
        }
    }

    suspend fun clearRecentSearches() = dao.clearRecentSearches()

    suspend fun clearLocalUserData(ownerKey: String) {
        dao.clearAttempts(ownerKey)
        dao.clearTests(ownerKey)
        dao.clearBookmarks(ownerKey)
        dao.clearNotes(ownerKey)
        dao.clearRevisions(ownerKey)
        dao.clearStudyTasks(ownerKey)
        dao.clearAllAiMessages(ownerKey)
        dao.clearAllAiConversations(ownerKey)
        dao.clearProfile(ownerKey)
    }

    suspend fun deleteAccountAndData(ownerKey: String): Result<Unit> {
        clearLocalUserData(ownerKey)
        return if (isSignedInCloud(ownerKey)) {
            firestoreSync.deleteUserCloudProfile()
        } else {
            Result.success(Unit)
        }
    }

    fun buildAchievements(
        profile: UserProfileEntity?,
        attempts: List<QuestionAttemptEntity>,
        tests: List<TestResultEntity>,
        revisions: List<TopicRevisionEntity>
    ): List<AchievementItem> {
        val streak = profile?.streakDays ?: 0
        val solved = attempts.size
        val accurate = attempts.count { it.correct }
        val testCount = tests.size
        val revCount = revisions.size

        return listOf(
            AchievementItem(
                id = "ach_first_practice",
                title = "First Spark",
                description = "Attempt your first practice question",
                unlocked = solved >= 1,
                progressText = "${minOf(solved, 1)}/1"
            ),
            AchievementItem(
                id = "ach_ten_correct",
                title = "Precision Solver",
                description = "Answer 10 questions correctly",
                unlocked = accurate >= 10,
                progressText = "${minOf(accurate, 10)}/10"
            ),
            AchievementItem(
                id = "ach_first_mock",
                title = "Exam Ready",
                description = "Complete your first timed mock test",
                unlocked = testCount >= 1,
                progressText = "${minOf(testCount, 1)}/1"
            ),
            AchievementItem(
                id = "ach_streak_3",
                title = "Consistent Scholar",
                description = "Maintain a 3-day study streak",
                unlocked = streak >= 3,
                progressText = "${minOf(streak, 3)}/3 days"
            ),
            AchievementItem(
                id = "ach_reviser",
                title = "Memory Architect",
                description = "Schedule 3 topics in Spaced Repetition",
                unlocked = revCount >= 3,
                progressText = "${minOf(revCount, 3)}/3"
            )
        )
    }

    fun streamAiTutor(
        context: AiTutorContext,
        history: List<AiHistoryTurn>,
        prompt: String
    ): Flow<String> = aiProvider.streamTutorResponse(context, history, prompt)

    fun todayIso(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun computeNextStreak(lastDate: String, currentStreak: Int, today: String): Int {
        if (lastDate == today) return currentStreak.coerceAtLeast(1)
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
        return if (lastDate == yesterday) currentStreak + 1 else 1
    }

    private fun CachedAiQuestionEntity.toDomain(): QuestionItem = QuestionItem(
        id = id,
        fingerprint = fingerprint,
        examId = examId,
        subjectId = subjectId,
        subjectName = subjectName,
        chapterId = chapterId,
        chapterName = chapterName,
        topicId = topicId,
        topicName = topicName,
        difficulty = QuestionDifficulty.fromName(difficulty),
        questionType = QuestionType.entries.firstOrNull { it.name == questionType } ?: QuestionType.SINGLE_CHOICE,
        questionText = questionText,
        options = optionsJoined.split("|||").filter { it.isNotBlank() },
        correctAnswer = correctAnswer,
        solutionSteps = solutionStepsJoined.split("|||").filter { it.isNotBlank() },
        explanation = explanation,
        relatedFormula = relatedFormula,
        sourceLabel = "AI-Generated Original (Validated)",
        verificationStatus = VerificationStatus.AI_GENERATED_VALIDATED
    )

    private fun QuestionItem.toCacheEntity(): CachedAiQuestionEntity = CachedAiQuestionEntity(
        id = id,
        fingerprint = fingerprint,
        examId = examId,
        subjectId = subjectId,
        subjectName = subjectName,
        chapterId = chapterId,
        chapterName = chapterName,
        topicId = topicId,
        topicName = topicName,
        difficulty = difficulty.name,
        questionType = questionType.name,
        questionText = questionText,
        optionsJoined = options.joinToString("|||"),
        correctAnswer = correctAnswer,
        solutionStepsJoined = solutionSteps.joinToString("|||"),
        explanation = explanation,
        relatedFormula = relatedFormula
    )

    companion object {
        const val GUEST_KEY = "guest"
        const val GUEST_AI_LIMIT = 12
    }
}
