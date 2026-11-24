package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.content.SyllabusCatalog
import com.example.data.model.BookmarkType
import com.example.data.model.RevisionConfidence
import com.example.data.repository.RepeatPolicy
import com.example.ui.MastreyViewModel
import com.example.ui.components.ExamBadgePill
import com.example.ui.components.MastreyCard
import com.example.ui.components.MastreyEmptyState
import com.example.ui.components.MastreyGradientButton
import com.example.ui.components.ScoreTrendChart
import com.example.ui.components.SubjectBarChart
import com.example.ui.components.examAccentColor
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.LocalMastreyDesignConfig
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.VividViolet
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisionCenterScreen(
    viewModel: MastreyViewModel
) {
    BackHandler { viewModel.navigateBack() }

    val activeExam by viewModel.activeExam.collectAsStateWithLifecycle()
    val revisions by viewModel.revisions.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val designCfg = LocalMastreyDesignConfig.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddNoteDialog by remember { mutableStateOf(false) }

    val examRevisions = remember(revisions, activeExam) { revisions.filter { it.examId == activeExam.id } }
    val examNotes = remember(notes, activeExam) { notes.filter { it.examId == activeExam.id } }
    val baseFlashcards = remember(activeExam) { SyllabusCatalog.flashcardsFor(activeExam) }

    var flashSubjectFilter by remember(activeExam) { mutableStateOf("All") }
    var shuffleSeed by remember { mutableIntStateOf(0) }
    val flashcards = remember(baseFlashcards, flashSubjectFilter, shuffleSeed) {
        val list = baseFlashcards.filter { flashSubjectFilter == "All" || it.subjectName == flashSubjectFilter }
        if (shuffleSeed == 0) list else list.shuffled()
    }
    var cardIndex by remember(flashcards) { mutableIntStateOf(0) }
    var flipped by remember(cardIndex, flashcards) { mutableStateOf(false) }
    var showHint by remember(cardIndex, flashcards) { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = if (designCfg.reducedMotion) 0 else 420),
        label = "flashcardFlip"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revision & Flashcards") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 2) {
                FloatingActionButton(
                    onClick = { showAddNoteDialog = true },
                    modifier = Modifier.testTag("add_personal_note_fab")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Personal Note")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Spaced Rep") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Flashcards") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("My Notes") })
            }

            when (selectedTab) {
                0 -> {
                    val now = System.currentTimeMillis()
                    val due = examRevisions.filter { it.nextReviewEpochMs <= now }
                    val upcoming = examRevisions.filter { it.nextReviewEpochMs > now }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (examRevisions.isEmpty()) {
                            item {
                                MastreyEmptyState(
                                    icon = Icons.Filled.Refresh,
                                    title = "No Scheduled Revisions Yet",
                                    subtitle = "Open any chapter in Learn and mark topics as Easy, Need Revision, or Difficult to build your spaced-repetition queue.",
                                    actionLabel = "Go to Learn",
                                    onAction = { viewModel.navigateBack() }
                                )
                            }
                        } else {
                            if (due.isNotEmpty()) {
                                item { Text("Due for Revision Today (${due.size})", style = MaterialTheme.typography.titleMedium, color = WarningAmber) }
                                items(due, key = { it.id }) { rev ->
                                    MastreyCard(accentBorder = WarningAmber) {
                                        Text(rev.topicTitle, style = MaterialTheme.typography.titleMedium)
                                        Text("Reviewed ${rev.reviewCount} times • Last: ${rev.confidence}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            RevisionConfidence.entries.forEach { conf ->
                                                FilterChip(
                                                    selected = false,
                                                    onClick = {
                                                        viewModel.markTopicConfidence(rev.subjectId, rev.chapterId, rev.topicId, rev.topicTitle, conf)
                                                    },
                                                    label = { Text(conf.label) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            if (upcoming.isNotEmpty()) {
                                item { Text("Mastered & Scheduled (${upcoming.size})", style = MaterialTheme.typography.titleMedium, color = SuccessGreen) }
                                items(upcoming, key = { it.id }) { rev ->
                                    MastreyCard {
                                        Text(rev.topicTitle, style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            "Status: ${rev.confidence} • Reviewed ${rev.reviewCount}x",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    val subjectNames = remember(baseFlashcards) { listOf("All") + baseFlashcards.map { it.subjectName }.distinct() }
                    val currentCard = flashcards.getOrNull(cardIndex.coerceIn(0, (flashcards.size - 1).coerceAtLeast(0)))
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
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                                    items(subjectNames) { name ->
                                        FilterChip(
                                            selected = flashSubjectFilter == name,
                                            onClick = { flashSubjectFilter = name },
                                            label = { Text(name) }
                                        )
                                    }
                                }
                                IconButton(onClick = { shuffleSeed++ }) {
                                    Icon(Icons.Filled.Shuffle, contentDescription = "Shuffle Flashcards")
                                }
                            }
                        }
                        if (currentCard == null) {
                            item {
                                MastreyEmptyState(
                                    icon = Icons.Filled.Style,
                                    title = "No Flashcards Found",
                                    subtitle = "Select another subject filter."
                                )
                            }
                        } else {
                            item {
                                Text(
                                    "Card ${cardIndex + 1} of ${flashcards.size} • ${currentCard.subjectName} • ${currentCard.category.label}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            item {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp)
                                        .graphicsLayer {
                                            rotationY = rotation
                                            cameraDistance = 12f * density
                                        }
                                        .clickable { flipped = !flipped }
                                        .testTag("flashcard_flip_surface"),
                                    shape = RoundedCornerShape(22.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.5.dp, examAccentColor(activeExam).copy(alpha = 0.6f))
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize().padding(22.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (rotation <= 90f) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("QUESTION / PROMPT", style = MaterialTheme.typography.labelMedium, color = CyberCyan)
                                                Spacer(Modifier.height(14.dp))
                                                Text(currentCard.front, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                                                Spacer(Modifier.height(16.dp))
                                                Text("Tap card to flip", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        } else {
                                            Column(
                                                modifier = Modifier.graphicsLayer { rotationY = 180f },
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text("ANSWER & TAKEAWAY", style = MaterialTheme.typography.labelMedium, color = SuccessGreen)
                                                Spacer(Modifier.height(14.dp))
                                                Text(currentCard.back, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                                            }
                                        }
                                    }
                                }
                            }
                            if (showHint) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = CyberCyan.copy(alpha = 0.12f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Hint: ${currentCard.hint}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
                                    }
                                }
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(onClick = { showHint = !showHint }, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Filled.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(if (showHint) "Hide Hint" else "Hint")
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.toggleBookmark(
                                                subjectId = currentCard.subjectId,
                                                chapterId = currentCard.chapterId,
                                                type = BookmarkType.FLASHCARD,
                                                targetId = currentCard.id,
                                                title = currentCard.front,
                                                snippet = currentCard.back
                                            )
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Filled.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Save")
                                    }
                                }
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.markTopicConfidence(currentCard.subjectId, currentCard.chapterId, currentCard.id, currentCard.front.take(40), RevisionConfidence.DIFFICULT)
                                            cardIndex = (cardIndex + 1) % flashcards.size
                                            flipped = false
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Repeat") }
                                    MastreyGradientButton(
                                        text = "Know It • Next",
                                        onClick = {
                                            viewModel.markTopicConfidence(currentCard.subjectId, currentCard.chapterId, currentCard.id, currentCard.front.take(40), RevisionConfidence.EASY)
                                            cardIndex = (cardIndex + 1) % flashcards.size
                                            flipped = false
                                        },
                                        modifier = Modifier.weight(1.3f)
                                    )
                                }
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (examNotes.isEmpty()) {
                            item {
                                MastreyEmptyState(
                                    icon = Icons.Filled.EditNote,
                                    title = "No Personal Notes Yet",
                                    subtitle = "Tap + to capture formulas, mnemonics, mistakes, or short revision pointers.",
                                    actionLabel = "Create First Note",
                                    onAction = { showAddNoteDialog = true }
                                )
                            }
                        } else {
                            items(examNotes, key = { it.id }) { note ->
                                MastreyCard(accentBorder = if (note.highlighted) WarningAmber else null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(note.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                        Row {
                                            IconButton(
                                                onClick = {
                                                    viewModel.savePersonalNote(
                                                        subjectId = note.subjectId,
                                                        chapterId = note.chapterId,
                                                        title = note.title,
                                                        content = note.content,
                                                        highlighted = !note.highlighted,
                                                        existingId = note.id
                                                    )
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Filled.Star,
                                                    contentDescription = "Highlight Note",
                                                    tint = if (note.highlighted) WarningAmber else MaterialTheme.colorScheme.outline
                                                )
                                            }
                                            IconButton(onClick = { viewModel.deletePersonalNote(note.id) }) {
                                                Icon(Icons.Filled.Delete, contentDescription = "Delete Note")
                                            }
                                        }
                                    }
                                    Text(note.content, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddNoteDialog) {
        var title by remember { mutableStateOf("") }
        var body by remember { mutableStateOf("") }
        var highlight by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("New Personal Study Note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Note Content") }, minLines = 4, modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = highlight, onCheckedChange = { highlight = it })
                        Text("Highlight as High-Priority")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && body.isNotBlank()) {
                            viewModel.savePersonalNote(activeExam.id, "general", title, body, highlight)
                            showAddNoteDialog = false
                        }
                    },
                    enabled = title.isNotBlank() && body.isNotBlank()
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: MastreyViewModel
) {
    BackHandler { viewModel.navigateBack() }

    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val activeExam by viewModel.activeExam.collectAsStateWithLifecycle()
    val attempts by viewModel.attempts.collectAsStateWithLifecycle()
    val testResults by viewModel.testResults.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()

    val examAttempts = remember(attempts, activeExam) { attempts.filter { it.examId == activeExam.id && !it.skipped } }
    val examTests = remember(testResults, activeExam) { testResults.filter { it.examId == activeExam.id } }
    val accuracy = remember(examAttempts) {
        if (examAttempts.isEmpty()) 0.0 else (examAttempts.count { it.correct }.toDouble() / examAttempts.size) * 100.0
    }
    val weakTopics = remember(attempts, activeExam) { viewModel.computeWeakTopicNames() }

    val scoreSeries = remember(examTests) {
        examTests.reversed().map { if (it.maxScore > 0) ((it.score / it.maxScore) * 100.0).toFloat() else 0f }
    }
    val subjectBars = remember(examAttempts, activeExam) {
        val subs = SyllabusCatalog.subjectsFor(activeExam)
        subs.mapNotNull { sub ->
            val list = examAttempts.filter { it.subjectId == sub.id }
            if (list.isEmpty()) null
            else sub.name to ((list.count { it.correct }.toFloat() / list.size.toFloat()) * 100f)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance Analytics") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${activeExam.title} Telemetry", style = MaterialTheme.typography.titleLarge)
                    ExamBadgePill(activeExam)
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    StatBox("Solved", "${examAttempts.size}", Modifier.weight(1f))
                    StatBox("Accuracy", if (examAttempts.isEmpty()) "—" else "${"%.1f".format(accuracy)}%", Modifier.weight(1f))
                    StatBox("Tests", "${examTests.size}", Modifier.weight(1f))
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    StatBox("Study Time", "${profile?.totalStudyMinutes ?: 0}m", Modifier.weight(1f))
                    StatBox("Streak", "${profile?.streakDays ?: 0}d", Modifier.weight(1f))
                    StatBox("XP", "${profile?.xp ?: 0}", Modifier.weight(1f))
                }
            }

            if (scoreSeries.isEmpty()) {
                item {
                    MastreyEmptyState(
                        icon = Icons.Filled.Analytics,
                        title = "No Test Score Trend Yet",
                        subtitle = "Complete your first ${activeExam.title} mock test to plot real score trends here. MASTREY PRO never displays fabricated metrics."
                    )
                }
            } else {
                item {
                    MastreyCard {
                        Text("Mock Test Score Trend (%)", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(10.dp))
                        ScoreTrendChart(scoresPercent = scoreSeries)
                    }
                }
            }

            if (subjectBars.isNotEmpty()) {
                item {
                    MastreyCard {
                        Text("Subject Accuracy Breakdown", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(10.dp))
                        SubjectBarChart(entries = subjectBars)
                    }
                }
            }

            if (weakTopics.isNotEmpty()) {
                item {
                    MastreyCard(accentBorder = WarningAmber) {
                        Text("Focus Chapters (< 70% Accuracy)", style = MaterialTheme.typography.titleMedium, color = WarningAmber)
                        Spacer(Modifier.height(6.dp))
                        weakTopics.forEach { t -> Text("• $t", style = MaterialTheme.typography.bodySmall) }
                        Spacer(Modifier.height(10.dp))
                        MastreyGradientButton(
                            text = "Practice Wrong Questions",
                            onClick = { viewModel.openPracticeFor(null, null, RepeatPolicy.WRONG_ONLY) }
                        )
                    }
                }
            }

            item {
                Text("Achievements & Milestones", style = MaterialTheme.typography.titleLarge)
            }

            items(achievements, key = { it.id }) { ach ->
                MastreyCard(accentBorder = if (ach.unlocked) SuccessGreen else null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = if (ach.unlocked) SuccessGreen else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ach.title, style = MaterialTheme.typography.titleSmall)
                            Text(ach.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(ach.progressText, style = MaterialTheme.typography.labelLarge, color = if (ach.unlocked) SuccessGreen else MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    MastreyCard(modifier = modifier) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyPlannerScreen(
    viewModel: MastreyViewModel
) {
    BackHandler { viewModel.navigateBack() }

    val activeExam by viewModel.activeExam.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val allTasks by viewModel.studyTasks.collectAsStateWithLifecycle()

    val examTasks = remember(allTasks, activeExam) { allTasks.filter { it.examId == activeExam.id } }
    val groupedByDate = remember(examTasks) { examTasks.groupBy { it.scheduledDate } }

    var dailyHours by remember(profile) { mutableFloatStateOf(((profile?.dailyStudyMinutes ?: 120) / 60f).coerceIn(1f, 10f)) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Study Planner") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                modifier = Modifier.testTag("planner_add_task_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Study Task")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MastreyCard(accentBorder = VividViolet) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("7-Day ${activeExam.title} Schedule", style = MaterialTheme.typography.titleMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Pause", style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.width(6.dp))
                            Switch(
                                checked = settings.studyPlanPaused,
                                onCheckedChange = { viewModel.toggleStudyPlanPaused(it) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Daily Study Target: ${"%.1f".format(dailyHours)} hrs (${(dailyHours * 60).toInt()} min)", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = dailyHours,
                        onValueChange = { dailyHours = it },
                        valueRange = 1f..10f,
                        steps = 17
                    )
                    MastreyGradientButton(
                        text = "Regenerate Smart 7-Day Plan",
                        icon = Icons.Filled.CalendarMonth,
                        onClick = { viewModel.regenerateStudyPlan((dailyHours * 60).toInt()) },
                        modifier = Modifier.testTag("regenerate_study_plan_button")
                    )
                }
            }

            if (examTasks.isEmpty()) {
                item {
                    MastreyEmptyState(
                        icon = Icons.Filled.CalendarMonth,
                        title = "No Study Plan Tasks",
                        subtitle = "Tap Regenerate above to build a balanced 7-day plan across your ${activeExam.title} subjects and weak topics."
                    )
                }
            } else {
                groupedByDate.forEach { (date, tasks) ->
                    item(key = "header_$date") {
                        Text(date, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    items(tasks, key = { it.id }) { task ->
                        MastreyCard(onClick = { viewModel.toggleStudyTask(task) }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = if (task.completed) "Completed" else "Pending",
                                    tint = if (task.completed) SuccessGreen else MaterialTheme.colorScheme.outline
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(task.title, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        "${task.subjectName} • ${task.durationMinutes} min • ${task.taskType}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteStudyTask(task.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete Task", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }

    if (showAddTaskDialog) {
        var title by remember { mutableStateOf("") }
        var subject by remember { mutableStateOf(SyllabusCatalog.subjectsFor(activeExam).firstOrNull()?.name ?: "General") }
        var minutesText by remember { mutableStateOf("45") }
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Add Custom Study Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = minutesText, onValueChange = { minutesText = it.filter { c -> c.isDigit() } }, label = { Text("Duration (minutes)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addCustomStudyTask(title, subject, minutesText.toIntOrNull()?.coerceIn(10, 300) ?: 45, "CUSTOM")
                            showAddTaskDialog = false
                        }
                    },
                    enabled = title.isNotBlank()
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) { Text("Cancel") }
            }
        )
    }
}
