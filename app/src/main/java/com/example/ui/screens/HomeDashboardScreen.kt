package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.content.SyllabusCatalog
import com.example.data.model.ExamSystem
import com.example.data.repository.RepeatPolicy
import com.example.ui.AppDestination
import com.example.ui.MastreyViewModel
import com.example.ui.PrimaryTab
import com.example.ui.components.ExamBadgePill
import com.example.ui.components.MastreyCard
import com.example.ui.components.StudentAvatarColors
import com.example.ui.components.examAccentColor
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.VividViolet
import com.example.ui.theme.WarningAmber
import java.util.Calendar
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    viewModel: MastreyViewModel
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val activeExam by viewModel.activeExam.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val attempts by viewModel.attempts.collectAsStateWithLifecycle()
    val testResults by viewModel.testResults.collectAsStateWithLifecycle()
    val studyTasks by viewModel.studyTasks.collectAsStateWithLifecycle()
    val revisions by viewModel.revisions.collectAsStateWithLifecycle()

    var showExamSwitcher by remember { mutableStateOf(false) }

    val examAttempts = remember(attempts, activeExam) { attempts.filter { it.examId == activeExam.id } }
    val examTasks = remember(studyTasks, activeExam) { studyTasks.filter { it.examId == activeExam.id } }
    val todayTasks = remember(examTasks) { examTasks.take(3) }
    val weakTopics = remember(attempts, activeExam) { viewModel.computeWeakTopicNames() }
    val dueRevisionsCount = remember(revisions, activeExam) {
        val now = System.currentTimeMillis()
        revisions.count { it.examId == activeExam.id && it.nextReviewEpochMs <= now }
    }
    val lastChapter = remember(profile, activeExam) { viewModel.getLastOpenedChapter() }

    val displayName = profile?.displayName?.ifBlank { "Student" } ?: "Student"
    val targetYear = profile?.targetYear ?: 2027
    val dailyGoalMinutes = (profile?.dailyStudyMinutes ?: 120).coerceAtLeast(15)
    val totalMinutes = profile?.totalStudyMinutes ?: 0
    val dailyProgressFraction = ((totalMinutes % dailyGoalMinutes).toFloat() / dailyGoalMinutes.toFloat())
        .let { if (totalMinutes >= dailyGoalMinutes && it == 0f) 1f else it.coerceIn(0f, 1f) }
    val xp = profile?.xp ?: 0
    val level = (xp / 250) + 1
    val streakDays = profile?.streakDays ?: 0
    val avatarColor = StudentAvatarColors[(profile?.avatarIndex ?: 0).coerceIn(StudentAvatarColors.indices)]

    val daysRemaining = remember(targetYear) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(targetYear, Calendar.APRIL, 15, 9, 0, 0)
        }
        val diff = target.timeInMillis - now.timeInMillis
        if (diff > 0) TimeUnit.MILLISECONDS.toDays(diff).toInt() else 0
    }

    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 4..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    val examAccent = examAccentColor(activeExam)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Row: Avatar, Greeting, Search, Settings
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(avatarColor, VividViolet)))
                            .clickable { viewModel.navigateTo(AppDestination.ProfileSetup) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "$greeting,",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppDestination.GlobalSearch) },
                        modifier = Modifier.testTag("home_search_button")
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = "Global Search")
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(AppDestination.Settings) },
                        modifier = Modifier.testTag("home_settings_button")
                    ) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            }
        }

        // Active Exam Switcher + Cloud/Guest Status + Streak/XP Strip
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExamBadgePill(
                        exam = activeExam,
                        onClick = { showExamSwitcher = true },
                        modifier = Modifier.testTag("home_exam_switcher")
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { showExamSwitcher = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.SwapHoriz, contentDescription = "Switch Exam", modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Switch", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (currentUser != null) SuccessGreen.copy(alpha = 0.14f) else WarningAmber.copy(alpha = 0.14f),
                    modifier = Modifier.clickable { viewModel.navigateTo(AppDestination.Settings) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (currentUser != null) Icons.Filled.CloudDone else Icons.Filled.PersonOutline,
                            contentDescription = "Account Mode",
                            tint = if (currentUser != null) SuccessGreen else WarningAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (currentUser != null) "Cloud Synced" else "Guest Mode",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (currentUser != null) SuccessGreen else WarningAmber
                        )
                    }
                }
            }
        }

        // Futuristic Hero Telemetry Card (Countdown + Daily Goal Ring + Streak/XP)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, examAccent.copy(alpha = 0.45f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    examAccent.copy(alpha = 0.24f),
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.96f),
                                    VividViolet.copy(alpha = 0.18f)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${activeExam.title.uppercase()} • TARGET $targetYear",
                                style = MaterialTheme.typography.labelLarge,
                                color = examAccent
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (daysRemaining > 0) "$daysRemaining Days Remaining" else "Target Exam Window Active",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = activeExam.patternDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                MiniTelemetryPill(
                                    icon = Icons.Filled.LocalFireDepartment,
                                    label = "$streakDays d Streak",
                                    tint = WarningAmber
                                )
                                MiniTelemetryPill(
                                    icon = Icons.Rounded.AutoAwesome,
                                    label = "Lvl $level • $xp XP",
                                    tint = CyberCyan
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        // Daily Goal Ring
                        Box(
                            modifier = Modifier.size(84.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val stroke = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                drawArc(
                                    color = trackColor,
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = stroke
                                )
                                drawArc(
                                    brush = Brush.sweepGradient(listOf(examAccent, CyberCyan, VividViolet)),
                                    startAngle = -90f,
                                    sweepAngle = (dailyProgressFraction * 360f).coerceAtLeast(18f),
                                    useCenter = false,
                                    style = stroke
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${(dailyProgressFraction * 100).toInt()}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${dailyGoalMinutes}m",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Primary 6 Core Action Cards (2x3 Grid)
        item {
            Text("Core Workspace", style = MaterialTheme.typography.titleLarge)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    HomeActionTile(
                        title = "Continue Learning",
                        subtitle = lastChapter?.title ?: "Explore syllabus",
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        accent = ElectricBlue,
                        modifier = Modifier.weight(1f).testTag("home_continue_learning"),
                        onClick = {
                            if (lastChapter != null) viewModel.openChapter(lastChapter.id)
                            else viewModel.switchPrimaryTab(PrimaryTab.LEARN)
                        }
                    )
                    HomeActionTile(
                        title = "Practice Now",
                        subtitle = "${examAttempts.size} solved • Unseen first",
                        icon = Icons.Filled.Quiz,
                        accent = CyberCyan,
                        modifier = Modifier.weight(1f).testTag("home_practice_now"),
                        onClick = { viewModel.openPracticeFor(null, null, RepeatPolicy.UNSEEN_ONLY) }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    HomeActionTile(
                        title = "Generate Test",
                        subtitle = "${testResults.count { it.examId == activeExam.id }} tests taken",
                        icon = Icons.Filled.Assignment,
                        accent = VividViolet,
                        modifier = Modifier.weight(1f).testTag("home_generate_test"),
                        onClick = { viewModel.switchPrimaryTab(PrimaryTab.TESTS) }
                    )
                    HomeActionTile(
                        title = "Ask ${activeExam.group.aiTitle}",
                        subtitle = "Instant exam tutor",
                        icon = Icons.Filled.Psychology,
                        accent = examAccent,
                        modifier = Modifier.weight(1f).testTag("home_ask_ai"),
                        onClick = { viewModel.switchPrimaryTab(PrimaryTab.AI) }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    HomeActionTile(
                        title = "Revision Center",
                        subtitle = if (dueRevisionsCount > 0) "$dueRevisionsCount due today" else "Spaced repetition & cards",
                        icon = Icons.Filled.Refresh,
                        accent = WarningAmber,
                        modifier = Modifier.weight(1f).testTag("home_revision_center"),
                        onClick = { viewModel.navigateTo(AppDestination.RevisionCenter) }
                    )
                    HomeActionTile(
                        title = "Performance",
                        subtitle = "Real accuracy & trends",
                        icon = Icons.Filled.Analytics,
                        accent = SuccessGreen,
                        modifier = Modifier.weight(1f).testTag("home_performance"),
                        onClick = { viewModel.navigateTo(AppDestination.Analytics) }
                    )
                }
            }
        }

        // Weak Topics Alert (Strictly Driven by Real Attempts)
        if (weakTopics.isNotEmpty()) {
            item {
                MastreyCard(
                    accentBorder = WarningAmber,
                    onClick = { viewModel.openPracticeFor(null, null, RepeatPolicy.WRONG_ONLY) },
                    modifier = Modifier.testTag("home_weak_topics_card")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Weak Areas Detected", style = MaterialTheme.typography.titleMedium, color = WarningAmber)
                            Text(
                                text = weakTopics.joinToString(" • "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Practice weak areas")
                    }
                }
            }
        }

        // Quick Tools & Ecosystem Grid
        item {
            Text("Study Ecosystem", style = MaterialTheme.typography.titleLarge)
        }

        item {
            val shortcuts = listOf(
                Triple("Formulas", Icons.Filled.Functions, AppDestination.FormulaCenter),
                Triple("Planner", Icons.Filled.CalendarMonth, AppDestination.StudyPlanner),
                Triple("Flashcards", Icons.Filled.Style, AppDestination.RevisionCenter),
                Triple("Bookmarks", Icons.Filled.Bookmark, AppDestination.Bookmarks),
                Triple("Predictor", Icons.Filled.TrendingUp, AppDestination.RankPredictor),
                Triple("Search", Icons.Filled.Search, AppDestination.GlobalSearch),
                Triple("Settings", Icons.Filled.Settings, AppDestination.Settings),
                Triple("Developer", Icons.Filled.Code, AppDestination.DeveloperProfile)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                shortcuts.chunked(4).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { (label, icon, dest) ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { viewModel.navigateTo(dest) }
                                    .testTag("shortcut_${label.lowercase()}"),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                                    Spacer(Modifier.height(6.dp))
                                    Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Today's Study Tasks Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Today's Study Tasks", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Open Planner",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(AppDestination.StudyPlanner) }
                        .padding(6.dp)
                )
            }
        }

        if (todayTasks.isEmpty()) {
            item {
                MastreyCard(onClick = { viewModel.navigateTo(AppDestination.StudyPlanner) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Generate a smart 7-day ${activeExam.title} study plan tailored to your daily goal.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            items(todayTasks, key = { it.id }) { task ->
                MastreyCard(onClick = { viewModel.toggleStudyTask(task) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = if (task.completed) "Completed" else "Pending",
                            tint = if (task.completed) SuccessGreen else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.title, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${task.subjectName} • ${task.durationMinutes} min • ${task.taskType}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Subject Coverage Snapshot
        item {
            val subjects = remember(activeExam) { SyllabusCatalog.subjectsFor(activeExam) }
            MastreyCard {
                Text("${activeExam.title} Subjects", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
                subjects.forEach { subject ->
                    val chaptersCount = SyllabusCatalog.chaptersFor(activeExam).count { it.subjectId == subject.id }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.switchPrimaryTab(PrimaryTab.LEARN) }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(subject.name, style = MaterialTheme.typography.titleSmall)
                            Text(subject.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("$chaptersCount ch", style = MaterialTheme.typography.labelLarge, color = examAccent)
                    }
                }
            }
        }

        item { Spacer(Modifier.height(20.dp)) }
    }

    if (showExamSwitcher) {
        ModalBottomSheet(onDismissRequest = { showExamSwitcher = false }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Switch Active Exam System", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Each exam ecosystem is strictly isolated with its own subjects, syllabus, formulas, mock tests, and dedicated MASTREY AI profile.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ExamSystem.entries.forEach { exam ->
                    val selected = exam == activeExam
                    MastreyCard(
                        accentBorder = if (selected) examAccentColor(exam) else null,
                        onClick = {
                            viewModel.switchExamSystem(exam)
                            showExamSwitcher = false
                        },
                        modifier = Modifier.testTag("switch_exam_${exam.id.lowercase()}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(exam.title, style = MaterialTheme.typography.titleMedium, color = if (selected) examAccentColor(exam) else MaterialTheme.colorScheme.onSurface)
                                Text(exam.patternDescription, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (selected) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = "Selected", tint = examAccentColor(exam))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun MiniTelemetryPill(icon: ImageVector, label: String, tint: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = tint.copy(alpha = 0.16f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(5.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = tint)
        }
    }
}

@Composable
private fun HomeActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    MastreyCard(
        modifier = modifier,
        accentBorder = accent.copy(alpha = 0.55f),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = accent, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(2.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
