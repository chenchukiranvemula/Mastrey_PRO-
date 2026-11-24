package com.example.data.remote

import android.content.Context
import com.example.R
import com.example.data.local.AiConversationEntity
import com.example.data.local.AiMessageEntity
import com.example.data.local.BookmarkEntity
import com.example.data.local.ContentReportEntity
import com.example.data.local.PersonalNoteEntity
import com.example.data.local.QuestionAttemptEntity
import com.example.data.local.StudyTaskEntity
import com.example.data.local.TestResultEntity
import com.example.data.local.TopicRevisionEntity
import com.example.data.local.UserProfileEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class FirestoreSyncRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")

    private fun sanitizeDocId(raw: String): String {
        val cleaned = raw.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120)
        return cleaned.ifBlank { "doc_${System.currentTimeMillis()}" }
    }

    suspend fun upsertProfile(profile: UserProfileEntity): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "users/$uid"
        return try {
            val docRef = db.collection("users").document(uid)
            val snap = docRef.get().await()
            val safeName = profile.displayName.trim().ifBlank { "Student" }.take(60)
            if (snap.exists()) {
                val updateMap = mapOf(
                    "displayName" to safeName,
                    "avatarIndex" to profile.avatarIndex.coerceIn(0, 7),
                    "selectedExam" to profile.selectedExam,
                    "targetYear" to profile.targetYear.coerceIn(2025, 2035),
                    "prepLevel" to profile.prepLevel,
                    "dailyStudyMinutes" to profile.dailyStudyMinutes.coerceIn(15, 720),
                    "preferredLanguage" to profile.preferredLanguage.take(30).ifBlank { "English" },
                    "xp" to profile.xp.coerceAtLeast(0),
                    "streakDays" to profile.streakDays.coerceAtLeast(0),
                    "lastStudyDate" to profile.lastStudyDate.take(20),
                    "totalStudyMinutes" to profile.totalStudyMinutes.coerceAtLeast(0),
                    "lastChapterId" to profile.lastChapterId.take(120),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updateMap).await()
            } else {
                val createMap = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "avatarIndex" to profile.avatarIndex.coerceIn(0, 7),
                    "selectedExam" to profile.selectedExam,
                    "targetYear" to profile.targetYear.coerceIn(2025, 2035),
                    "prepLevel" to profile.prepLevel,
                    "dailyStudyMinutes" to profile.dailyStudyMinutes.coerceIn(15, 720),
                    "preferredLanguage" to profile.preferredLanguage.take(30).ifBlank { "English" },
                    "xp" to profile.xp.coerceAtLeast(0),
                    "streakDays" to profile.streakDays.coerceAtLeast(0),
                    "lastStudyDate" to profile.lastStudyDate.take(20),
                    "totalStudyMinutes" to profile.totalStudyMinutes.coerceAtLeast(0),
                    "lastChapterId" to profile.lastChapterId.take(120),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createMap).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun fetchProfile(): Result<UserProfileEntity?> {
        val uid = auth.currentUser?.uid ?: return Result.success(null)
        val path = "users/$uid"
        return try {
            val snap = db.collection("users").document(uid).get().await()
            if (!snap.exists()) return Result.success(null)
            val data = snap.data ?: return Result.success(null)
            val entity = UserProfileEntity(
                ownerKey = uid,
                displayName = (data["displayName"] as? String)?.ifBlank { "Student" } ?: "Student",
                avatarIndex = (data["avatarIndex"] as? Number)?.toInt() ?: 0,
                selectedExam = (data["selectedExam"] as? String) ?: "JEE_MAIN",
                targetYear = (data["targetYear"] as? Number)?.toInt() ?: 2027,
                prepLevel = (data["prepLevel"] as? String) ?: "INTERMEDIATE",
                dailyStudyMinutes = (data["dailyStudyMinutes"] as? Number)?.toInt() ?: 120,
                preferredLanguage = (data["preferredLanguage"] as? String) ?: "English",
                xp = (data["xp"] as? Number)?.toInt() ?: 0,
                streakDays = (data["streakDays"] as? Number)?.toInt() ?: 0,
                lastStudyDate = (data["lastStudyDate"] as? String) ?: "",
                totalStudyMinutes = (data["totalStudyMinutes"] as? Number)?.toInt() ?: 0,
                lastChapterId = (data["lastChapterId"] as? String) ?: ""
            )
            Result.success(entity)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            Result.failure(e)
        }
    }

    suspend fun uploadAttempt(attempt: QuestionAttemptEntity): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val docId = sanitizeDocId(attempt.id)
        val path = "users/$uid/attempts/$docId"
        return try {
            val payload = mapOf(
                "userId" to uid,
                "examId" to attempt.examId,
                "subjectId" to attempt.subjectId.take(64).ifBlank { "general" },
                "chapterId" to attempt.chapterId.take(120).ifBlank { "general" },
                "topicId" to attempt.topicId.take(120).ifBlank { "general" },
                "questionId" to attempt.questionId.take(120),
                "questionFingerprint" to attempt.questionFingerprint.take(128).ifBlank { attempt.questionId },
                "selectedAnswer" to attempt.selectedAnswer.take(200),
                "correct" to attempt.correct,
                "skipped" to attempt.skipped,
                "difficulty" to attempt.difficulty,
                "timeSpentSec" to attempt.timeSpentSec.coerceIn(0, 10800),
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid).collection("attempts").document(docId).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun uploadTestResult(test: TestResultEntity): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val docId = sanitizeDocId(test.id)
        val path = "users/$uid/tests/$docId"
        return try {
            val subjectList = test.subjectSummaryJoined.lines().filter { it.isNotBlank() }.take(20)
            val weakList = test.weakTopicsJoined.lines().filter { it.isNotBlank() }.take(20)
            val payload = mapOf(
                "userId" to uid,
                "examId" to test.examId,
                "title" to test.title.take(120).ifBlank { "Practice Test" },
                "mode" to test.mode,
                "totalQuestions" to test.totalQuestions.coerceIn(1, 200),
                "correctCount" to test.correctCount.coerceIn(0, 200),
                "incorrectCount" to test.incorrectCount.coerceIn(0, 200),
                "unattemptedCount" to test.unattemptedCount.coerceIn(0, 200),
                "score" to test.score.coerceIn(-400.0, 1000.0),
                "maxScore" to test.maxScore.coerceIn(1.0, 1000.0),
                "accuracyPercent" to test.accuracyPercent.coerceIn(0.0, 100.0),
                "durationMinutes" to test.durationMinutes.coerceIn(1, 180),
                "timeTakenSec" to test.timeTakenSec.coerceIn(0, 10800),
                "subjectSummary" to subjectList,
                "weakTopics" to weakList,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid).collection("tests").document(docId).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun uploadBookmark(bm: BookmarkEntity): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val docId = sanitizeDocId(bm.id)
        val path = "users/$uid/bookmarks/$docId"
        return try {
            val payload = mapOf(
                "userId" to uid,
                "examId" to bm.examId,
                "subjectId" to bm.subjectId.take(64),
                "chapterId" to bm.chapterId.take(120),
                "itemType" to bm.itemType,
                "targetId" to bm.targetId.take(120),
                "title" to bm.title.take(160).ifBlank { "Saved Item" },
                "snippet" to bm.snippet.take(1000),
                "collectionName" to bm.collectionName.take(60).ifBlank { "Saved" },
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid).collection("bookmarks").document(docId).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteBookmark(bookmarkId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.success(Unit)
        val docId = sanitizeDocId(bookmarkId)
        val path = "users/$uid/bookmarks/$docId"
        return try {
            db.collection("users").document(uid).collection("bookmarks").document(docId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun upsertNote(note: PersonalNoteEntity): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val docId = sanitizeDocId(note.id)
        val path = "users/$uid/notes/$docId"
        return try {
            val docRef = db.collection("users").document(uid).collection("notes").document(docId)
            val snap = docRef.get().await()
            if (snap.exists()) {
                docRef.update(
                    mapOf(
                        "title" to note.title.take(140).ifBlank { "Note" },
                        "content" to note.content.take(5000).ifBlank { "-" },
                        "highlighted" to note.highlighted,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "examId" to note.examId,
                        "subjectId" to note.subjectId.take(64),
                        "chapterId" to note.chapterId.take(120),
                        "title" to note.title.take(140).ifBlank { "Note" },
                        "content" to note.content.take(5000).ifBlank { "-" },
                        "highlighted" to note.highlighted,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteNote(noteId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.success(Unit)
        val docId = sanitizeDocId(noteId)
        val path = "users/$uid/notes/$docId"
        return try {
            db.collection("users").document(uid).collection("notes").document(docId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun upsertRevision(rev: TopicRevisionEntity): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val docId = sanitizeDocId(rev.topicId)
        val path = "users/$uid/revisions/$docId"
        return try {
            val docRef = db.collection("users").document(uid).collection("revisions").document(docId)
            val snap = docRef.get().await()
            if (snap.exists()) {
                docRef.update(
                    mapOf(
                        "confidence" to rev.confidence,
                        "reviewCount" to rev.reviewCount.coerceIn(0, 10000),
                        "nextReviewEpochMs" to rev.nextReviewEpochMs.coerceAtLeast(0L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "examId" to rev.examId,
                        "subjectId" to rev.subjectId.take(64).ifBlank { "general" },
                        "chapterId" to rev.chapterId.take(120).ifBlank { "general" },
                        "topicId" to rev.topicId.take(120),
                        "topicTitle" to rev.topicTitle.take(160).ifBlank { "Topic" },
                        "confidence" to rev.confidence,
                        "reviewCount" to rev.reviewCount.coerceIn(0, 10000),
                        "nextReviewEpochMs" to rev.nextReviewEpochMs.coerceAtLeast(0L),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun upsertStudyTask(task: StudyTaskEntity): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val docId = sanitizeDocId(task.id)
        val path = "users/$uid/study_tasks/$docId"
        return try {
            val docRef = db.collection("users").document(uid).collection("study_tasks").document(docId)
            val snap = docRef.get().await()
            if (snap.exists()) {
                docRef.update(
                    mapOf(
                        "title" to task.title.take(160).ifBlank { "Study Task" },
                        "scheduledDate" to task.scheduledDate.take(20),
                        "durationMinutes" to task.durationMinutes.coerceIn(5, 360),
                        "completed" to task.completed,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "examId" to task.examId,
                        "title" to task.title.take(160).ifBlank { "Study Task" },
                        "subjectName" to task.subjectName.take(80).ifBlank { "General" },
                        "chapterId" to task.chapterId.take(120),
                        "scheduledDate" to task.scheduledDate.take(20),
                        "durationMinutes" to task.durationMinutes.coerceIn(5, 360),
                        "taskType" to task.taskType,
                        "completed" to task.completed,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun upsertAiConversation(conv: AiConversationEntity): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val docId = sanitizeDocId(conv.id)
        val path = "users/$uid/ai_conversations/$docId"
        return try {
            val docRef = db.collection("users").document(uid).collection("ai_conversations").document(docId)
            val snap = docRef.get().await()
            if (snap.exists()) {
                docRef.update(
                    mapOf(
                        "title" to conv.title.take(140).ifBlank { "Conversation" },
                        "responseStyle" to conv.responseStyle.take(40).ifBlank { "Step-by-step" },
                        "messageCount" to conv.messageCount.coerceIn(0, 5000),
                        "lastMessagePreview" to conv.lastMessagePreview.take(300),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "examProfile" to conv.examProfile,
                        "title" to conv.title.take(140).ifBlank { "Conversation" },
                        "responseStyle" to conv.responseStyle.take(40).ifBlank { "Step-by-step" },
                        "messageCount" to conv.messageCount.coerceIn(0, 5000),
                        "lastMessagePreview" to conv.lastMessagePreview.take(300),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun uploadAiMessage(msg: AiMessageEntity): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val convId = sanitizeDocId(msg.conversationId)
        val msgId = sanitizeDocId(msg.id)
        val path = "users/$uid/ai_conversations/$convId/messages/$msgId"
        return try {
            val payload = mapOf(
                "userId" to uid,
                "role" to msg.role,
                "content" to msg.content.take(12000).ifBlank { "-" },
                "bookmarked" to msg.bookmarked,
                "flagged" to msg.flagged,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid)
                .collection("ai_conversations").document(convId)
                .collection("messages").document(msgId)
                .set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun submitReport(report: ContentReportEntity): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.success(Unit)
        val docId = sanitizeDocId(report.id)
        val path = "users/$uid/reports/$docId"
        return try {
            val payload = mapOf(
                "userId" to uid,
                "examId" to report.examId.take(32).ifBlank { "JEE_MAIN" },
                "targetType" to report.targetType,
                "targetId" to report.targetId.take(120),
                "reason" to report.reason.take(500).ifBlank { "Reported issue" },
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid).collection("reports").document(docId).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteUserCloudProfile(): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.success(Unit)
        val path = "users/$uid"
        return try {
            requireUserId()
            db.collection("users").document(uid).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }
}
