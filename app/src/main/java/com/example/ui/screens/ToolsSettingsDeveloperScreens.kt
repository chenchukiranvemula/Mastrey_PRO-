package com.example.ui.screens

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.R
import com.example.data.content.SyllabusCatalog
import com.example.data.model.AiResponseStyle
import com.example.data.model.BookmarkType
import com.example.data.predictors.RankPredictorEngine
import com.example.data.remote.launchInteractiveGoogleSignIn
import com.example.data.remote.signOutFromGoogle
import com.example.ui.AppDestination
import com.example.ui.MastreyViewModel
import com.example.ui.PrimaryTab
import com.example.ui.components.MastreyCard
import com.example.ui.components.MastreyEmptyState
import com.example.ui.components.MastreyGradientButton
import com.example.ui.components.ReportIssueDialog
import com.example.ui.theme.AccentColorPreference
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.InterfaceDensityPreference
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextSizePreference
import com.example.ui.theme.ThemeModePreference
import com.example.ui.theme.VividViolet
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    viewModel: MastreyViewModel
) {
    BackHandler { viewModel.navigateBack() }

    val activeExam by viewModel.activeExam.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val conversations by viewModel.allAiConversations.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()

    var query by remember { mutableStateOf("") }
    val q = query.trim().lowercase()

    val chapters = remember(activeExam, q) {
        if (q.isBlank()) emptyList()
        else SyllabusCatalog.chaptersFor(activeExam).filter {
            it.title.lowercase().contains(q) || it.introduction.lowercase().contains(q) ||
                it.topics.any { t -> t.title.lowercase().contains(q) }
        }
    }
    val formulas = remember(activeExam, q) {
        if (q.isBlank()) emptyList()
        else SyllabusCatalog.formulasFor(activeExam).filter {
            it.title.lowercase().contains(q) || it.expression.lowercase().contains(q) ||
                it.keywords.any { k -> k.lowercase().contains(q) }
        }
    }
    val questions = remember(activeExam, q) {
        if (q.isBlank()) emptyList()
        else viewModel.getAllExamQuestions().filter {
            it.questionText.lowercase().contains(q) || it.topicName.lowercase().contains(q)
        }.take(10)
    }
    val matchedNotes = remember(notes, q) {
        if (q.isBlank()) emptyList() else notes.filter { it.title.lowercase().contains(q) || it.content.lowercase().contains(q) }
    }
    val matchedBookmarks = remember(bookmarks, q) {
        if (q.isBlank()) emptyList() else bookmarks.filter { it.title.lowercase().contains(q) || it.snippet.lowercase().contains(q) }
    }
    val matchedChats = remember(conversations, q) {
        if (q.isBlank()) emptyList() else conversations.filter { it.title.lowercase().contains(q) || it.lastMessagePreview.lowercase().contains(q) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Global Search") },
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
                    onValueChange = {
                        query = it
                        if (it.trim().length >= 3) viewModel.saveSearchQuery(it.trim())
                    },
                    placeholder = { Text("Search chapters, formulas, questions, notes, AI chats...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("global_search_input")
                )
            }

            if (q.isBlank()) {
                if (recentSearches.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Recent Searches", style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = { viewModel.clearRecentSearches() }) { Text("Clear") }
                        }
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(recentSearches) { rec ->
                                FilterChip(selected = false, onClick = { query = rec.query }, label = { Text(rec.query) })
                            }
                        }
                    }
                } else {
                    item {
                        MastreyEmptyState(
                            icon = Icons.Filled.Search,
                            title = "Search Across ${activeExam.title}",
                            subtitle = "Instantly find formulas, chapters, practice questions, personal notes, bookmarks, and MASTREY AI conversations."
                        )
                    }
                }
            } else {
                val totalHits = chapters.size + formulas.size + questions.size + matchedNotes.size + matchedBookmarks.size + matchedChats.size
                if (totalHits == 0) {
                    item {
                        MastreyEmptyState(
                            icon = Icons.Filled.Search,
                            title = "No Matches Found",
                            subtitle = "Try another keyword or ask ${activeExam.group.aiTitle} directly.",
                            actionLabel = "Ask ${activeExam.group.aiTitle}",
                            onAction = { viewModel.openAiWithContext(null, null, "Explain $query for ${activeExam.title}") }
                        )
                    }
                }

                if (chapters.isNotEmpty()) {
                    item { Text("Chapters (${chapters.size})", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
                    items(chapters, key = { "ch_${it.id}" }) { ch ->
                        MastreyCard(onClick = { viewModel.openChapter(ch.id) }) {
                            Text(ch.title, style = MaterialTheme.typography.titleSmall)
                            Text(ch.introduction, style = MaterialTheme.typography.bodySmall, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (formulas.isNotEmpty()) {
                    item { Text("Formulas (${formulas.size})", style = MaterialTheme.typography.titleMedium, color = CyberCyan) }
                    items(formulas, key = { "fm_${it.id}" }) { fm ->
                        MastreyCard(onClick = { viewModel.navigateTo(AppDestination.FormulaCenter) }) {
                            Text("${fm.title} — ${fm.expression}", style = MaterialTheme.typography.titleSmall)
                            Text("${fm.subjectName} • ${fm.chapterName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (questions.isNotEmpty()) {
                    item { Text("Practice Questions (${questions.size})", style = MaterialTheme.typography.titleMedium, color = VividViolet) }
                    items(questions, key = { "q_${it.id}" }) { qItem ->
                        MastreyCard(onClick = { viewModel.openPracticeFor(qItem.subjectId, qItem.chapterId) }) {
                            Text(qItem.questionText, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                            Text("${qItem.subjectName} • ${qItem.topicName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (matchedNotes.isNotEmpty()) {
                    item { Text("Personal Notes (${matchedNotes.size})", style = MaterialTheme.typography.titleMedium) }
                    items(matchedNotes, key = { "n_${it.id}" }) { n ->
                        MastreyCard(onClick = { viewModel.navigateTo(AppDestination.RevisionCenter) }) {
                            Text(n.title, style = MaterialTheme.typography.titleSmall)
                            Text(n.content, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                        }
                    }
                }

                if (matchedBookmarks.isNotEmpty()) {
                    item { Text("Bookmarks (${matchedBookmarks.size})", style = MaterialTheme.typography.titleMedium) }
                    items(matchedBookmarks, key = { "b_${it.id}" }) { b ->
                        MastreyCard(onClick = { viewModel.navigateTo(AppDestination.Bookmarks) }) {
                            Text("[${b.itemType}] ${b.title}", style = MaterialTheme.typography.titleSmall)
                            Text(b.snippet, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                        }
                    }
                }

                if (matchedChats.isNotEmpty()) {
                    item { Text("AI Conversations (${matchedChats.size})", style = MaterialTheme.typography.titleMedium) }
                    items(matchedChats, key = { "c_${it.id}" }) { c ->
                        MastreyCard(onClick = {
                            viewModel.selectConversation(c.id)
                            viewModel.switchPrimaryTab(PrimaryTab.AI)
                        }) {
                            Text(c.title, style = MaterialTheme.typography.titleSmall)
                            Text(c.lastMessagePreview, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    viewModel: MastreyViewModel
) {
    BackHandler { viewModel.navigateBack() }

    val context = LocalContext.current
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    var selectedType by remember { mutableStateOf<BookmarkType?>(null) }
    var search by remember { mutableStateOf("") }

    val filtered = remember(bookmarks, selectedType, search) {
        val s = search.trim().lowercase()
        bookmarks.filter { b ->
            val typeMatch = selectedType == null || b.itemType == selectedType!!.name
            val qMatch = s.isBlank() || b.title.lowercase().contains(s) || b.snippet.lowercase().contains(s)
            typeMatch && qMatch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved & Bookmarks") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val text = viewModel.buildExportSummaryText()
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Saved Items"))
                        }
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share Saved Bookmarks")
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
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Filter saved items...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(selected = selectedType == null, onClick = { selectedType = null }, label = { Text("All") })
                    }
                    items(BookmarkType.entries) { t ->
                        FilterChip(selected = selectedType == t, onClick = { selectedType = t }, label = { Text(t.label) })
                    }
                }
            }
            if (filtered.isEmpty()) {
                item {
                    MastreyEmptyState(
                        icon = Icons.Filled.Bookmark,
                        title = "No Bookmarks Saved",
                        subtitle = "Bookmark formulas, chapters, questions, flashcards, or AI answers anywhere in the app to collect them here."
                    )
                }
            } else {
                items(filtered, key = { it.id }) { b ->
                    MastreyCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(b.itemType, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            IconButton(onClick = { viewModel.removeBookmark(b.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove Bookmark", modifier = Modifier.size(18.dp))
                            }
                        }
                        Text(b.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(b.snippet, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankPredictorScreen(
    viewModel: MastreyViewModel
) {
    BackHandler { viewModel.navigateBack() }

    var selectedTab by remember { mutableIntStateOf(0) }
    var marksInput by remember(selectedTab) {
        mutableStateOf(when (selectedTab) { 0 -> "180"; 1 -> "150"; else -> "640" })
    }
    var category by remember { mutableStateOf("GENERAL") }
    var instFilter by remember { mutableStateOf("ALL") }
    var branchKeyword by remember { mutableStateOf("") }

    val result = remember(selectedTab, marksInput, category, instFilter, branchKeyword) {
        val m = marksInput.toIntOrNull() ?: 0
        when (selectedTab) {
            0 -> RankPredictorEngine.predictJeeMain(m, category, instFilter, branchKeyword)
            1 -> RankPredictorEngine.predictJeeAdvanced(m, category)
            else -> RankPredictorEngine.predictNeetUg(m, category)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rank & College Predictor") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("JEE Main") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("JEE Adv") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("NEET UG") })
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    MastreyCard {
                        val maxMarks = when (selectedTab) { 0 -> 300; 1 -> 360; else -> 720 }
                        OutlinedTextField(
                            value = marksInput,
                            onValueChange = { marksInput = it.filter { c -> c.isDigit() || c == '-' } },
                            label = { Text("Expected Score (out of $maxMarks)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("predictor_marks_input")
                        )
                        Spacer(Modifier.height(10.dp))
                        Text("Category", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(listOf("GENERAL", "GEN-EWS", "OBC-NCL", "SC", "ST")) { cat ->
                                FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
                            }
                        }
                        if (selectedTab == 0) {
                            Spacer(Modifier.height(10.dp))
                            Text("Institution Type", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("ALL", "NIT", "IIIT", "GFTI").forEach { type ->
                                    FilterChip(selected = instFilter == type, onClick = { instFilter = type }, label = { Text(type) })
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = branchKeyword,
                                onValueChange = { branchKeyword = it },
                                label = { Text("Filter branch (e.g. CSE, ECE, Mech)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item {
                    MastreyCard(accentBorder = ElectricBlue) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = CyberCyan)
                            Spacer(Modifier.width(8.dp))
                            Text(result.examTitle, style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(Modifier.height(8.dp))
                        result.estimatedPercentile?.let {
                            Text("Estimated Percentile: $it", style = MaterialTheme.typography.titleMedium, color = CyberCyan)
                        }
                        Text("Indicative Rank Range: ${result.estimatedRankRange}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(6.dp))
                        Text(result.eligibilitySummary, style = MaterialTheme.typography.bodySmall)
                    }
                }

                if (result.possibleInstitutions.isNotEmpty()) {
                    item { Text("Indicative Historical Options", style = MaterialTheme.typography.titleMedium) }
                    items(result.possibleInstitutions) { inst ->
                        MastreyCard {
                            Text("${inst.name} (${inst.type})", style = MaterialTheme.typography.titleSmall)
                            Text("Branches: ${inst.branch}", style = MaterialTheme.typography.bodySmall)
                            Text(inst.indicativeBand, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = WarningAmber.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(result.disclaimer, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MastreyViewModel
) {
    BackHandler { viewModel.navigateBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showBugDialog by remember { mutableStateOf(false) }
    var authLoading by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.updateNotifications(enabled = granted)
        if (granted) {
            sendTestStudyNotification(context)
            viewModel.showMessage("Study notifications enabled!")
        } else {
            viewModel.showMessage("Notification permission denied. You can enable it later in Android Settings.")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Account & Cloud Sync
            item {
                MastreyCard(accentBorder = if (currentUser != null) SuccessGreen else WarningAmber) {
                    Text("Account & Cloud Sync", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    if (currentUser != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CloudDone, contentDescription = null, tint = SuccessGreen)
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(currentUser?.displayName ?: profile?.displayName ?: "Signed-In Student", style = MaterialTheme.typography.titleMedium)
                                Text(currentUser?.email ?: "Google Account Connected", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { viewModel.navigateTo(AppDestination.ProfileSetup) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Edit Profile")
                            }
                            OutlinedButton(
                                onClick = {
                                    signOutFromGoogle(credentialManager, scope) {
                                        viewModel.showMessage("Signed out. Switched to local Guest Mode.")
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("settings_signout_button")
                            ) {
                                Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Sign Out")
                            }
                        }
                    } else {
                        Text("Guest Mode Active (Local Device Only)", style = MaterialTheme.typography.titleSmall, color = WarningAmber)
                        Text(
                            "Sign in free with Google to sync your progress, notes, mock tests, and bookmarks to Firebase Cloud and migrate your guest data.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        MastreyGradientButton(
                            text = if (authLoading) "Connecting Google..." else "Upgrade & Sync with Google",
                            icon = Icons.Filled.Login,
                            enabled = !authLoading,
                            onClick = {
                                authLoading = true
                                launchInteractiveGoogleSignIn(
                                    context = context,
                                    credentialManager = credentialManager,
                                    scope = scope,
                                    onAuthSuccess = { user ->
                                        authLoading = false
                                        viewModel.onGoogleSignInSuccess(user)
                                    },
                                    onAuthCancelled = {
                                        authLoading = false
                                        viewModel.showMessage("Google Sign-In cancelled")
                                    },
                                    onAuthError = { err ->
                                        authLoading = false
                                        viewModel.showMessage(err)
                                    }
                                )
                            },
                            modifier = Modifier.testTag("settings_google_upgrade_button")
                        )
                        Spacer(Modifier.height(6.dp))
                        TextButton(onClick = { viewModel.navigateTo(AppDestination.ProfileSetup) }) {
                            Text("Edit Local Student Profile")
                        }
                    }
                }
            }

            // 2. Appearance
            item {
                MastreyCard {
                    Text("Appearance", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("Theme Mode", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeModePreference.entries.forEach { mode ->
                            FilterChip(
                                selected = settings.themeMode == mode,
                                onClick = { viewModel.updateThemeMode(mode) },
                                label = { Text(mode.label) },
                                modifier = Modifier.testTag("theme_${mode.name.lowercase()}")
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Accent Color", style = MaterialTheme.typography.titleSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(AccentColorPreference.entries) { accent ->
                            FilterChip(
                                selected = settings.accentColor == accent,
                                onClick = { viewModel.updateAccent(accent) },
                                label = { Text(accent.label) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Interface Density", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InterfaceDensityPreference.entries.forEach { d ->
                            FilterChip(
                                selected = settings.interfaceDensity == d,
                                onClick = { viewModel.updateDensity(d) },
                                label = { Text(d.label) }
                            )
                        }
                    }
                }
            }

            // 3. Reading & Typography
            item {
                MastreyCard {
                    Text("Reading & Typography", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("Text Size", style = MaterialTheme.typography.titleSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(TextSizePreference.entries) { sz ->
                            FilterChip(
                                selected = settings.textSize == sz,
                                onClick = { viewModel.updateTextSize(sz) },
                                label = { Text(sz.label) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Line Spacing (${"%.2f".format(settings.lineSpacingMultiplier)}x)", style = MaterialTheme.typography.titleSmall)
                    Slider(
                        value = settings.lineSpacingMultiplier,
                        onValueChange = { viewModel.updateLineSpacing(it) },
                        valueRange = 0.9f..1.45f
                    )
                    SettingsToggleRow("Monospace Formula Highlight", settings.mathMonospaceHighlight) { viewModel.updateMathHighlight(it) }
                    SettingsToggleRow("Warm Reading Comfort", settings.readingComfortWarm) { viewModel.updateReadingComfort(it) }
                }
            }

            // 4. Accessibility
            item {
                MastreyCard {
                    Text("Accessibility", style = MaterialTheme.typography.titleLarge)
                    SettingsToggleRow("High Contrast Mode", settings.highContrast) { viewModel.updateHighContrast(it) }
                    SettingsToggleRow("Reduced Motion", settings.reducedMotion) { viewModel.updateReducedMotion(it) }
                    SettingsToggleRow("Larger Controls (56dp Buttons)", settings.largerControls) { viewModel.updateLargerControls(it) }
                }
            }

            // 5. Notifications (Android 13+ POST_NOTIFICATIONS runtime compliant)
            item {
                MastreyCard {
                    Text("Notifications & Study Reminders", style = MaterialTheme.typography.titleLarge)
                    SettingsToggleRow("Enable Study Notifications", settings.notificationsEnabled) { enable ->
                        if (!enable) {
                            viewModel.updateNotifications(enabled = false)
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.updateNotifications(enabled = true)
                            sendTestStudyNotification(context)
                            viewModel.showMessage("Study notifications enabled")
                        }
                    }
                    if (settings.notificationsEnabled) {
                        Text("Daily Reminder Hour: ${settings.reminderHour}:00", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = settings.reminderHour.toFloat(),
                            onValueChange = { viewModel.updateNotifications(reminderHour = it.toInt()) },
                            valueRange = 5f..23f,
                            steps = 17
                        )
                        SettingsToggleRow("Daily Study Reminders", settings.studyReminders) { viewModel.updateNotifications(studyReminders = it) }
                        SettingsToggleRow("Spaced Revision Alerts", settings.revisionAlerts) { viewModel.updateNotifications(revisionAlerts = it) }
                        SettingsToggleRow("Mock Test Alerts", settings.testAlerts) { viewModel.updateNotifications(testAlerts = it) }
                        OutlinedButton(
                            onClick = {
                                sendTestStudyNotification(context)
                                viewModel.showMessage("Test study reminder sent")
                            }
                        ) {
                            Icon(Icons.Filled.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Send Test Reminder")
                        }
                    }
                }
            }

            // 6. AI Tutor Preferences
            item {
                MastreyCard {
                    Text("MASTREY AI Preferences", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("Default Explanation Style", style = MaterialTheme.typography.titleSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(AiResponseStyle.entries) { style ->
                            FilterChip(
                                selected = settings.aiExplanationStyle == style,
                                onClick = { viewModel.updateAiSettings(style = style) },
                                label = { Text(style.label) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Response Length", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Concise", "Balanced", "Comprehensive").forEach { len ->
                            FilterChip(
                                selected = settings.aiResponseLength == len,
                                onClick = { viewModel.updateAiSettings(length = len) },
                                label = { Text(len) }
                            )
                        }
                    }
                    SettingsToggleRow("Weak-Topic Personalization", settings.aiPersonalizationEnabled) {
                        viewModel.updateAiSettings(personalization = it)
                    }
                }
            }

            // 7. Data & Privacy
            item {
                MastreyCard {
                    Text("Data & Privacy", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val summary = viewModel.buildExportSummaryText()
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, summary)
                            }
                            context.startActivity(Intent.createChooser(intent, "Export MASTREY PRO Data"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Export Study Progress & Bookmarks")
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { viewModel.clearLocalCache() }, modifier = Modifier.weight(1f)) {
                            Text("Clear Cache")
                        }
                        OutlinedButton(onClick = { viewModel.clearAiHistory() }, modifier = Modifier.weight(1f)) {
                            Text("Clear AI Chats")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { showDeleteAccountDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Delete Account & Clear Local Data", color = Color.White)
                    }
                }
            }

            // 8. App & Developer
            item {
                MastreyCard {
                    Text("About & Support", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    Text("MASTREY PRO • Version ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { showFeedbackDialog = true }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.Feedback, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Feedback")
                        }
                        OutlinedButton(onClick = { showBugDialog = true }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Report Bug")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    MastreyGradientButton(
                        text = "About Developer",
                        icon = Icons.Filled.Code,
                        onClick = { viewModel.navigateTo(AppDestination.DeveloperProfile) },
                        modifier = Modifier.testTag("settings_about_developer_button")
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account & Data?") },
            text = { Text("This clears all saved local progress, notes, bookmarks, AI chats, and signs out your session. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        signOutFromGoogle(credentialManager, scope) {
                            viewModel.deleteAccount {}
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) { Text("Delete Everything", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showFeedbackDialog) {
        ReportIssueDialog(
            title = "Send Feedback",
            placeholder = "Share suggestions to improve MASTREY PRO...",
            onDismiss = { showFeedbackDialog = false },
            onSubmit = { msg -> viewModel.submitContentReport("FEEDBACK", "app", msg) }
        )
    }

    if (showBugDialog) {
        ReportIssueDialog(
            title = "Report a Bug",
            placeholder = "Describe the bug and steps to reproduce...",
            onDismiss = { showBugDialog = false },
            onSubmit = { msg -> viewModel.submitContentReport("BUG_REPORT", "app", msg) }
        )
    }
}

@Composable
private fun SettingsToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun sendTestStudyNotification(context: Context) {
    val channelId = "mastrey_study_reminders"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "MASTREY PRO Study Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("MASTREY PRO Study Reminder")
        .setContentText("Your daily practice & spaced revision session is ready. Keep your streak alive!")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()
    NotificationManagerCompat.from(context).notify(1001, notification)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperProfileScreen(
    viewModel: MastreyViewModel
) {
    BackHandler { viewModel.navigateBack() }

    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showBugDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Developer") },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.2.dp, ElectricBlue.copy(alpha = 0.55f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        ElectricBlue.copy(alpha = 0.24f),
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        VividViolet.copy(alpha = 0.20f)
                                    )
                                )
                            )
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(82.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(ElectricBlue, VividViolet, CyberCyan))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "VC",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "V. CHENCHUKIRAN",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "DEVSECOPS & CLOUD ENGINEERING",
                            style = MaterialTheme.typography.labelLarge,
                            color = CyberCyan,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = "Founder • Developer • Creator of MASTREY PRO",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            item {
                MastreyCard(accentBorder = CyberCyan) {
                    Text("Vision", style = MaterialTheme.typography.titleMedium, color = CyberCyan)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "\"Technology should not create barriers to education. MASTREY PRO is built with the vision of bringing powerful learning tools, practice systems and AI assistance together in one accessible platform for every student.\"",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            item {
                MastreyCard {
                    Text("Platform Architecture & Tech Stack", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    val stack = listOf("Kotlin", "Jetpack Compose", "Material 3", "Firebase AI Logic", "Cloud Firestore", "Room SQLite", "DataStore", "Credential Manager")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(stack) { item ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            ) {
                                Text(item, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "MASTREY PRO • Version ${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { showFeedbackDialog = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Feedback, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Send Feedback")
                    }
                    OutlinedButton(onClick = { showBugDialog = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Report Bug")
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }

    if (showFeedbackDialog) {
        ReportIssueDialog(
            title = "Send App Feedback",
            onDismiss = { showFeedbackDialog = false },
            onSubmit = { msg -> viewModel.submitContentReport("FEEDBACK", "developer_profile", msg) }
        )
    }

    if (showBugDialog) {
        ReportIssueDialog(
            title = "Report a Bug",
            onDismiss = { showBugDialog = false },
            onSubmit = { msg -> viewModel.submitContentReport("BUG_REPORT", "developer_profile", msg) }
        )
    }
}
