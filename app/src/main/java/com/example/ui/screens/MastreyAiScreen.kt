package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AiMessageEntity
import com.example.data.model.AiResponseStyle
import com.example.data.model.BookmarkType
import com.example.data.model.ExamGroup
import com.example.data.repository.MastreyRepository
import com.example.ui.AiStreamState
import com.example.ui.MastreyViewModel
import com.example.ui.components.MastreyCard
import com.example.ui.components.ReportIssueDialog
import com.example.ui.components.examAccentColor
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.VividViolet
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MastreyAiScreen(
    viewModel: MastreyViewModel
) {
    val context = LocalContext.current
    val activeExam by viewModel.activeExam.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val messages by viewModel.activeMessages.collectAsStateWithLifecycle()
    val conversations by viewModel.aiConversations.collectAsStateWithLifecycle()
    val activeConvId by viewModel.activeConversationId.collectAsStateWithLifecycle()
    val streamState by viewModel.aiStreamState.collectAsStateWithLifecycle()
    val draftInput by viewModel.aiDraftInput.collectAsStateWithLifecycle()
    val contextSubject by viewModel.aiContextSubject.collectAsStateWithLifecycle()
    val contextChapter by viewModel.aiContextChapter.collectAsStateWithLifecycle()

    var showHistorySheet by remember { mutableStateOf(false) }
    var reportMessageId by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size, streamState) {
        val target = (messages.size + 1).coerceAtLeast(0)
        if (target > 0) listState.animateScrollToItem(target)
    }

    val accent = examAccentColor(activeExam)
    val quickPrompts = remember(activeExam) {
        when (activeExam.group) {
            ExamGroup.JEE -> listOf(
                "Solve a Work-Energy Theorem numerical step-by-step",
                "Explain SN1 vs SN2 reaction mechanism traps for JEE",
                "Give high-yield Definite Integration shortcuts",
                "Create 5 rapid flashcards on Electrostatics"
            )
            ExamGroup.NEET -> listOf(
                "Explain C3 vs C4 photosynthesis pathways from NCERT",
                "Summarize Human Physiology high-yield NEET lines",
                "Give a step-by-step Physics optics numerical for NEET",
                "Create 5 NCERT Botany flashcards on Genetics"
            )
            ExamGroup.CA -> listOf(
                "Explain Bank Reconciliation Statement adjustments with example",
                "Summarize Indian Contract Act essential elements & case laws",
                "Explain Elasticity of Demand types & numerical formulas",
                "Create 5 Time Value of Money practice flashcards"
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // Dedicated Exam AI Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(accent, VividViolet))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Psychology, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(activeExam.group.aiTitle, style = MaterialTheme.typography.titleMedium, color = accent)
                            Text(
                                activeExam.group.aiSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                    Row {
                        IconButton(onClick = { showHistorySheet = true }, modifier = Modifier.testTag("ai_history_button")) {
                            Icon(Icons.Filled.History, contentDescription = "AI Chat History")
                        }
                        IconButton(onClick = { viewModel.startNewAiConversation() }, modifier = Modifier.testTag("ai_new_chat_button")) {
                            Icon(Icons.Filled.AddComment, contentDescription = "New Chat")
                        }
                        if (activeConvId != null) {
                            IconButton(onClick = { viewModel.clearActiveConversation() }) {
                                Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear Chat")
                            }
                        }
                    }
                }

                if (currentUser == null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Guest Preview: ${settings.guestAiUsageCount}/${MastreyRepository.GUEST_AI_LIMIT} prompts used • Sign in free with Google in Settings for unlimited cloud sync.",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarningAmber
                    )
                }

                if (!contextChapter.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Active Context: ${contextSubject.orEmpty()} • $contextChapter",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan
                    )
                }

                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AiResponseStyle.entries) { style ->
                        FilterChip(
                            selected = settings.aiExplanationStyle == style,
                            onClick = { viewModel.updateAiSettings(style = style) },
                            label = { Text(style.label, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }
            }
        }

        // Chat Stream Area
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (messages.isEmpty() && streamState is AiStreamState.Idle) {
                item {
                    MastreyCard(accentBorder = accent) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = accent)
                            Spacer(Modifier.width(8.dp))
                            Text("Welcome to ${activeExam.group.aiTitle}", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Ask any ${activeExam.title} conceptual doubt, numerical problem, derivation, or revision summary. Strictly tuned for ${activeExam.title}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        quickPrompts.forEach { prompt ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.sendAiMessage(prompt) },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    prompt,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
                AiChatBubble(
                    message = msg,
                    accent = accent,
                    onCopy = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("MASTREY AI", msg.content))
                        viewModel.showMessage("AI response copied")
                    },
                    onRegenerate = {
                        val lastUser = messages.lastOrNull { it.role == "USER" }?.content
                        if (!lastUser.isNullOrBlank()) viewModel.sendAiMessage(lastUser, isRegenerate = true)
                    },
                    onBookmark = {
                        viewModel.toggleBookmark(
                            subjectId = activeExam.id,
                            chapterId = contextChapter ?: activeExam.group.name,
                            type = BookmarkType.AI_ANSWER,
                            targetId = msg.id,
                            title = "${activeExam.group.aiTitle} Answer",
                            snippet = msg.content.take(220)
                        )
                    },
                    onReport = { reportMessageId = msg.id }
                )
            }

            when (val st = streamState) {
                is AiStreamState.Loading -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, accent.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = accent)
                                Spacer(Modifier.width(10.dp))
                                Text("${activeExam.group.aiTitle} is reasoning...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                is AiStreamState.Streaming -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, accent.copy(alpha = 0.55f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(activeExam.group.aiTitle, style = MaterialTheme.typography.labelMedium, color = accent)
                                Spacer(Modifier.height(4.dp))
                                Text(st.partialText, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                is AiStreamState.Error -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = ErrorRed.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("AI Request Interrupted", style = MaterialTheme.typography.titleSmall, color = ErrorRed)
                                Spacer(Modifier.height(4.dp))
                                Text(st.message, style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.height(8.dp))
                                Button(onClick = { viewModel.sendAiMessage(st.preservedPrompt) }) {
                                    Text("Retry Saved Prompt")
                                }
                            }
                        }
                    }
                }
                AiStreamState.Idle -> Unit
            }
        }

        // Bottom Input Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = draftInput,
                    onValueChange = { viewModel.updateAiDraft(it) },
                    placeholder = { Text("Ask ${activeExam.group.aiTitle} anything...") },
                    maxLines = 4,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).testTag("ai_prompt_input")
                )
                Spacer(Modifier.width(8.dp))
                val busy = streamState is AiStreamState.Loading || streamState is AiStreamState.Streaming
                IconButton(
                    onClick = { viewModel.sendAiMessage(draftInput) },
                    enabled = draftInput.isNotBlank() && !busy,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (draftInput.isNotBlank() && !busy) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        .testTag("ai_send_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send prompt", tint = Color.White)
                }
            }
        }
    }

    if (showHistorySheet) {
        ModalBottomSheet(onDismissRequest = { showHistorySheet = false }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("${activeExam.group.aiTitle} Conversations", style = MaterialTheme.typography.titleLarge)
                if (conversations.isEmpty()) {
                    Text(
                        "No saved chats for ${activeExam.group.displayName} yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    conversations.take(15).forEach { conv ->
                        MastreyCard(
                            onClick = {
                                viewModel.selectConversation(conv.id)
                                showHistorySheet = false
                            },
                            accentBorder = if (conv.id == activeConvId) accent else null
                        ) {
                            Text(conv.title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                            Text(
                                "${conv.responseStyle} • ${conv.messageCount} messages",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }

    if (reportMessageId != null) {
        ReportIssueDialog(
            title = "Report Incorrect AI Answer",
            onDismiss = { reportMessageId = null },
            onSubmit = { reason -> viewModel.submitContentReport("AI_ANSWER", reportMessageId!!, reason) }
        )
    }
}

@Composable
private fun AiChatBubble(
    message: AiMessageEntity,
    accent: Color,
    onCopy: () -> Unit,
    onRegenerate: () -> Unit,
    onBookmark: () -> Unit,
    onReport: () -> Unit
) {
    val isUser = message.role == "USER"
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) accent.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(
                1.dp,
                if (isUser) accent.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth(if (isUser) 0.86f else 0.96f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    if (isUser) "You" else "MASTREY AI",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUser) accent else CyberCyan
                )
                Spacer(Modifier.height(4.dp))
                Text(message.content, style = MaterialTheme.typography.bodyMedium)
                if (!isUser) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(onClick = onCopy, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Answer", modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onRegenerate, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Regenerate Answer", modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onBookmark, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Filled.BookmarkBorder, contentDescription = "Bookmark AI Answer", modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onReport, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Filled.Flag, contentDescription = "Report AI Answer", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
