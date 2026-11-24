package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.remote.configureAppCheck
import com.example.ui.AppDestination
import com.example.ui.MastreyViewModel
import com.example.ui.PrimaryTab
import com.example.ui.components.OfflineStatusBanner
import com.example.ui.screens.ActiveExamScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AuthChoiceScreen
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.ChapterDetailScreen
import com.example.ui.screens.DeveloperProfileScreen
import com.example.ui.screens.ExamResultsScreen
import com.example.ui.screens.FormulaCenterScreen
import com.example.ui.screens.GlobalSearchScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.LearnScreen
import com.example.ui.screens.MastreyAiScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PracticeScreen
import com.example.ui.screens.ProfileSetupScreen
import com.example.ui.screens.RankPredictorScreen
import com.example.ui.screens.RevisionCenterScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StudyPlannerScreen
import com.example.ui.screens.TestGeneratorScreen
import com.example.ui.theme.MastreyProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // CRITICAL: Configure Firebase App Check BEFORE any Firebase, AI, or UI initialization.
        configureAppCheck(this, intent)
        enableEdgeToEdge()

        setContent {
            val viewModel: MastreyViewModel = viewModel(
                factory = MastreyViewModel.factory(application)
            )
            MastreyProAppRoot(viewModel = viewModel)
        }
    }
}

@Composable
private fun MastreyProAppRoot(viewModel: MastreyViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val destination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    MastreyProTheme(
        themeMode = settings.themeMode,
        accent = settings.accentColor,
        textSize = settings.textSize,
        lineSpacingMultiplier = settings.lineSpacingMultiplier,
        highContrast = settings.highContrast,
        reducedMotion = settings.reducedMotion,
        largerControls = settings.largerControls,
        density = settings.interfaceDensity
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (val dest = destination) {
                AppDestination.Splash -> {
                    SplashScreen(onFinished = { viewModel.completeSplash() })
                }
                AppDestination.Onboarding -> {
                    OnboardingScreen(onFinish = { viewModel.finishOnboarding() })
                }
                AppDestination.Auth -> {
                    AuthChoiceScreen(
                        onGoogleSuccess = { user -> viewModel.onGoogleSignInSuccess(user) },
                        onContinueAsGuest = { viewModel.continueAsGuest() }
                    )
                }
                AppDestination.ProfileSetup -> {
                    val profile by viewModel.profile.collectAsStateWithLifecycle()
                    BackHandler(enabled = settings.profileSetupCompleted) { viewModel.navigateBack() }
                    ProfileSetupScreen(
                        existingProfile = profile,
                        isEditingFromSettings = settings.profileSetupCompleted,
                        onSave = { name, avatarIdx, exam, year, level, dailyMin, lang ->
                            viewModel.saveStudentProfile(
                                displayName = name,
                                avatarIndex = avatarIdx,
                                exam = exam,
                                targetYear = year,
                                prepLevel = level,
                                dailyMinutes = dailyMin,
                                language = lang,
                                navigateHomeAfter = !settings.profileSetupCompleted
                            )
                            if (settings.profileSetupCompleted) {
                                viewModel.navigateBack()
                            }
                        }
                    )
                }
                is AppDestination.MainShell -> {
                    MainAdaptiveShell(
                        activeTab = dest.tab,
                        isOffline = isOffline,
                        snackbarHostState = snackbarHostState,
                        viewModel = viewModel
                    )
                }
                is AppDestination.ChapterDetail -> {
                    ChapterDetailScreen(chapterId = dest.chapterId, viewModel = viewModel)
                }
                AppDestination.FormulaCenter -> {
                    FormulaCenterScreen(viewModel = viewModel)
                }
                AppDestination.RevisionCenter -> {
                    RevisionCenterScreen(viewModel = viewModel)
                }
                AppDestination.StudyPlanner -> {
                    StudyPlannerScreen(viewModel = viewModel)
                }
                AppDestination.Analytics -> {
                    AnalyticsScreen(viewModel = viewModel)
                }
                AppDestination.Bookmarks -> {
                    BookmarksScreen(viewModel = viewModel)
                }
                AppDestination.GlobalSearch -> {
                    GlobalSearchScreen(viewModel = viewModel)
                }
                AppDestination.RankPredictor -> {
                    RankPredictorScreen(viewModel = viewModel)
                }
                AppDestination.Settings -> {
                    SettingsScreen(viewModel = viewModel)
                }
                AppDestination.DeveloperProfile -> {
                    DeveloperProfileScreen(viewModel = viewModel)
                }
                AppDestination.ActiveExam -> {
                    ActiveExamScreen(viewModel = viewModel)
                }
                AppDestination.ExamResults -> {
                    ExamResultsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

private fun PrimaryTab.icon(): ImageVector = when (this) {
    PrimaryTab.HOME -> Icons.Filled.Home
    PrimaryTab.LEARN -> Icons.AutoMirrored.Filled.MenuBook
    PrimaryTab.PRACTICE -> Icons.Filled.Quiz
    PrimaryTab.TESTS -> Icons.Filled.Assignment
    PrimaryTab.AI -> Icons.Filled.Psychology
}

@Composable
private fun MainAdaptiveShell(
    activeTab: PrimaryTab,
    isOffline: Boolean,
    snackbarHostState: SnackbarHostState,
    viewModel: MastreyViewModel
) {
    BackHandler(enabled = activeTab != PrimaryTab.HOME) {
        viewModel.switchPrimaryTab(PrimaryTab.HOME)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        if (useRail) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { padding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .statusBarsPadding()
                ) {
                    NavigationRail(modifier = Modifier.fillMaxHeight()) {
                        PrimaryTab.entries.forEach { tab ->
                            NavigationRailItem(
                                selected = activeTab == tab,
                                onClick = { viewModel.switchPrimaryTab(tab) },
                                icon = { Icon(tab.icon(), contentDescription = tab.label) },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag("nav_rail_${tab.name.lowercase()}")
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        OfflineStatusBanner(isOffline = isOffline)
                        Box(modifier = Modifier.weight(1f)) {
                            PrimaryTabContent(tab = activeTab, viewModel = viewModel)
                        }
                    }
                }
            }
        } else {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    NavigationBar(windowInsets = WindowInsets.navigationBars) {
                        PrimaryTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = activeTab == tab,
                                onClick = { viewModel.switchPrimaryTab(tab) },
                                icon = { Icon(tab.icon(), contentDescription = tab.label) },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag("nav_bar_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    OfflineStatusBanner(isOffline = isOffline)
                    Box(modifier = Modifier.weight(1f)) {
                        PrimaryTabContent(tab = activeTab, viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun PrimaryTabContent(tab: PrimaryTab, viewModel: MastreyViewModel) {
    when (tab) {
        PrimaryTab.HOME -> HomeDashboardScreen(viewModel = viewModel)
        PrimaryTab.LEARN -> LearnScreen(viewModel = viewModel)
        PrimaryTab.PRACTICE -> PracticeScreen(viewModel = viewModel)
        PrimaryTab.TESTS -> TestGeneratorScreen(viewModel = viewModel)
        PrimaryTab.AI -> MastreyAiScreen(viewModel = viewModel)
    }
}
