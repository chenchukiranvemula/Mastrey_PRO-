package com.example.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiHistoryTurn
import com.example.data.ai.AiTutorContext
import com.example.data.content.SyllabusCatalog
import com.example.data.local.AiConversationEntity
import com.example.data.local.AiMessageEntity
import com.example.data.local.AppSettingsState
import com.example.data.local.BookmarkEntity
import com.example.data.local.MastreyDatabase
import com.example.data.local.PersonalNoteEntity
import com.example.data.local.QuestionAttemptEntity
import com.example.data.local.RecentSearchEntity
import com.example.data.local.SettingsPreferences
import com.example.data.local.StudyTaskEntity
import com.example.data.local.TestResultEntity
import com.example.data.local.TopicRevisionEntity
import com.example.data.local.UserProfileEntity
import com.example.data.model.AchievementItem
import com.example.data.model.AiResponseStyle
import com.example.data.model.BookmarkType
import com.example.data.model.ChapterItem
import com.example.data.model.ExamSystem
import com.example.data.model.PrepLevel
import com.example.data.model.QuestionDifficulty
import com.example.data.model.QuestionItem
import com.example.data.model.RevisionConfidence
import com.example.data.remote.FirestoreSyncRepository
import com.example.data.remote.authStateFlow
import com.example.data.repository.MastreyRepository
import com.example.data.repository.RepeatPolicy
import com.example.ui.theme.AccentColorPreference
import com.example.ui.theme.InterfaceDensityPreference
import com.example.ui.theme.TextSizePreference
import com.example.ui.theme.ThemeModePreference
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class PrimaryTab(val label: String) {
    HOME("Home"),
    LEARN("Learn"),
    PRACTICE("Practice"),
    TESTS("Tests"),
    AI("AI")
}

sealed interface AppDestination {
    data object Splash : AppDestination
    data object Onboarding : AppDestination
    data object Auth : AppDestination
    data object ProfileSetup : AppDestination
    data class MainShell(val tab: PrimaryTab = PrimaryTab.HOME) : AppDestination
    data class ChapterDetail(val chapterId: String) : AppDestination
    data object FormulaCenter : AppDestination
    data object RevisionCenter : AppDestination
    data object StudyPlanner : AppDestination
    data object Analytics : AppDestination
    data object Bookmarks : AppDestination
    data object GlobalSearch : AppDestination
    data object RankPredictor : AppDestination
    data object Settings : AppDestination
    data object DeveloperProfile : AppDestination
    data object ActiveExam : AppDestination
    data object ExamResults : AppDestination
}

data class ActiveExamSession(
    val title: String,
    val exam: ExamSystem,
    val mode: String, // "EXAM" or "PRACTICE"
    val questions: List<QuestionItem>,
    val durationMinutes: Int,
    val remainingSeconds: Int,
    val currentIndex: Int = 0,
    val answers: Map<String, String> = emptyMap(),
    val visitedIds: Set<String> = emptySet(),
    val markedForReviewIds: Set<String> = emptySet(),
    val questionStartTimeMs: Long = System.currentTimeMillis(),
    val perQuestionSeconds: Map<String, Int> = emptyMap(),
    val submitted: Boolean = false
)

data class CompletedExamReview(
    val result: TestResultEntity,
    val exam: ExamSystem,
    val questions: List<QuestionItem>,
    val userAnswers: Map<String, String>,
    val perQuestionSeconds: Map<String, Int>
)

sealed interface AiStreamState {
    data object Idle : AiStreamState
    data object Loading : AiStreamState
    data class Streaming(val partialText: String) : AiStreamState
    data class Error(val message: String, val preservedPrompt: String) : AiStreamState
}

@OptIn(ExperimentalCoroutinesApi::class)
class MastreyViewModel(
    private val appContext: Context,
    private val repository: MastreyRepository,
    private val settingsPrefs: SettingsPreferences
) : ViewModel() {

    private val _currentUser = MutableStateFlow<FirebaseUser?>(Firebase.auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    val settings: StateFlow<AppSettingsState> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettingsState()
    )

    val ownerKey: StateFlow<String> = combine(_currentUser, settings) { user, _ ->
        user?.uid ?: MastreyRepository.GUEST_KEY
    }.stateIn(viewModelScope, SharingStarted.Eagerly, Firebase.auth.currentUser?.uid ?: MastreyRepository.GUEST_KEY)

    val profile: StateFlow<UserProfileEntity?> = ownerKey.flatMapLatest { key ->
        repository.observeProfile(key)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeExam: StateFlow<ExamSystem> = profile.combine(settings) { p, _ ->
        ExamSystem.fromId(p?.selectedExam)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExamSystem.JEE_MAIN)

    val attempts: StateFlow<List<QuestionAttemptEntity>> = ownerKey.flatMapLatest { key ->
        repository.observeAttempts(key)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val testResults: StateFlow<List<TestResultEntity>> = ownerKey.flatMapLatest { key ->
        repository.observeTestResults(key)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<BookmarkEntity>> = ownerKey.flatMapLatest { key ->
        repository.observeBookmarks(key)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<PersonalNoteEntity>> = ownerKey.flatMapLatest { key ->
        repository.observeNotes(key)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val revisions: StateFlow<List<TopicRevisionEntity>> = ownerKey.flatMapLatest { key ->
        repository.observeRevisions(key)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyTasks: StateFlow<List<StudyTaskEntity>> = ownerKey.flatMapLatest { key ->
        repository.observeStudyTasks(key)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSearches: StateFlow<List<RecentSearchEntity>> = repository.observeRecentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cachedAiQuestions: StateFlow<List<QuestionItem>> = activeExam.flatMapLatest { exam ->
        repository.observeAiQuestions(exam)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiConversations: StateFlow<List<AiConversationEntity>> = combine(ownerKey, activeExam) { key, exam ->
        key to exam.group.name
    }.flatMapLatest { (key, groupName) ->
        repository.observeConversations(key, groupName)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAiConversations: StateFlow<List<AiConversationEntity>> = ownerKey.flatMapLatest { key ->
        repository.observeAllConversations(key)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    val activeMessages: StateFlow<List<AiMessageEntity>> = _activeConversationId.flatMapLatest { convId ->
        if (convId == null) flowOf(emptyList()) else repository.observeMessages(convId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<AchievementItem>> = combine(profile, attempts, testResults, revisions) { p, a, t, r ->
        repository.buildAchievements(p, a, t, r)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation Backstack
    private val _backStack = MutableStateFlow<List<AppDestination>>(listOf(AppDestination.Splash))
    val currentDestination: StateFlow<AppDestination> = _backStack.combine(settings) { stack, _ ->
        stack.lastOrNull() ?: AppDestination.MainShell()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AppDestination.Splash)

    // Network offline detection
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    // Status Banner / Toast
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // AI Streaming State & Context
    private val _aiStreamState = MutableStateFlow<AiStreamState>(AiStreamState.Idle)
    val aiStreamState: StateFlow<AiStreamState> = _aiStreamState.asStateFlow()
    private val _aiContextSubject = MutableStateFlow<String?>(null)
    val aiContextSubject: StateFlow<String?> = _aiContextSubject.asStateFlow()
    private val _aiContextChapter = MutableStateFlow<String?>(null)
    val aiContextChapter: StateFlow<String?> = _aiContextChapter.asStateFlow()
    private val _aiDraftInput = MutableStateFlow("")
    val aiDraftInput: StateFlow<String> = _aiDraftInput.asStateFlow()

    // AI Question Generation State
    private val _isGeneratingQuestions = MutableStateFlow(false)
    val isGeneratingQuestions: StateFlow<Boolean> = _isGeneratingQuestions.asStateFlow()
    private val _questionGenError = MutableStateFlow<String?>(null)
    val questionGenError: StateFlow<String?> = _questionGenError.asStateFlow()

    // Active Practice Filter
    private val _practiceSubjectFilter = MutableStateFlow<String?>(null)
    val practiceSubjectFilter: StateFlow<String?> = _practiceSubjectFilter.asStateFlow()
    private val _practiceChapterFilter = MutableStateFlow<String?>(null)
    val practiceChapterFilter: StateFlow<String?> = _practiceChapterFilter.asStateFlow()
    private val _practiceRepeatPolicy = MutableStateFlow(RepeatPolicy.UNSEEN_ONLY)
    val practiceRepeatPolicy: StateFlow<RepeatPolicy> = _practiceRepeatPolicy.asStateFlow()

    // Active Exam & Review State
    private val _activeExamSession = MutableStateFlow<ActiveExamSession?>(null)
    val activeExamSession: StateFlow<ActiveExamSession?> = _activeExamSession.asStateFlow()
    private val _completedExamReview = MutableStateFlow<CompletedExamReview?>(null)
    val completedExamReview: StateFlow<CompletedExamReview?> = _completedExamReview.asStateFlow()

    private var timerJob: Job? = null
    private var streamJob: Job? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    init {
        observeAuthChanges()
        observeNetworkConnectivity()
    }

    private fun observeAuthChanges() {
        viewModelScope.launch {
            Firebase.auth.authStateFlow().collect { user ->
                _currentUser.value = user
                if (user != null) {
                    repository.ensureProfileInitialized(user.uid, user.displayName ?: "Student")
                }
            }
        }
    }

    private fun observeNetworkConnectivity() {
        try {
            val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val caps = cm.getNetworkCapabilities(cm.activeNetwork)
            _isOffline.value = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) != true

            val cb = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isOffline.value = false
                }
                override fun onLost(network: Network) {
                    _isOffline.value = true
                }
            }
            networkCallback = cb
            val req = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
            cm.registerNetworkCallback(req, cb)
        } catch (_: Exception) {
            _isOffline.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        streamJob?.cancel()
        try {
            val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            networkCallback?.let { cm.unregisterNetworkCallback(it) }
        } catch (_: Exception) {}
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    // --- Navigation ---
    fun completeSplash() {
        val currentSettings = settings.value
        val user = Firebase.auth.currentUser
        val next = when {
            user != null && currentSettings.profileSetupCompleted -> AppDestination.MainShell()
            user != null -> AppDestination.ProfileSetup
            currentSettings.guestModeActive && currentSettings.profileSetupCompleted -> AppDestination.MainShell()
            currentSettings.onboardingCompleted -> AppDestination.Auth
            else -> AppDestination.Onboarding
        }
        if (user != null) {
            viewModelScope.launch {
                repository.ensureProfileInitialized(user.uid, user.displayName ?: "Student")
            }
        } else if (currentSettings.guestModeActive) {
            viewModelScope.launch {
                repository.ensureProfileInitialized(MastreyRepository.GUEST_KEY, "Guest Learner")
            }
        }
        _backStack.value = listOf(next)
    }

    fun finishOnboarding() {
        viewModelScope.launch {
            settingsPrefs.setOnboardingCompleted(true)
            _backStack.value = listOf(AppDestination.Auth)
        }
    }

    fun onGoogleSignInSuccess(user: FirebaseUser) {
        viewModelScope.launch {
            _currentUser.value = user
            val msg = repository.migrateGuestToRegistered(user.uid, user.displayName)
            showMessage(msg)
            val currentStackTop = _backStack.value.lastOrNull()
            if (currentStackTop is AppDestination.Auth || currentStackTop is AppDestination.Onboarding) {
                val next = if (settings.value.profileSetupCompleted) AppDestination.MainShell() else AppDestination.ProfileSetup
                _backStack.value = listOf(next)
            }
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            settingsPrefs.setGuestModeActive(true)
            repository.ensureProfileInitialized(MastreyRepository.GUEST_KEY, "Guest Learner")
            val next = if (settings.value.profileSetupCompleted) AppDestination.MainShell() else AppDestination.ProfileSetup
            _backStack.value = listOf(next)
        }
    }

    fun navigateTo(dest: AppDestination) {
        _backStack.update { stack ->
            if (dest is AppDestination.MainShell) listOf(dest)
            else if (stack.lastOrNull() == dest) stack
            else stack + dest
        }
    }

    fun switchPrimaryTab(tab: PrimaryTab) {
        _backStack.value = listOf(AppDestination.MainShell(tab))
    }

    fun navigateBack(): Boolean {
        val current = _backStack.value
        if (current.size > 1) {
            _backStack.value = current.dropLast(1)
            return true
        }
        val top = current.lastOrNull()
        if (top is AppDestination.MainShell && top.tab != PrimaryTab.HOME) {
            _backStack.value = listOf(AppDestination.MainShell(PrimaryTab.HOME))
            return true
        }
        return false
    }

    // --- Profile & Exam Switching ---
    fun saveStudentProfile(
        displayName: String,
        avatarIndex: Int,
        exam: ExamSystem,
        targetYear: Int,
        prepLevel: PrepLevel,
        dailyMinutes: Int,
        language: String,
        navigateHomeAfter: Boolean
    ) {
        viewModelScope.launch {
            val key = ownerKey.value
            val existing = profile.value
            val entity = (existing ?: UserProfileEntity(ownerKey = key, displayName = displayName)).copy(
                ownerKey = key,
                displayName = displayName.trim().ifBlank { "Student" },
                avatarIndex = avatarIndex,
                selectedExam = exam.id,
                targetYear = targetYear,
                prepLevel = prepLevel.name,
                dailyStudyMinutes = dailyMinutes,
                preferredLanguage = language
            )
            repository.saveProfile(entity)
            // Seed initial study plan if none exists for this exam
            val existingTasks = studyTasks.value.filter { it.examId == exam.id }
            if (existingTasks.isEmpty()) {
                repository.generateSmartStudyPlan(key, exam, dailyMinutes, emptyList())
            }
            if (navigateHomeAfter) {
                _backStack.value = listOf(AppDestination.MainShell(PrimaryTab.HOME))
            } else {
                showMessage("Profile saved for ${exam.title}.")
            }
        }
    }

    fun switchExamSystem(exam: ExamSystem) {
        viewModelScope.launch {
            val key = ownerKey.value
            val current = repository.ensureProfileInitialized(key, _currentUser.value?.displayName ?: "Student")
            repository.saveProfile(current.copy(selectedExam = exam.id))
            _practiceSubjectFilter.value = null
            _practiceChapterFilter.value = null
            _activeConversationId.value = null
            _aiContextSubject.value = null
            _aiContextChapter.value = null
            showMessage("Switched workspace to ${exam.title}")
        }
    }

    // --- Learning Engine ---
    fun openChapter(chapterId: String) {
        viewModelScope.launch {
            repository.recordChapterOpened(ownerKey.value, chapterId)
        }
        navigateTo(AppDestination.ChapterDetail(chapterId))
    }

    fun openPracticeFor(subjectId: String?, chapterId: String? = null, policy: RepeatPolicy = RepeatPolicy.UNSEEN_ONLY) {
        _practiceSubjectFilter.value = subjectId
        _practiceChapterFilter.value = chapterId
        _practiceRepeatPolicy.value = policy
        switchPrimaryTab(PrimaryTab.PRACTICE)
    }

    fun openAiWithContext(subjectName: String?, chapterTitle: String?, prefillPrompt: String? = null) {
        _aiContextSubject.value = subjectName
        _aiContextChapter.value = chapterTitle
        if (!prefillPrompt.isNullOrBlank()) {
            _aiDraftInput.value = prefillPrompt
        }
        switchPrimaryTab(PrimaryTab.AI)
    }

    // --- Practice & Non-Repeating Question Pool ---
    fun getAllExamQuestions(): List<QuestionItem> {
        val exam = activeExam.value
        return (SyllabusCatalog.questionsFor(exam) + cachedAiQuestions.value).distinctBy { it.fingerprint }
    }

    fun selectPracticeQuestions(
        subjectId: String?,
        chapterId: String?,
        difficulty: QuestionDifficulty?,
        policy: RepeatPolicy,
        count: Int
    ) = repository.selectQuestions(
        exam = activeExam.value,
        allExamQuestions = getAllExamQuestions(),
        attempts = attempts.value,
        bookmarks = bookmarks.value,
        subjectId = subjectId,
        chapterId = chapterId,
        difficulty = difficulty,
        policy = policy,
        requestedCount = count
    )

    fun recordPracticeAnswer(question: QuestionItem, selectedAnswer: String, timeSpentSec: Int) {
        viewModelScope.launch {
            val isCorrect = selectedAnswer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
            repository.recordQuestionAttempt(
                ownerKey = ownerKey.value,
                question = question,
                selectedAnswer = selectedAnswer,
                correct = isCorrect,
                skipped = false,
                timeSpentSec = timeSpentSec
            )
        }
    }

    fun generateAiPracticeQuestions(
        subjectId: String,
        subjectName: String,
        chapterId: String,
        chapterName: String,
        difficulty: QuestionDifficulty,
        count: Int = 3
    ) {
        viewModelScope.launch {
            _isGeneratingQuestions.value = true
            _questionGenError.value = null
            val res = repository.generateAndCacheAiQuestions(
                exam = activeExam.value,
                subjectId = subjectId,
                subjectName = subjectName,
                chapterId = chapterId,
                chapterName = chapterName,
                difficulty = difficulty,
                count = count
            )
            _isGeneratingQuestions.value = false
            res.onSuccess { list ->
                showMessage("Added ${list.size} validated AI-generated questions to your bank.")
            }.onFailure { err ->
                _questionGenError.value = err.localizedMessage ?: "Unable to generate AI questions."
            }
        }
    }

    // --- Mock Test Engine ---
    fun startMockTest(
        title: String,
        subjectId: String?,
        chapterId: String?,
        difficulty: QuestionDifficulty?,
        questionCount: Int,
        durationMinutes: Int,
        mode: String,
        policy: RepeatPolicy
    ): String? {
        val exam = activeExam.value
        val selection = repository.selectQuestions(
            exam = exam,
            allExamQuestions = getAllExamQuestions(),
            attempts = attempts.value,
            bookmarks = bookmarks.value,
            subjectId = subjectId,
            chapterId = chapterId,
            difficulty = difficulty,
            policy = policy,
            requestedCount = questionCount
        )
        if (selection.questions.isEmpty()) {
            return selection.poolWarning ?: "No questions available for this test configuration."
        }
        val firstId = selection.questions.first().id
        val clampedDuration = durationMinutes.coerceIn(10, 180)
        _activeExamSession.value = ActiveExamSession(
            title = title.ifBlank { "${exam.title} Mock Test" },
            exam = exam,
            mode = mode,
            questions = selection.questions,
            durationMinutes = clampedDuration,
            remainingSeconds = clampedDuration * 60,
            currentIndex = 0,
            visitedIds = setOf(firstId),
            questionStartTimeMs = System.currentTimeMillis()
        )
        startExamCountdown()
        navigateTo(AppDestination.ActiveExam)
        return selection.poolWarning
    }

    private fun startExamCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val session = _activeExamSession.value ?: break
                if (session.submitted) break
                val nextRemaining = session.remainingSeconds - 1
                if (nextRemaining <= 0) {
                    submitActiveExam()
                    break
                } else {
                    _activeExamSession.value = session.copy(remainingSeconds = nextRemaining)
                }
            }
        }
    }

    private fun accumulateCurrentQuestionTime(session: ActiveExamSession): Map<String, Int> {
        val currentQ = session.questions.getOrNull(session.currentIndex) ?: return session.perQuestionSeconds
        val elapsed = ((System.currentTimeMillis() - session.questionStartTimeMs) / 1000L).toInt().coerceAtLeast(1)
        val prev = session.perQuestionSeconds[currentQ.id] ?: 0
        return session.perQuestionSeconds + (currentQ.id to (prev + elapsed))
    }

    fun selectExamAnswer(questionId: String, option: String) {
        _activeExamSession.update { s ->
            s?.copy(answers = s.answers + (questionId to option))
        }
    }

    fun clearExamAnswer(questionId: String) {
        _activeExamSession.update { s ->
            s?.copy(answers = s.answers - questionId)
        }
    }

    fun toggleMarkForReview(questionId: String) {
        _activeExamSession.update { s ->
            if (s == null) return@update null
            val next = if (questionId in s.markedForReviewIds) s.markedForReviewIds - questionId else s.markedForReviewIds + questionId
            s.copy(markedForReviewIds = next)
        }
    }

    fun jumpToExamQuestion(index: Int) {
        _activeExamSession.update { s ->
            if (s == null || index !in s.questions.indices) return@update s
            val updatedTimes = accumulateCurrentQuestionTime(s)
            val nextId = s.questions[index].id
            s.copy(
                currentIndex = index,
                visitedIds = s.visitedIds + nextId,
                perQuestionSeconds = updatedTimes,
                questionStartTimeMs = System.currentTimeMillis()
            )
        }
    }

    fun submitActiveExam() {
        timerJob?.cancel()
        val session = _activeExamSession.value ?: return
        if (session.submitted) return

        val finalTimes = accumulateCurrentQuestionTime(session)
        val exam = session.exam
        var correct = 0
        var incorrect = 0
        var unattempted = 0
        var score = 0.0
        val subjectCorrect = mutableMapOf<String, Int>()
        val subjectTotal = mutableMapOf<String, Int>()
        val weakTopicSet = linkedSetOf<String>()

        session.questions.forEach { q ->
            subjectTotal[q.subjectName] = (subjectTotal[q.subjectName] ?: 0) + 1
            val chosen = session.answers[q.id]
            when {
                chosen.isNullOrBlank() -> unattempted++
                chosen.trim().equals(q.correctAnswer.trim(), ignoreCase = true) -> {
                    correct++
                    score += exam.positiveMarks
                    subjectCorrect[q.subjectName] = (subjectCorrect[q.subjectName] ?: 0) + 1
                }
                else -> {
                    incorrect++
                    score -= exam.negativeMarks
                    weakTopicSet.add(q.topicName)
                }
            }
        }

        val maxScore = session.questions.size * exam.positiveMarks
        val attempted = correct + incorrect
        val accuracy = if (attempted > 0) (correct.toDouble() / attempted) * 100.0 else 0.0
        val timeTaken = (session.durationMinutes * 60 - session.remainingSeconds).coerceAtLeast(1)
        val subjectLines = subjectTotal.entries.map { (sub, tot) ->
            "$sub:${subjectCorrect[sub] ?: 0}/$tot"
        }

        val result = TestResultEntity(
            id = "test_${UUID.randomUUID().toString().replace("-", "").take(14)}",
            ownerKey = ownerKey.value,
            examId = exam.id,
            title = session.title,
            mode = session.mode,
            totalQuestions = session.questions.size,
            correctCount = correct,
            incorrectCount = incorrect,
            unattemptedCount = unattempted,
            score = score,
            maxScore = maxScore,
            accuracyPercent = accuracy,
            durationMinutes = session.durationMinutes,
            timeTakenSec = timeTaken,
            subjectSummaryJoined = subjectLines.joinToString("\n"),
            weakTopicsJoined = weakTopicSet.joinToString("\n")
        )

        _activeExamSession.value = session.copy(submitted = true, perQuestionSeconds = finalTimes)
        _completedExamReview.value = CompletedExamReview(
            result = result,
            exam = exam,
            questions = session.questions,
            userAnswers = session.answers,
            perQuestionSeconds = finalTimes
        )

        viewModelScope.launch {
            repository.saveTestResult(ownerKey.value, result)
            session.questions.forEach { q ->
                val chosen = session.answers[q.id].orEmpty()
                val skipped = chosen.isBlank()
                val isCorrect = !skipped && chosen.trim().equals(q.correctAnswer.trim(), ignoreCase = true)
                repository.recordQuestionAttempt(
                    ownerKey = ownerKey.value,
                    question = q,
                    selectedAnswer = chosen,
                    correct = isCorrect,
                    skipped = skipped,
                    timeSpentSec = finalTimes[q.id] ?: 15
                )
            }
        }

        _backStack.update { stack ->
            stack.filterNot { it is AppDestination.ActiveExam } + AppDestination.ExamResults
        }
    }

    // --- Bookmarks, Notes, Revisions, Study Planner ---
    fun toggleBookmark(
        subjectId: String,
        chapterId: String,
        type: BookmarkType,
        targetId: String,
        title: String,
        snippet: String,
        collectionName: String = "Saved"
    ) {
        viewModelScope.launch {
            val saved = repository.toggleBookmark(
                ownerKey = ownerKey.value,
                examId = activeExam.value.id,
                subjectId = subjectId,
                chapterId = chapterId,
                type = type,
                targetId = targetId,
                title = title,
                snippet = snippet,
                collectionName = collectionName
            )
            showMessage(if (saved) "Saved to Bookmarks" else "Removed from Bookmarks")
        }
    }

    fun removeBookmark(bookmarkId: String) {
        viewModelScope.launch {
            repository.removeBookmark(ownerKey.value, bookmarkId)
        }
    }

    fun savePersonalNote(subjectId: String, chapterId: String, title: String, content: String, highlighted: Boolean, existingId: String? = null) {
        viewModelScope.launch {
            repository.savePersonalNote(ownerKey.value, activeExam.value.id, subjectId, chapterId, title, content, highlighted, existingId)
            showMessage("Personal note saved")
        }
    }

    fun deletePersonalNote(noteId: String) {
        viewModelScope.launch {
            repository.deletePersonalNote(ownerKey.value, noteId)
            showMessage("Note deleted")
        }
    }

    fun markTopicConfidence(subjectId: String, chapterId: String, topicId: String, topicTitle: String, confidence: RevisionConfidence) {
        viewModelScope.launch {
            repository.markTopicRevision(
                ownerKey = ownerKey.value,
                examId = activeExam.value.id,
                subjectId = subjectId,
                chapterId = chapterId,
                topicId = topicId,
                topicTitle = topicTitle,
                confidence = confidence
            )
            showMessage("Scheduled next review (${confidence.label})")
        }
    }

    fun regenerateStudyPlan(dailyMinutes: Int) {
        viewModelScope.launch {
            val weakTopics = computeWeakTopicNames()
            repository.generateSmartStudyPlan(ownerKey.value, activeExam.value, dailyMinutes, weakTopics)
            showMessage("Generated personalized 7-day study plan")
        }
    }

    fun toggleStudyTask(task: StudyTaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(ownerKey.value, task)
        }
    }

    fun addCustomStudyTask(title: String, subjectName: String, durationMinutes: Int, taskType: String) {
        viewModelScope.launch {
            repository.addCustomStudyTask(ownerKey.value, activeExam.value.id, title, subjectName, durationMinutes, taskType)
            showMessage("Added task to study plan")
        }
    }

    fun deleteStudyTask(taskId: String) {
        viewModelScope.launch { repository.deleteStudyTask(taskId) }
    }

    fun toggleStudyPlanPaused(paused: Boolean) {
        viewModelScope.launch { settingsPrefs.setStudyPlanPaused(paused) }
    }

    // --- MASTREY AI Tutor ---
    fun updateAiDraft(text: String) {
        _aiDraftInput.value = text
    }

    fun selectConversation(convId: String?) {
        _activeConversationId.value = convId
        _aiStreamState.value = AiStreamState.Idle
    }

    fun startNewAiConversation() {
        _activeConversationId.value = null
        _aiStreamState.value = AiStreamState.Idle
    }

    fun clearActiveConversation() {
        val convId = _activeConversationId.value ?: return
        viewModelScope.launch {
            repository.clearConversation(convId)
            _activeConversationId.value = null
            _aiStreamState.value = AiStreamState.Idle
            showMessage("Conversation cleared")
        }
    }

    fun sendAiMessage(promptText: String, isRegenerate: Boolean = false) {
        val cleanPrompt = promptText.trim()
        if (cleanPrompt.isBlank()) return
        val isGuest = ownerKey.value == MastreyRepository.GUEST_KEY
        if (isGuest && settings.value.guestAiUsageCount >= MastreyRepository.GUEST_AI_LIMIT) {
            _aiStreamState.value = AiStreamState.Error(
                message = "Guest AI limit (${MastreyRepository.GUEST_AI_LIMIT} prompts) reached. Continue with your free Google account in Settings for extended AI access and cloud sync.",
                preservedPrompt = cleanPrompt
            )
            return
        }

        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            _aiDraftInput.value = ""
            _aiStreamState.value = AiStreamState.Loading

            val exam = activeExam.value
            val style = settings.value.aiExplanationStyle
            val key = ownerKey.value

            val activeConv = aiConversations.value.firstOrNull { it.id == _activeConversationId.value }
                ?: repository.createAiConversation(key, exam.group.name, cleanPrompt.take(50), style.label).also {
                    _activeConversationId.value = it.id
                }

            val priorHistory = activeMessages.value.map { AiHistoryTurn(isUser = it.role == "USER", text = it.content) }
            if (!isRegenerate) {
                repository.appendAiMessage(key, activeConv, "USER", cleanPrompt)
            }

            val ctx = AiTutorContext(
                exam = exam,
                subjectName = _aiContextSubject.value,
                chapterTitle = _aiContextChapter.value,
                style = style,
                responseLength = settings.value.aiResponseLength,
                weakTopics = computeWeakTopicNames(),
                includeWeakTopicContext = settings.value.aiPersonalizationEnabled
            )

            var finalResponse = ""
            try {
                repository.streamAiTutor(ctx, priorHistory, cleanPrompt).collect { partial ->
                    finalResponse = partial
                    _aiStreamState.value = AiStreamState.Streaming(partial)
                }
                if (finalResponse.isNotBlank()) {
                    repository.appendAiMessage(key, activeConv, "MODEL", finalResponse)
                    if (isGuest) settingsPrefs.incrementGuestAiUsage()
                    _aiStreamState.value = AiStreamState.Idle
                } else {
                    _aiDraftInput.value = cleanPrompt
                    _aiStreamState.value = AiStreamState.Error("Empty response received. Your message was preserved.", cleanPrompt)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiDraftInput.value = cleanPrompt
                _aiStreamState.value = AiStreamState.Error(
                    message = e.localizedMessage ?: "MASTREY AI is temporarily unavailable. Please check your connection and retry.",
                    preservedPrompt = cleanPrompt
                )
            }
        }
    }

    fun submitContentReport(targetType: String, targetId: String, reason: String) {
        viewModelScope.launch {
            repository.submitReport(ownerKey.value, activeExam.value.id, targetType, targetId, reason)
            showMessage("Thank you! Report recorded.")
        }
    }

    fun saveSearchQuery(query: String) {
        viewModelScope.launch { repository.saveRecentSearch(query) }
    }

    fun clearRecentSearches() {
        viewModelScope.launch { repository.clearRecentSearches() }
    }

    // --- Settings Mutations ---
    fun updateThemeMode(mode: ThemeModePreference) = viewModelScope.launch { settingsPrefs.updateThemeMode(mode) }
    fun updateAccent(accent: AccentColorPreference) = viewModelScope.launch { settingsPrefs.updateAccentColor(accent) }
    fun updateDensity(density: InterfaceDensityPreference) = viewModelScope.launch { settingsPrefs.updateDensity(density) }
    fun updateTextSize(size: TextSizePreference) = viewModelScope.launch { settingsPrefs.updateTextSize(size) }
    fun updateLineSpacing(mult: Float) = viewModelScope.launch { settingsPrefs.updateLineSpacing(mult) }
    fun updateReadingComfort(warm: Boolean) = viewModelScope.launch { settingsPrefs.updateReadingComfort(warm) }
    fun updateMathHighlight(enabled: Boolean) = viewModelScope.launch { settingsPrefs.updateMathHighlight(enabled) }
    fun updateReducedMotion(enabled: Boolean) = viewModelScope.launch { settingsPrefs.updateReducedMotion(enabled) }
    fun updateHighContrast(enabled: Boolean) = viewModelScope.launch { settingsPrefs.updateHighContrast(enabled) }
    fun updateLargerControls(enabled: Boolean) = viewModelScope.launch { settingsPrefs.updateLargerControls(enabled) }
    fun updateNotifications(
        enabled: Boolean? = null,
        reminderHour: Int? = null,
        studyReminders: Boolean? = null,
        revisionAlerts: Boolean? = null,
        testAlerts: Boolean? = null
    ) = viewModelScope.launch {
        settingsPrefs.updateNotifications(enabled, reminderHour, studyReminders, revisionAlerts, testAlerts)
    }
    fun updateAiSettings(style: AiResponseStyle? = null, length: String? = null, personalization: Boolean? = null) =
        viewModelScope.launch { settingsPrefs.updateAiSettings(style, length, personalization) }

    fun clearAiHistory() {
        viewModelScope.launch {
            repository.clearAllAiHistory(ownerKey.value)
            _activeConversationId.value = null
            showMessage("AI conversation history cleared")
        }
    }

    fun clearLocalCache() {
        viewModelScope.launch {
            repository.clearRecentSearches()
            showMessage("Local search & temporary cache cleared")
        }
    }

    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAccountAndData(ownerKey.value)
            settingsPrefs.resetAllPreferences()
            _backStack.value = listOf(AppDestination.Auth)
            showMessage("Account data cleared.")
            onComplete()
        }
    }

    fun buildExportSummaryText(): String {
        val p = profile.value
        val exam = activeExam.value
        return buildString {
            appendLine("=== MASTREY PRO STUDENT DATA EXPORT ===")
            appendLine("Name: ${p?.displayName ?: "Student"}")
            appendLine("Active Exam: ${exam.title} (Target ${p?.targetYear ?: 2027})")
            appendLine("XP: ${p?.xp ?: 0} | Streak: ${p?.streakDays ?: 0} days | Study Minutes: ${p?.totalStudyMinutes ?: 0}")
            appendLine("Questions Attempted: ${attempts.value.size} | Mock Tests Completed: ${testResults.value.size}")
            appendLine("Bookmarks: ${bookmarks.value.size} | Personal Notes: ${notes.value.size}")
            appendLine("\n--- Recent Mock Tests ---")
            testResults.value.take(10).forEach {
                appendLine("• ${it.title}: ${it.score}/${it.maxScore} (${"%.1f".format(it.accuracyPercent)}% accuracy)")
            }
            appendLine("\n--- Saved Bookmarks ---")
            bookmarks.value.take(25).forEach {
                appendLine("• [${it.itemType}] ${it.title}: ${it.snippet.take(80)}")
            }
        }
    }

    fun computeWeakTopicNames(): List<String> {
        val examAttempts = attempts.value.filter { it.examId == activeExam.value.id && !it.skipped }
        if (examAttempts.isEmpty()) return emptyList()
        val chaptersById = SyllabusCatalog.chaptersFor(activeExam.value).associateBy { it.id }
        return examAttempts.groupBy { it.chapterId }
            .mapValues { (_, list) ->
                val correct = list.count { it.correct }
                correct.toDouble() / list.size
            }
            .filter { it.value < 0.7 }
            .entries.sortedBy { it.value }
            .mapNotNull { chaptersById[it.key]?.title }
            .take(5)
    }

    fun getLastOpenedChapter(): ChapterItem? {
        val chapters = SyllabusCatalog.chaptersFor(activeExam.value)
        val lastId = profile.value?.lastChapterId
        return chapters.firstOrNull { it.id == lastId } ?: chapters.firstOrNull()
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = MastreyDatabase.getInstance(application)
                    val prefs = SettingsPreferences(application)
                    val firestoreSync = FirestoreSyncRepository(application)
                    val repo = MastreyRepository(db.mastreyDao(), prefs, firestoreSync)
                    return MastreyViewModel(application, repo, prefs) as T
                }
            }
    }
}
