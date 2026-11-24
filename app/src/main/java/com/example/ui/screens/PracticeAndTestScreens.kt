package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.content.SyllabusCatalog
import com.example.data.model.BookmarkType
import com.example.data.model.QuestionDifficulty
import com.example.data.model.QuestionItem
import com.example.data.repository.RepeatPolicy
import com.example.ui.AppDestination
import com.example.ui.MastreyViewModel
import com.example.ui.PrimaryTab
import com.example.ui.components.ExamBadgePill
import com.example.ui.components.FormulaExpressionBox
import com.example.ui.components.MastreyCard
import com.example.ui.components.MastreyEmptyState
import com.example.ui.components.MastreyGradientButton
import com.example.ui.components.ReportIssueDialog
import com.example.ui.components.SubjectBarChart
import com.example.ui.components.VerificationBadge
import com.example.ui.components.examAccentColor
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.VividViolet
import com.example.ui.theme.WarningAmber

@Composable
fun PracticeScreen(
    viewModel: MastreyViewModel
) {
    val activeExam by viewModel.activeExam.collectAsStateWithLifecycle()
    val attempts by viewModel.attempts.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val cachedAi by viewModel.cachedAiQuestions.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingQuestions.collectAsStateWithLifecycle()
    val genError by viewModel.questionGenError.collectAsStateWithLifecycle()
    val preSubject by viewModel.practiceSubjectFilter.collectAsStateWithLifecycle()
    val preChapter by viewModel.practiceChapterFilter.collectAsStateWithLifecycle()
    val prePolicy by viewModel.practiceRepeatPolicy.collectAsStateWithLifecycle()

    val subjects = remember(activeExam) { SyllabusCatalog.subjectsFor(activeExam) }
    val chapters = remember(activeExam) { SyllabusCatalog.chaptersFor(activeExam) }

    var selectedSubjectId by remember(activeExam, preSubject) { mutableStateOf(preSubject) }
    var selectedChapterId by remember(activeExam, preChapter) { mutableStateOf(preChapter) }
    var selectedDifficulty by remember { mutableStateOf<QuestionDifficulty?>(null) }
    var repeatPolicy by remember(prePolicy) { mutableStateOf(prePolicy) }

    val selection = remember(
        activeExam, attempts, bookmarks, cachedAi, selectedSubjectId, selectedChapterId, selectedDifficulty, repeatPolicy
    ) {
        viewModel.selectPracticeQuestions(
            subjectId = selectedSubjectId,
            chapterId = selectedChapterId,
            difficulty = selectedDifficulty,
            policy = repeatPolicy,
            count = 20
        )
    }

    var currentIndex by remember(selectedSubjectId, selectedChapterId, selectedDifficulty, repeatPolicy, activeExam) { mutableIntStateOf(0) }
    val questions = selection.questions
    val activeQuestion = questions.getOrNull(currentIndex.coerceIn(0, (questions.size - 1).coerceAtLeast(0)))

    var selectedOption by remember(activeQuestion?.id) { mutableStateOf<String?>(null) }
    var checked by remember(activeQuestion?.id) { mutableStateOf(false) }
    var questionStartMs by remember(activeQuestion?.id) { mutableStateOf(System.currentTimeMillis()) }
    var reportQuestionId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Practice Engine", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Non-repeating smart question pool",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ExamBadgePill(activeExam)
            }
        }

        // Subject Filter
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedSubjectId == null,
                        onClick = {
                            selectedSubjectId = null
                            selectedChapterId = null
                        },
                        label = { Text("All Subjects") }
                    )
                }
                items(subjects, key = { it.id }) { sub ->
                    FilterChip(
                        selected = selectedSubjectId == sub.id,
                        onClick = {
                            selectedSubjectId = sub.id
                            selectedChapterId = null
                        },
                        label = { Text(sub.name) }
                    )
                }
            }
        }

        // Repeat Policy & Difficulty
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(RepeatPolicy.entries) { policy ->
                    FilterChip(
                        selected = repeatPolicy == policy,
                        onClick = { repeatPolicy = policy },
                        label = { Text(policy.label) },
                        modifier = Modifier.testTag("policy_${policy.name.lowercase()}")
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedDifficulty == null,
                    onClick = { selectedDifficulty = null },
                    label = { Text("Any Level") }
                )
                QuestionDifficulty.entries.forEach { diff ->
                    FilterChip(
                        selected = selectedDifficulty == diff,
                        onClick = { selectedDifficulty = diff },
                        label = { Text(diff.label) }
                    )
                }
            }
        }

        // AI Question Generation Bar
        item {
            val targetSub = subjects.firstOrNull { it.id == selectedSubjectId } ?: subjects.first()
            val targetChap = chapters.firstOrNull { it.id == selectedChapterId }
                ?: chapters.firstOrNull { it.subjectId == targetSub.id }
                ?: chapters.first()

            MastreyCard(accentBorder = VividViolet) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Need Fresh Practice Questions?", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Generate AI-validated ${activeExam.title} questions for ${targetChap.title}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.generateAiPracticeQuestions(
                                subjectId = targetSub.id,
                                subjectName = targetSub.name,
                                chapterId = targetChap.id,
                                chapterName = targetChap.title,
                                difficulty = selectedDifficulty ?: QuestionDifficulty.MEDIUM,
                                count = 3
                            )
                        },
                        enabled = !isGenerating,
                        modifier = Modifier.testTag("practice_generate_ai_button")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Generate")
                        }
                    }
                }
                if (!genError.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(genError!!, style = MaterialTheme.typography.bodySmall, color = ErrorRed)
                }
            }
        }

        if (!selection.poolWarning.isNullOrBlank()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = WarningAmber.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        selection.poolWarning,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        if (activeQuestion == null) {
            item {
                MastreyEmptyState(
                    icon = Icons.Filled.Quiz,
                    title = "No Questions in Current Filter",
                    subtitle = "Switch to 'All / Revision Mode' or tap Generate above to create fresh AI-validated questions for this exam.",
                    actionLabel = "Switch to Revision Mode",
                    onAction = { repeatPolicy = RepeatPolicy.ALL_REVISION }
                )
            }
        } else {
            item {
                val isBookmarked = bookmarks.any { it.targetId == activeQuestion.id && it.itemType == BookmarkType.QUESTION.name }
                MastreyCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Q${currentIndex + 1}/${questions.size} • ${activeQuestion.subjectName} • ${activeQuestion.difficulty.label}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row {
                            IconButton(
                                onClick = {
                                    viewModel.toggleBookmark(
                                        subjectId = activeQuestion.subjectId,
                                        chapterId = activeQuestion.chapterId,
                                        type = BookmarkType.QUESTION,
                                        targetId = activeQuestion.id,
                                        title = "${activeQuestion.topicName} (${activeQuestion.difficulty.label})",
                                        snippet = activeQuestion.questionText
                                    )
                                }
                            ) {
                                Icon(
                                    if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                    contentDescription = "Bookmark Question",
                                    tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { reportQuestionId = activeQuestion.id }) {
                                Icon(Icons.Filled.Flag, contentDescription = "Report Question")
                            }
                        }
                    }

                    VerificationBadge(activeQuestion.verificationStatus)
                    Spacer(Modifier.height(10.dp))
                    Text(activeQuestion.questionText, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))

                    activeQuestion.options.forEachIndexed { idx, opt ->
                        val isChosen = selectedOption == opt
                        val isCorrectOpt = opt.trim().equals(activeQuestion.correctAnswer.trim(), ignoreCase = true)
                        val border = when {
                            checked && isCorrectOpt -> SuccessGreen
                            checked && isChosen && !isCorrectOpt -> ErrorRed
                            isChosen -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        }
                        val bg = when {
                            checked && isCorrectOpt -> SuccessGreen.copy(alpha = 0.14f)
                            checked && isChosen && !isCorrectOpt -> ErrorRed.copy(alpha = 0.14f)
                            isChosen -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            else -> MaterialTheme.colorScheme.surface
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = !checked) { selectedOption = opt }
                                .testTag("practice_option_$idx"),
                            shape = RoundedCornerShape(12.dp),
                            color = bg,
                            border = BorderStroke(1.2.dp, border)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${('A' + idx)}. ",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = border
                                )
                                Text(opt, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        if (!checked) {
                            MastreyGradientButton(
                                text = "Check Answer",
                                enabled = selectedOption != null,
                                onClick = {
                                    val chosen = selectedOption ?: return@MastreyGradientButton
                                    val elapsed = ((System.currentTimeMillis() - questionStartMs) / 1000L).toInt().coerceAtLeast(1)
                                    checked = true
                                    viewModel.recordPracticeAnswer(activeQuestion, chosen, elapsed)
                                },
                                modifier = Modifier.weight(1f).testTag("practice_check_button")
                            )
                        } else {
                            MastreyGradientButton(
                                text = if (currentIndex < questions.lastIndex) "Next Question" else "Restart Set",
                                icon = Icons.AutoMirrored.Filled.ArrowForward,
                                onClick = {
                                    currentIndex = if (currentIndex < questions.lastIndex) currentIndex + 1 else 0
                                    selectedOption = null
                                    checked = false
                                    questionStartMs = System.currentTimeMillis()
                                },
                                modifier = Modifier.weight(1f).testTag("practice_next_button")
                            )
                        }
                    }

                    if (checked) {
                        Spacer(Modifier.height(14.dp))
                        val isRight = selectedOption?.trim().equals(activeQuestion.correctAnswer.trim(), ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isRight) SuccessGreen.copy(alpha = 0.12f) else ErrorRed.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    if (isRight) "Correct Answer!" else "Incorrect • Correct Answer: ${activeQuestion.correctAnswer}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isRight) SuccessGreen else ErrorRed
                                )
                                Spacer(Modifier.height(6.dp))
                                activeQuestion.solutionSteps.forEachIndexed { idx, step ->
                                    Text("${idx + 1}. $step", style = MaterialTheme.typography.bodySmall)
                                }
                                if (activeQuestion.explanation.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(activeQuestion.explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (activeQuestion.relatedFormula.isNotBlank()) {
                                    Spacer(Modifier.height(8.dp))
                                    FormulaExpressionBox(activeQuestion.relatedFormula)
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(20.dp)) }
    }

    if (reportQuestionId != null) {
        ReportIssueDialog(
            title = "Report Question Issue",
            onDismiss = { reportQuestionId = null },
            onSubmit = { reason -> viewModel.submitContentReport("QUESTION", reportQuestionId!!, reason) }
        )
    }
}

@Composable
fun TestGeneratorScreen(
    viewModel: MastreyViewModel
) {
    val activeExam by viewModel.activeExam.collectAsStateWithLifecycle()
    val testResults by viewModel.testResults.collectAsStateWithLifecycle()
    val subjects = remember(activeExam) { SyllabusCatalog.subjectsFor(activeExam) }

    var testTitle by remember(activeExam) { mutableStateOf("${activeExam.title} Custom Mock Test") }
    var selectedSubjectId by remember(activeExam) { mutableStateOf<String?>(null) }
    var selectedDifficulty by remember { mutableStateOf<QuestionDifficulty?>(null) }
    var repeatPolicy by remember { mutableStateOf(RepeatPolicy.UNSEEN_ONLY) }
    var questionCount by remember { mutableIntStateOf(6) }
    var durationMinutes by remember { mutableFloatStateOf(30f) }
    var mode by remember { mutableStateOf("EXAM") }
    var launchWarning by remember { mutableStateOf<String?>(null) }

    val examHistory = remember(testResults, activeExam) { testResults.filter { it.examId == activeExam.id } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Mock Test Engine", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Timed simulation • +${activeExam.positiveMarks} / -${activeExam.negativeMarks}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ExamBadgePill(activeExam)
            }
        }

        item {
            MastreyCard(accentBorder = examAccentColor(activeExam)) {
                Text("Configure Your Test", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = testTitle,
                    onValueChange = { testTitle = it },
                    label = { Text("Test Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("test_title_input")
                )

                Spacer(Modifier.height(12.dp))
                Text("Subject Scope", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedSubjectId == null,
                            onClick = { selectedSubjectId = null },
                            label = { Text("Full ${activeExam.shortTitle}") }
                        )
                    }
                    items(subjects, key = { it.id }) { sub ->
                        FilterChip(
                            selected = selectedSubjectId == sub.id,
                            onClick = { selectedSubjectId = sub.id },
                            label = { Text(sub.name) }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Mode", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = mode == "EXAM", onClick = { mode = "EXAM" }, label = { Text("Exam Mode (Strict)") })
                    FilterChip(selected = mode == "PRACTICE", onClick = { mode = "PRACTICE" }, label = { Text("Practice Mode") })
                }

                Spacer(Modifier.height(12.dp))
                Text("Question Pool Policy", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(RepeatPolicy.entries) { p ->
                        FilterChip(selected = repeatPolicy == p, onClick = { repeatPolicy = p }, label = { Text(p.label) })
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Questions: $questionCount", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 6, 10, 15, 25).forEach { count ->
                        FilterChip(selected = questionCount == count, onClick = { questionCount = count }, label = { Text("$count Q") })
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Duration: ${durationMinutes.toInt()} minutes (10–180 min)", style = MaterialTheme.typography.titleSmall)
                Slider(
                    value = durationMinutes,
                    onValueChange = { durationMinutes = it },
                    valueRange = 10f..180f,
                    steps = 16
                )

                if (!launchWarning.isNullOrBlank()) {
                    Text(launchWarning!!, style = MaterialTheme.typography.bodySmall, color = WarningAmber)
                    Spacer(Modifier.height(8.dp))
                }

                MastreyGradientButton(
                    text = "Start Timed Test",
                    icon = Icons.Filled.Timer,
                    onClick = {
                        launchWarning = viewModel.startMockTest(
                            title = testTitle,
                            subjectId = selectedSubjectId,
                            chapterId = null,
                            difficulty = selectedDifficulty,
                            questionCount = questionCount,
                            durationMinutes = durationMinutes.toInt(),
                            mode = mode,
                            policy = repeatPolicy
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("start_mock_test_button")
                )
            }
        }

        item {
            Text("Recent ${activeExam.title} Test History", style = MaterialTheme.typography.titleLarge)
        }

        if (examHistory.isEmpty()) {
            item {
                MastreyEmptyState(
                    icon = Icons.Filled.Assignment,
                    title = "No Mock Tests Yet",
                    subtitle = "Launch your first timed simulation above to unlock real score trends and question-by-question review."
                )
            }
        } else {
            items(examHistory.take(8), key = { it.id }) { res ->
                MastreyCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(res.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${res.correctCount}✓  ${res.incorrectCount}✗  ${res.unattemptedCount} skipped • ${"%.1f".format(res.accuracyPercent)}% accuracy",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "${"%.1f".format(res.score)}/${res.maxScore.toInt()}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(20.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveExamScreen(
    viewModel: MastreyViewModel
) {
    val session by viewModel.activeExamSession.collectAsStateWithLifecycle()
    var showPaletteSheet by remember { mutableStateOf(false) }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler { showExitDialog = true }

    val active = session
    if (active == null) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
            MastreyEmptyState(
                icon = Icons.Filled.Timer,
                title = "No Active Test",
                subtitle = "Return to the test generator to start a mock test.",
                actionLabel = "Back",
                onAction = { viewModel.navigateBack() }
            )
        }
        return
    }

    val currentQ = active.questions[active.currentIndex]
    val chosenAnswer = active.answers[currentQ.id]
    val isMarked = currentQ.id in active.markedForReviewIds

    val mins = active.remainingSeconds / 60
    val secs = active.remainingSeconds % 60
    val timerColor = when {
        active.remainingSeconds <= 120 -> ErrorRed
        active.remainingSeconds <= 300 -> WarningAmber
        else -> CyberCyan
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(active.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                        Text(
                            "Q ${active.currentIndex + 1} of ${active.questions.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.Filled.Close, contentDescription = "Exit Test")
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = timerColor.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, timerColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Timer, contentDescription = "Remaining Time", tint = timerColor, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "%02d:%02d".format(mins, secs),
                                style = MaterialTheme.typography.labelLarge,
                                color = timerColor
                            )
                        }
                    }
                    IconButton(onClick = { showPaletteSheet = true }, modifier = Modifier.testTag("exam_palette_button")) {
                        Icon(Icons.Filled.GridView, contentDescription = "Question Palette")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 4.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (active.currentIndex > 0) viewModel.jumpToExamQuestion(active.currentIndex - 1) },
                        enabled = active.currentIndex > 0
                    ) { Text("Prev") }

                    OutlinedButton(
                        onClick = { viewModel.toggleMarkForReview(currentQ.id) }
                    ) {
                        Text(if (isMarked) "Unmark" else "Mark")
                    }

                    Spacer(Modifier.weight(1f))

                    if (active.currentIndex < active.questions.lastIndex) {
                        Button(
                            onClick = { viewModel.jumpToExamQuestion(active.currentIndex + 1) },
                            modifier = Modifier.testTag("exam_save_next_button")
                        ) { Text("Save & Next") }
                    } else {
                        Button(
                            onClick = { showSubmitDialog = true },
                            modifier = Modifier.testTag("exam_submit_button")
                        ) { Text("Submit") }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MastreyCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${currentQ.subjectName} • ${currentQ.topicName}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (isMarked) {
                            Text("Marked for Review", style = MaterialTheme.typography.labelMedium, color = VividViolet)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(currentQ.questionText, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(14.dp))

                    currentQ.options.forEachIndexed { idx, opt ->
                        val selected = chosenAnswer == opt
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.selectExamAnswer(currentQ.id, opt) }
                                .testTag("exam_opt_$idx"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.2.dp,
                                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("${('A' + idx)}. ", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                Text(opt, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    if (chosenAnswer != null) {
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.clearExamAnswer(currentQ.id) }) {
                            Text("Clear Selected Answer")
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { showSubmitDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Submit Test Early (${active.answers.size}/${active.questions.size} answered)")
                }
            }
        }
    }

    if (showPaletteSheet) {
        ModalBottomSheet(onDismissRequest = { showPaletteSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Question Navigator Palette", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Green = Answered • Violet = Marked • Amber = Visited Unanswered • Gray = Unvisited",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(220.dp)
                ) {
                    itemsIndexed(active.questions) { idx, q ->
                        val answered = active.answers.containsKey(q.id)
                        val marked = q.id in active.markedForReviewIds
                        val visited = q.id in active.visitedIds
                        val color = when {
                            answered && marked -> CyberCyan
                            marked -> VividViolet
                            answered -> SuccessGreen
                            visited -> WarningAmber
                            else -> MaterialTheme.colorScheme.outline
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(color.copy(alpha = 0.22f))
                                .clickable {
                                    viewModel.jumpToExamQuestion(idx)
                                    showPaletteSheet = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${idx + 1}", style = MaterialTheme.typography.titleSmall, color = color)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("Submit Mock Test?") },
            text = {
                Text("Answered: ${active.answers.size} / ${active.questions.size}\nMarked for Review: ${active.markedForReviewIds.size}\nYou will immediately see your score and detailed solutions.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        viewModel.submitActiveExam()
                    },
                    modifier = Modifier.testTag("confirm_submit_exam")
                ) { Text("Submit Now") }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) { Text("Continue Test") }
            }
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Leave Active Exam?") },
            text = { Text("Do you want to submit your progress now or keep attempting?") },
            confirmButton = {
                Button(onClick = {
                    showExitDialog = false
                    viewModel.submitActiveExam()
                }) { Text("Submit & View Score") }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("Stay in Exam") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamResultsScreen(
    viewModel: MastreyViewModel
) {
    BackHandler { viewModel.switchPrimaryTab(PrimaryTab.TESTS) }

    val review by viewModel.completedExamReview.collectAsStateWithLifecycle()
    var reportQId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exam Results & Solutions") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.switchPrimaryTab(PrimaryTab.TESTS) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Tests")
                    }
                }
            )
        }
    ) { padding ->
        val data = review
        if (data == null) {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                MastreyEmptyState(
                    icon = Icons.Filled.CheckCircle,
                    title = "No Completed Result Selected",
                    subtitle = "Complete a mock test to view full analytics and question-by-question solutions."
                )
            }
            return@Scaffold
        }

        val res = data.result
        val pct = if (res.maxScore > 0) ((res.score / res.maxScore) * 100.0).coerceIn(0.0, 100.0) else 0.0
        val avgSec = if (res.totalQuestions > 0) res.timeTakenSec / res.totalQuestions else 0
        val subjectEntries = remember(res.subjectSummaryJoined) {
            res.subjectSummaryJoined.lines().mapNotNull { line ->
                val parts = line.split(":")
                if (parts.size != 2) return@mapNotNull null
                val frac = parts[1].split("/")
                val c = frac.getOrNull(0)?.toFloatOrNull() ?: 0f
                val t = frac.getOrNull(1)?.toFloatOrNull()?.coerceAtLeast(1f) ?: 1f
                parts[0] to ((c / t) * 100f)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MastreyCard(accentBorder = examAccentColor(data.exam)) {
                    Text(res.title, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text("TOTAL SCORE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${"%.1f".format(res.score)} / ${res.maxScore.toInt()}",
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${"%.1f".format(pct)}% Score", style = MaterialTheme.typography.titleMedium)
                            Text("${"%.1f".format(res.accuracyPercent)}% Accuracy", style = MaterialTheme.typography.labelLarge, color = SuccessGreen)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("✓ Correct: ${res.correctCount}", color = SuccessGreen, style = MaterialTheme.typography.labelLarge)
                        Text("✗ Wrong: ${res.incorrectCount}", color = ErrorRed, style = MaterialTheme.typography.labelLarge)
                        Text("• Skipped: ${res.unattemptedCount}", color = WarningAmber, style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Time Taken: ${res.timeTakenSec / 60}m ${res.timeTakenSec % 60}s • Avg ${avgSec}s / question",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (subjectEntries.isNotEmpty()) {
                item {
                    MastreyCard {
                        Text("Subject Breakdown", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(10.dp))
                        SubjectBarChart(entries = subjectEntries)
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    MastreyGradientButton(
                        text = "Retry Wrong",
                        onClick = { viewModel.openPracticeFor(null, null, RepeatPolicy.WRONG_ONLY) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(AppDestination.FormulaCenter) },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) { Text("Review Formulas") }
                }
            }

            item {
                Text("Question-by-Question Solution Review", style = MaterialTheme.typography.titleLarge)
            }

            itemsIndexed(data.questions, key = { _, q -> q.id }) { idx, q ->
                val userAns = data.userAnswers[q.id]
                val isCorrect = !userAns.isNullOrBlank() && userAns.trim().equals(q.correctAnswer.trim(), ignoreCase = true)
                val statusColor = when {
                    userAns.isNullOrBlank() -> WarningAmber
                    isCorrect -> SuccessGreen
                    else -> ErrorRed
                }
                MastreyCard(accentBorder = statusColor) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Q${idx + 1} • ${q.subjectName} • ${when { userAns.isNullOrBlank() -> "Unattempted"; isCorrect -> "Correct"; else -> "Incorrect" }}",
                            style = MaterialTheme.typography.labelLarge,
                            color = statusColor
                        )
                        IconButton(onClick = { reportQId = q.id }) {
                            Icon(Icons.Filled.Flag, contentDescription = "Report Question", modifier = Modifier.size(18.dp))
                        }
                    }
                    Text(q.questionText, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))
                    Text("Your Answer: ${userAns ?: "Not Attempted"}", style = MaterialTheme.typography.bodySmall, color = statusColor)
                    Text("Correct Answer: ${q.correctAnswer}", style = MaterialTheme.typography.bodySmall, color = SuccessGreen)
                    Spacer(Modifier.height(6.dp))
                    q.solutionSteps.forEachIndexed { sIdx, step ->
                        Text("${sIdx + 1}. $step", style = MaterialTheme.typography.bodySmall)
                    }
                    if (q.relatedFormula.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        FormulaExpressionBox(q.relatedFormula)
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }

    if (reportQId != null) {
        ReportIssueDialog(
            title = "Report Solution Error",
            onDismiss = { reportQId = null },
            onSubmit = { reason -> viewModel.submitContentReport("QUESTION", reportQId!!, reason) }
        )
    }
}
