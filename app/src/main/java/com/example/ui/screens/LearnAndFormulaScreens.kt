package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.content.SyllabusCatalog
import com.example.data.model.BookmarkType
import com.example.data.model.ExplanationDepth
import com.example.data.model.FormulaItem
import com.example.data.model.RevisionConfidence
import com.example.data.model.TopicItem
import com.example.data.model.VerificationStatus
import com.example.data.repository.RepeatPolicy
import com.example.ui.AppDestination
import com.example.ui.MastreyViewModel
import com.example.ui.components.ExamBadgePill
import com.example.ui.components.FormulaExpressionBox
import com.example.ui.components.MastreyCard
import com.example.ui.components.MastreyEmptyState
import com.example.ui.components.MastreyGradientButton
import com.example.ui.components.VerificationBadge
import com.example.ui.components.examAccentColor
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun LearnScreen(
    viewModel: MastreyViewModel
) {
    val activeExam by viewModel.activeExam.collectAsStateWithLifecycle()
    val revisions by viewModel.revisions.collectAsStateWithLifecycle()
    val subjects = remember(activeExam) { SyllabusCatalog.subjectsFor(activeExam) }
    val allChapters = remember(activeExam) { SyllabusCatalog.chaptersFor(activeExam) }

    var selectedSubjectId by remember(activeExam) { mutableStateOf(subjects.firstOrNull()?.id.orEmpty()) }
    val chapters = remember(allChapters, selectedSubjectId) {
        allChapters.filter { it.subjectId == selectedSubjectId }.sortedBy { it.order }
    }
    val selectedSubject = subjects.firstOrNull { it.id == selectedSubjectId }

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
                Column(modifier = Modifier.weight(1f)) {
                    Text("Learning & Syllabus", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        activeExam.syllabusVersion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = { viewModel.navigateTo(AppDestination.FormulaCenter) },
                    modifier = Modifier.testTag("learn_open_formulas")
                ) {
                    Icon(Icons.Filled.Functions, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Formulas")
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(subjects, key = { it.id }) { subject ->
                    FilterChip(
                        selected = subject.id == selectedSubjectId,
                        onClick = { selectedSubjectId = subject.id },
                        label = { Text(subject.name) },
                        modifier = Modifier.testTag("learn_subject_${subject.id}")
                    )
                }
            }
        }

        if (selectedSubject != null) {
            item {
                MastreyCard(accentBorder = examAccentColor(activeExam)) {
                    Text(selectedSubject.name, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(selectedSubject.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Text(selectedSubject.weightageNote, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        items(chapters, key = { it.id }) { chapter ->
            val reviewedCount = revisions.count { it.chapterId == chapter.id }
            val totalTopics = chapter.topics.size
            MastreyCard(
                onClick = { viewModel.openChapter(chapter.id) },
                modifier = Modifier.testTag("chapter_card_${chapter.id}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Chapter ${chapter.order} • ${chapter.importanceTag}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "~${chapter.estimatedMinutes} min",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(chapter.title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text(
                    chapter.introduction,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VerificationBadge(chapter.verificationStatus)
                    Text(
                        text = if (totalTopics > 0) "$reviewedCount/$totalTopics topics reviewed" else "Syllabus Node",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item { Spacer(Modifier.height(20.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterDetailScreen(
    chapterId: String,
    viewModel: MastreyViewModel
) {
    BackHandler { viewModel.navigateBack() }

    val activeExam by viewModel.activeExam.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val revisions by viewModel.revisions.collectAsStateWithLifecycle()

    val chapter = remember(activeExam, chapterId) { SyllabusCatalog.findChapter(activeExam, chapterId) }
    val subject = remember(activeExam, chapter) {
        SyllabusCatalog.subjectsFor(activeExam).firstOrNull { it.id == chapter?.subjectId }
    }
    val chapterFormulas = remember(activeExam, chapterId) {
        SyllabusCatalog.formulasFor(activeExam).filter { it.chapterId == chapterId }
    }

    var depth by remember { mutableStateOf(ExplanationDepth.STANDARD) }
    var showNoteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(chapter?.title ?: "Chapter", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (chapter != null) {
                        val isSaved = bookmarks.any { it.targetId == chapter.id && it.itemType == BookmarkType.TOPIC.name }
                        IconButton(
                            onClick = {
                                viewModel.toggleBookmark(
                                    subjectId = chapter.subjectId,
                                    chapterId = chapter.id,
                                    type = BookmarkType.TOPIC,
                                    targetId = chapter.id,
                                    title = chapter.title,
                                    snippet = chapter.introduction
                                )
                            },
                            modifier = Modifier.testTag("chapter_bookmark_button")
                        ) {
                            Icon(
                                if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = "Bookmark Chapter",
                                tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = { showNoteDialog = true },
                            modifier = Modifier.testTag("chapter_add_note_button")
                        ) {
                            Icon(Icons.Filled.EditNote, contentDescription = "Add Personal Note")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (chapter == null) {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                MastreyEmptyState(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    title = "Chapter Not Found",
                    subtitle = "This chapter belongs to another exam workspace. Switch back to view it.",
                    actionLabel = "Go Back",
                    onAction = { viewModel.navigateBack() }
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                MastreyCard(accentBorder = examAccentColor(activeExam)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExamBadgePill(activeExam)
                        VerificationBadge(chapter.verificationStatus)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(chapter.title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(6.dp))
                    Text(chapter.introduction, style = MaterialTheme.typography.bodyMedium)
                    if (chapter.learningObjectives.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Text("Learning Objectives", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        chapter.learningObjectives.forEach { obj ->
                            Text("• $obj", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        MastreyGradientButton(
                            text = "Practice Chapter",
                            icon = Icons.Filled.Quiz,
                            onClick = { viewModel.openPracticeFor(chapter.subjectId, chapter.id, RepeatPolicy.UNSEEN_ONLY) },
                            modifier = Modifier.weight(1f).testTag("chapter_practice_button")
                        )
                        OutlinedButton(
                            onClick = {
                                viewModel.openAiWithContext(
                                    subjectName = subject?.name,
                                    chapterTitle = chapter.title,
                                    prefillPrompt = "Explain the key concepts, formulas, and exam traps in ${chapter.title} for ${activeExam.title}."
                                )
                            },
                            modifier = Modifier.height(48.dp).testTag("chapter_ask_ai_button")
                        ) {
                            Icon(Icons.Filled.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Ask AI")
                        }
                    }
                }
            }

            // Explanation Depth Selector
            item {
                Column {
                    Text("Explanation Depth", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExplanationDepth.entries.forEach { d ->
                            FilterChip(
                                selected = depth == d,
                                onClick = { depth = d },
                                label = { Text(d.label) },
                                modifier = Modifier.testTag("depth_${d.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            if (chapter.topics.isEmpty() || chapter.verificationStatus == VerificationStatus.AWAITING_VERIFIED) {
                item {
                    MastreyEmptyState(
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        title = "Awaiting Verified Content",
                        subtitle = "This syllabus chapter is registered in the ${activeExam.title} curriculum index and is awaiting full verified module notes. Use ${activeExam.group.aiTitle} for an interactive concept walkthrough right now.",
                        actionLabel = "Ask ${activeExam.group.aiTitle} to Teach This Chapter",
                        onAction = {
                            viewModel.openAiWithContext(
                                subjectName = subject?.name,
                                chapterTitle = chapter.title,
                                prefillPrompt = "Teach me ${chapter.title} (${activeExam.title}) from basics to exam-level with formulas and 2 solved examples."
                            )
                        }
                    )
                }
            } else {
                items(chapter.topics, key = { it.id }) { topic ->
                    val currentRev = revisions.firstOrNull { it.topicId == topic.id }
                    TopicModuleCard(
                        topic = topic,
                        depth = depth,
                        currentConfidence = currentRev?.confidence,
                        onMarkConfidence = { conf ->
                            viewModel.markTopicConfidence(chapter.subjectId, chapter.id, topic.id, topic.title, conf)
                        },
                        onAskAiTopic = {
                            viewModel.openAiWithContext(
                                subjectName = subject?.name,
                                chapterTitle = "${chapter.title} > ${topic.title}",
                                prefillPrompt = "Give me an exam-focused breakdown and solved numerical on ${topic.title} from ${chapter.title}."
                            )
                        }
                    )
                }
            }

            if (chapterFormulas.isNotEmpty()) {
                item {
                    Text("Chapter Formulas", style = MaterialTheme.typography.titleLarge)
                }
                items(chapterFormulas, key = { it.id }) { formula ->
                    MastreyCard {
                        Text(formula.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        FormulaExpressionBox(formula.expression, useMonoHighlight = settings.mathMonospaceHighlight)
                        Spacer(Modifier.height(6.dp))
                        Text(formula.explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }

    if (showNoteDialog && chapter != null) {
        var noteTitle by remember { mutableStateOf("${chapter.title} — Key Notes") }
        var noteBody by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Add Personal Note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Note Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteBody,
                        onValueChange = { noteBody = it },
                        label = { Text("Your notes, tricks, or doubts...") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteBody.isNotBlank()) {
                            viewModel.savePersonalNote(chapter.subjectId, chapter.id, noteTitle, noteBody, highlighted = false)
                            showNoteDialog = false
                        }
                    },
                    enabled = noteBody.isNotBlank()
                ) { Text("Save Note") }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun TopicModuleCard(
    topic: TopicItem,
    depth: ExplanationDepth,
    currentConfidence: String?,
    onMarkConfidence: (RevisionConfidence) -> Unit,
    onAskAiTopic: () -> Unit
) {
    MastreyCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(topic.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            IconButton(onClick = onAskAiTopic) {
                Icon(Icons.Filled.Psychology, contentDescription = "Ask AI about topic", tint = MaterialTheme.colorScheme.primary)
            }
        }
        if (topic.subtopics.isNotEmpty()) {
            Text(
                topic.subtopics.joinToString(" • "),
                style = MaterialTheme.typography.labelMedium,
                color = CyberCyan
            )
            Spacer(Modifier.height(8.dp))
        }

        val theoryText = when (depth) {
            ExplanationDepth.BEGINNER -> topic.beginnerTheory
            ExplanationDepth.STANDARD -> topic.standardTheory
            ExplanationDepth.ADVANCED -> topic.advancedTheory
        }
        Text(theoryText, style = MaterialTheme.typography.bodyMedium)

        if (topic.keyDefinitions.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text("Key Definitions", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            topic.keyDefinitions.forEach { def ->
                Text("• $def", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 3.dp))
            }
        }

        if (!topic.derivationNote.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Derivation / Concept Flow", style = MaterialTheme.typography.labelLarge, color = CyberCyan)
                    Spacer(Modifier.height(4.dp))
                    Text(topic.derivationNote, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        if (topic.solvedExamples.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text("Solved Examples", style = MaterialTheme.typography.titleSmall, color = SuccessGreen)
            topic.solvedExamples.forEachIndexed { idx, ex ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = SuccessGreen.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Example ${idx + 1}: ${ex.problem}", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(4.dp))
                        ex.steps.forEachIndexed { sIdx, step ->
                            Text("${sIdx + 1}. $step", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("Answer: ${ex.finalAnswer}", style = MaterialTheme.typography.labelLarge, color = SuccessGreen)
                    }
                }
            }
        }

        if (topic.shortcuts.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Shortcuts & Quick Methods", style = MaterialTheme.typography.titleSmall, color = CyberCyan)
            }
            topic.shortcuts.forEach { s -> Text("• $s", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp)) }
        }

        if (topic.commonMistakes.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Common Mistakes to Avoid", style = MaterialTheme.typography.titleSmall, color = WarningAmber)
            }
            topic.commonMistakes.forEach { m -> Text("• $m", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp)) }
        }

        if (topic.examTips.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text("Exam Tips", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
            topic.examTips.forEach { t -> Text("• $t", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp)) }
        }

        if (topic.revisionPoints.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text("Quick Revision Points", style = MaterialTheme.typography.titleSmall)
            topic.revisionPoints.forEach { r -> Text("✓ $r", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp)) }
        }

        Spacer(Modifier.height(12.dp))
        Text("Schedule Spaced Repetition:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RevisionConfidence.entries.forEach { conf ->
                FilterChip(
                    selected = currentConfidence == conf.name,
                    onClick = { onMarkConfidence(conf) },
                    label = { Text(conf.label) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulaCenterScreen(
    viewModel: MastreyViewModel
) {
    BackHandler { viewModel.navigateBack() }

    val context = LocalContext.current
    val activeExam by viewModel.activeExam.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()

    var query by remember { mutableStateOf("") }
    var searchAllExams by remember { mutableStateOf(false) }
    var selectedSubject by remember(activeExam, searchAllExams) { mutableStateOf("All") }

    val baseFormulas = remember(activeExam, searchAllExams) {
        if (searchAllExams) SyllabusCatalog.allFormulas() else SyllabusCatalog.formulasFor(activeExam)
    }
    val subjectNames = remember(baseFormulas) { listOf("All") + baseFormulas.map { it.subjectName }.distinct() }

    val filtered = remember(baseFormulas, query, selectedSubject) {
        val q = query.trim().lowercase()
        baseFormulas.filter { f ->
            val matchSubject = selectedSubject == "All" || f.subjectName == selectedSubject
            val matchQuery = q.isBlank() ||
                f.title.lowercase().contains(q) ||
                f.expression.lowercase().contains(q) ||
                f.topicName.lowercase().contains(q) ||
                f.chapterName.lowercase().contains(q) ||
                f.subjectName.lowercase().contains(q) ||
                f.keywords.any { it.lowercase().contains(q) } ||
                f.variables.keys.any { it.lowercase().contains(q) }
            matchSubject && matchQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Formula Center") },
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
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search formula, symbol, or topic (e.g. kinetic energy)...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("formula_search_input")
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !searchAllExams,
                        onClick = { searchAllExams = false },
                        label = { Text(activeExam.title) }
                    )
                    FilterChip(
                        selected = searchAllExams,
                        onClick = { searchAllExams = true },
                        label = { Text("All Exams") }
                    )
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(subjectNames) { name ->
                        FilterChip(
                            selected = selectedSubject == name,
                            onClick = { selectedSubject = name },
                            label = { Text(name) }
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    MastreyEmptyState(
                        icon = Icons.Filled.Functions,
                        title = "No Matching Formulas",
                        subtitle = "Try searching by symbol, chapter name, or enable 'All Exams' scope."
                    )
                }
            } else {
                items(filtered, key = { it.id }) { formula ->
                    val isBookmarked = bookmarks.any { it.targetId == formula.id && it.itemType == BookmarkType.FORMULA.name }
                    FormulaCardItem(
                        formula = formula,
                        useMono = settings.mathMonospaceHighlight,
                        isBookmarked = isBookmarked,
                        onCopy = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText(formula.title, "${formula.title}: ${formula.expression}"))
                            viewModel.showMessage("Formula copied to clipboard")
                        },
                        onShare = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "${formula.title} (${formula.subjectName})\n${formula.expression}\nUnits: ${formula.units}\nExample: ${formula.example}"
                                )
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Formula"))
                        },
                        onToggleBookmark = {
                            viewModel.toggleBookmark(
                                subjectId = formula.subjectId,
                                chapterId = formula.chapterId,
                                type = BookmarkType.FORMULA,
                                targetId = formula.id,
                                title = formula.title,
                                snippet = formula.expression
                            )
                        },
                        onOpenChapter = { viewModel.openChapter(formula.chapterId) },
                        onPractice = { viewModel.openPracticeFor(formula.subjectId, formula.chapterId) }
                    )
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun FormulaCardItem(
    formula: FormulaItem,
    useMono: Boolean,
    isBookmarked: Boolean,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenChapter: () -> Unit,
    onPractice: () -> Unit
) {
    MastreyCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${formula.subjectName} • ${formula.chapterName}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Surface(shape = RoundedCornerShape(50), color = CyberCyan.copy(alpha = 0.14f)) {
                Text(
                    formula.importanceLevel,
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(formula.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        FormulaExpressionBox(formula.expression, useMonoHighlight = useMono)
        Spacer(Modifier.height(8.dp))
        Text(formula.explanation, style = MaterialTheme.typography.bodyMedium)
        if (formula.variables.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            formula.variables.forEach { (sym, desc) ->
                Text("• $sym : $desc", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text("Units: ${formula.units}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text("Conditions: ${formula.conditionsOfUse}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Example: ${formula.example}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                IconButton(onClick = onCopy) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Formula", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Filled.Share, contentDescription = "Share Formula", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onToggleBookmark) {
                    Icon(
                        if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = "Bookmark Formula",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onOpenChapter) { Text("Chapter") }
                TextButton(onClick = onPractice) { Text("Practice") }
            }
        }
    }
}
