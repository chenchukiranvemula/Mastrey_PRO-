package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.local.UserProfileEntity
import com.example.data.model.ExamSystem
import com.example.data.model.PrepLevel
import com.example.data.remote.launchInteractiveGoogleSignIn
import com.example.ui.components.MastreyCard
import com.example.ui.components.MastreyGradientButton
import com.example.ui.components.StudentAvatarColors
import com.example.ui.components.StudentAvatarLabels
import com.example.ui.components.examAccentColor
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LocalMastreyDesignConfig
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.VividViolet
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val cfg = LocalMastreyDesignConfig.current
    val scale = remember { Animatable(if (cfg.reducedMotion) 1f else 0.72f) }
    val alpha = remember { Animatable(if (cfg.reducedMotion) 1f else 0f) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        if (!cfg.reducedMotion) {
            launch { scale.animateTo(1f, tween(650, easing = FastOutSlowInEasing)) }
            launch { alpha.animateTo(1f, tween(650)) }
        }
        progress.animateTo(1f, tween(if (cfg.reducedMotion) 400 else 1350))
        delay(120)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(MidnightBlack, DeepNavy, Color(0xFF101833)))
            )
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp).widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(124.dp)
                    .scale(scale.value)
                    .alpha(alpha.value)
                    .clip(RoundedCornerShape(28.dp))
                    .border(
                        2.dp,
                        Brush.linearGradient(listOf(ElectricBlue, VividViolet, CyberCyan)),
                        RoundedCornerShape(28.dp)
                    )
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_mastrey_logo_1795491940544),
                    contentDescription = "MASTREY PRO Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "MASTREY PRO",
                style = MaterialTheme.typography.displaySmall.copy(letterSpacing = 2.5.sp),
                color = Color.White,
                modifier = Modifier.alpha(alpha.value)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "MASTER  •  PRACTICE  •  WIN",
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
                color = CyberCyan,
                modifier = Modifier.alpha(alpha.value)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "The Complete Free AI Education Ecosystem",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha.value)
            )
            Spacer(Modifier.height(36.dp))
            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier.fillMaxWidth(0.72f).height(6.dp).clip(RoundedCornerShape(50)),
                color = ElectricBlue,
                trackColor = Color(0xFF1E293B)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Preparing JEE • NEET UG • CA Foundation Engines",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF64748B)
            )
        }
    }
}

private data class OnboardingPage(
    val title: String,
    val description: String,
    val badge: String,
    val icon: ImageVector,
    val accent: Color
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = remember {
        listOf(
            OnboardingPage(
                title = "Learn everything in one place",
                description = "Complete JEE, NEET UG, and CA Foundation syllabi organized chapter-by-chapter with Beginner, Standard, and Advanced theory plus a global Formula Center.",
                badge = "01 • LEARN",
                icon = Icons.Filled.LibraryBooks,
                accent = ElectricBlue
            ),
            OnboardingPage(
                title = "Practice without limits",
                description = "Smart non-repeating question history tracks every attempt so you solve fresh problems—with Revision Mode whenever you want to retry wrong or bookmarked questions.",
                badge = "02 • PRACTICE",
                icon = Icons.Filled.Quiz,
                accent = CyberCyan
            ),
            OnboardingPage(
                title = "Generate personalized mock tests",
                description = "Build custom timed tests from 10 to 180 minutes with exam-accurate marking, live question palette, auto-submit timer, and complete step-by-step solutions.",
                badge = "03 • TEST",
                icon = Icons.Filled.Timer,
                accent = VividViolet
            ),
            OnboardingPage(
                title = "Get help from AI",
                description = "Ask MASTREY JEE AI, MASTREY NEET AI, or MASTREY CA AI for streaming step-by-step derivations, doubt solving, summaries, and validated original practice MCQs.",
                badge = "04 • MASTREY AI",
                icon = Icons.Filled.AutoAwesome,
                accent = Color(0xFF10B981)
            ),
            OnboardingPage(
                title = "Analyze and improve your performance",
                description = "Real activity charts, weak-topic tracking, spaced-repetition flashcards, a smart 7-day study planner, and historical rank estimators—100% free for every student.",
                badge = "05 • WIN",
                icon = Icons.Filled.Insights,
                accent = Color(0xFFF59E0B)
            )
        )
    }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().widthIn(max = 560.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("MASTREY PRO", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            TextButton(onClick = onFinish, modifier = Modifier.testTag("onboarding_skip_button")) {
                Text("Skip")
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).widthIn(max = 560.dp)
        ) { idx ->
            val page = pages[idx]
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(page.accent.copy(alpha = 0.16f))
                        .border(1.5.dp, page.accent.copy(alpha = 0.5f), RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(page.icon, contentDescription = page.title, tint = page.accent, modifier = Modifier.size(50.dp))
                }
                Spacer(Modifier.height(22.dp))
                Surface(shape = RoundedCornerShape(50), color = page.accent.copy(alpha = 0.14f)) {
                    Text(
                        page.badge,
                        style = MaterialTheme.typography.labelLarge,
                        color = page.accent,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(pages.size) { idx ->
                val active = pagerState.currentPage == idx
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(if (active) 28.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        val isLast = pagerState.currentPage == pages.lastIndex
        MastreyGradientButton(
            text = if (isLast) "Get Started" else "Next",
            icon = Icons.AutoMirrored.Filled.ArrowForward,
            onClick = {
                if (isLast) onFinish()
                else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            },
            modifier = Modifier.fillMaxWidth().widthIn(max = 560.dp).testTag("onboarding_next_button")
        )
    }
}

@Composable
fun AuthChoiceScreen(
    onGoogleSuccess: (FirebaseUser) -> Unit,
    onContinueAsGuest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var isSigningIn by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surface)
                )
            )
            .safeDrawingPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 480.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.ic_mastrey_logo_1795491940544),
                contentDescription = "MASTREY PRO",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(84.dp).clip(RoundedCornerShape(22.dp))
            )
            Spacer(Modifier.height(16.dp))
            Text("Welcome to MASTREY PRO", style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(
                "Learn without limits. Practice without fear. Improve every day.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))

            MastreyCard(accentBorder = MaterialTheme.colorScheme.primary) {
                Text("Option 1 • Recommended", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Text("Cloud Sync Account", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Sync your exam progress, mock test analytics, bookmarks, AI chats, and study plan across devices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {
                        authError = null
                        isSigningIn = true
                        launchInteractiveGoogleSignIn(
                            context = context,
                            credentialManager = credentialManager,
                            scope = scope,
                            onAuthSuccess = { user ->
                                isSigningIn = false
                                onGoogleSuccess(user)
                            },
                            onAuthCancelled = {
                                isSigningIn = false
                            },
                            onAuthError = { msg ->
                                isSigningIn = false
                                authError = msg
                            }
                        )
                    },
                    enabled = !isSigningIn,
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("google_sign_in_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF0F172A))
                ) {
                    if (isSigningIn) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = ElectricBlue)
                    } else {
                        Icon(Icons.Filled.AccountCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Continue with Google", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (authError != null) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        authError.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            MastreyCard {
                Text("Option 2 • Instant Exploration", style = MaterialTheme.typography.labelLarge, color = CyberCyan)
                Spacer(Modifier.height(4.dp))
                Text("Guest Mode (Local Storage)", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Explore all syllabi, formulas, practice MCQs, mock tests, and AI tutors immediately without sign-in. Upgrade to Google anytime to transfer local progress.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onContinueAsGuest,
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("continue_guest_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text("Continue as Guest", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                "100% Free Education • No Paywalls • Built by V. CHENCHUKIRAN",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(
    existingProfile: UserProfileEntity?,
    isEditingFromSettings: Boolean,
    onSave: (String, Int, ExamSystem, Int, PrepLevel, Int, String) -> Unit
) {
    var displayName by remember(existingProfile) { mutableStateOf(existingProfile?.displayName ?: "") }
    var avatarIndex by remember(existingProfile) { mutableIntStateOf(existingProfile?.avatarIndex ?: 0) }
    var selectedExam by remember(existingProfile) { mutableStateOf(ExamSystem.fromId(existingProfile?.selectedExam)) }
    var targetYear by remember(existingProfile) { mutableIntStateOf(existingProfile?.targetYear ?: 2027) }
    var prepLevel by remember(existingProfile) { mutableStateOf(PrepLevel.fromName(existingProfile?.prepLevel)) }
    var dailyMinutes by remember(existingProfile) { mutableIntStateOf(existingProfile?.dailyStudyMinutes ?: 120) }
    var language by remember(existingProfile) { mutableStateOf(existingProfile?.preferredLanguage ?: "English") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.widthIn(max = 560.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        if (isEditingFromSettings) "Edit Student Profile" else "Personalize Your Ecosystem",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        "Tailors your syllabus, mock test scoring, study planner, and MASTREY AI tutor.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            MastreyCard {
                Text("Student Identity", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    placeholder = { Text("Enter your name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                )
                Spacer(Modifier.height(12.dp))
                Text("Choose Avatar Badge", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StudentAvatarLabels.forEachIndexed { idx, label ->
                        val selected = avatarIndex == idx
                        val tint = StudentAvatarColors[idx % StudentAvatarColors.size]
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { avatarIndex = idx }
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) tint else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            color = if (selected) tint.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(tint))
                                Spacer(Modifier.width(6.dp))
                                Text(label, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            MastreyCard {
                Text("Select Target Examination", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Each exam runs an independent syllabus, question bank, and AI profile.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                ExamSystem.entries.forEach { exam ->
                    val isSelected = exam == selectedExam
                    val accent = examAccentColor(exam)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedExam = exam }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) accent else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .testTag("exam_option_${exam.id}"),
                        color = if (isSelected) accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(exam.title, style = MaterialTheme.typography.titleMedium, color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface)
                                Text(exam.patternDescription, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (isSelected) {
                                Icon(Icons.Filled.Check, contentDescription = "Selected", tint = accent)
                            }
                        }
                    }
                }
            }

            MastreyCard {
                Text("Target Year & Preparation Level", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(2026, 2027, 2028, 2029).forEach { yr ->
                        FilterChip(
                            selected = targetYear == yr,
                            onClick = { targetYear = yr },
                            label = { Text("$yr") }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrepLevel.entries.forEach { lvl ->
                        FilterChip(
                            selected = prepLevel == lvl,
                            onClick = { prepLevel = lvl },
                            label = { Text(lvl.label) }
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Daily Study Target", style = MaterialTheme.typography.titleSmall)
                    Text("${dailyMinutes / 60}h ${dailyMinutes % 60}m / day", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = dailyMinutes.toFloat(),
                    onValueChange = { dailyMinutes = (it.toInt() / 15) * 15 },
                    valueRange = 30f..480f
                )
                Spacer(Modifier.height(8.dp))
                Text("Preferred Explanation Language", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("English", "Hinglish", "Hindi", "Telugu", "Tamil").forEach { lang ->
                        FilterChip(
                            selected = language == lang,
                            onClick = { language = lang },
                            label = { Text(lang) }
                        )
                    }
                }
            }

            MastreyGradientButton(
                text = if (isEditingFromSettings) "Save Profile Changes" else "Launch MASTREY PRO",
                onClick = {
                    onSave(
                        displayName.ifBlank { "Student" },
                        avatarIndex,
                        selectedExam,
                        targetYear,
                        prepLevel,
                        dailyMinutes,
                        language
                    )
                },
                modifier = Modifier.fillMaxWidth().testTag("save_profile_button")
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}
