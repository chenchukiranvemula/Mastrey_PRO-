package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AiResponseStyle
import com.example.ui.theme.AccentColorPreference
import com.example.ui.theme.InterfaceDensityPreference
import com.example.ui.theme.TextSizePreference
import com.example.ui.theme.ThemeModePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.mastreyDataStore: DataStore<Preferences> by preferencesDataStore(name = "mastrey_pro_prefs")

data class AppSettingsState(
    val onboardingCompleted: Boolean = false,
    val profileSetupCompleted: Boolean = false,
    val guestModeActive: Boolean = false,
    val themeMode: ThemeModePreference = ThemeModePreference.DARK,
    val accentColor: AccentColorPreference = AccentColorPreference.ELECTRIC_BLUE,
    val interfaceDensity: InterfaceDensityPreference = InterfaceDensityPreference.COMFORTABLE,
    val textSize: TextSizePreference = TextSizePreference.DEFAULT,
    val lineSpacingMultiplier: Float = 1.0f,
    val readingComfortWarm: Boolean = false,
    val mathMonospaceHighlight: Boolean = true,
    val reducedMotion: Boolean = false,
    val highContrast: Boolean = false,
    val largerControls: Boolean = false,
    val notificationsEnabled: Boolean = false,
    val reminderHour: Int = 19,
    val studyReminders: Boolean = true,
    val revisionAlerts: Boolean = true,
    val testAlerts: Boolean = true,
    val aiExplanationStyle: AiResponseStyle = AiResponseStyle.STEP_BY_STEP,
    val aiResponseLength: String = "Balanced",
    val aiPersonalizationEnabled: Boolean = true,
    val guestAiUsageCount: Int = 0,
    val studyPlanPaused: Boolean = false
)

class SettingsPreferences(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val PROFILE_SETUP_COMPLETED = booleanPreferencesKey("profile_setup_completed")
        val GUEST_MODE_ACTIVE = booleanPreferencesKey("guest_mode_active")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val INTERFACE_DENSITY = stringPreferencesKey("interface_density")
        val TEXT_SIZE = stringPreferencesKey("text_size")
        val LINE_SPACING = floatPreferencesKey("line_spacing")
        val READING_COMFORT_WARM = booleanPreferencesKey("reading_comfort_warm")
        val MATH_MONO_HIGHLIGHT = booleanPreferencesKey("math_mono_highlight")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val LARGER_CONTROLS = booleanPreferencesKey("larger_controls")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val STUDY_REMINDERS = booleanPreferencesKey("study_reminders")
        val REVISION_ALERTS = booleanPreferencesKey("revision_alerts")
        val TEST_ALERTS = booleanPreferencesKey("test_alerts")
        val AI_EXPLANATION_STYLE = stringPreferencesKey("ai_explanation_style")
        val AI_RESPONSE_LENGTH = stringPreferencesKey("ai_response_length")
        val AI_PERSONALIZATION = booleanPreferencesKey("ai_personalization")
        val GUEST_AI_USAGE = intPreferencesKey("guest_ai_usage")
        val STUDY_PLAN_PAUSED = booleanPreferencesKey("study_plan_paused")
    }

    val settingsFlow: Flow<AppSettingsState> = context.mastreyDataStore.data.map { prefs ->
        AppSettingsState(
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            profileSetupCompleted = prefs[Keys.PROFILE_SETUP_COMPLETED] ?: false,
            guestModeActive = prefs[Keys.GUEST_MODE_ACTIVE] ?: false,
            themeMode = ThemeModePreference.entries.firstOrNull { it.name == prefs[Keys.THEME_MODE] }
                ?: ThemeModePreference.DARK,
            accentColor = AccentColorPreference.entries.firstOrNull { it.name == prefs[Keys.ACCENT_COLOR] }
                ?: AccentColorPreference.ELECTRIC_BLUE,
            interfaceDensity = InterfaceDensityPreference.entries.firstOrNull { it.name == prefs[Keys.INTERFACE_DENSITY] }
                ?: InterfaceDensityPreference.COMFORTABLE,
            textSize = TextSizePreference.entries.firstOrNull { it.name == prefs[Keys.TEXT_SIZE] }
                ?: TextSizePreference.DEFAULT,
            lineSpacingMultiplier = prefs[Keys.LINE_SPACING] ?: 1.0f,
            readingComfortWarm = prefs[Keys.READING_COMFORT_WARM] ?: false,
            mathMonospaceHighlight = prefs[Keys.MATH_MONO_HIGHLIGHT] ?: true,
            reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false,
            highContrast = prefs[Keys.HIGH_CONTRAST] ?: false,
            largerControls = prefs[Keys.LARGER_CONTROLS] ?: false,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: false,
            reminderHour = prefs[Keys.REMINDER_HOUR] ?: 19,
            studyReminders = prefs[Keys.STUDY_REMINDERS] ?: true,
            revisionAlerts = prefs[Keys.REVISION_ALERTS] ?: true,
            testAlerts = prefs[Keys.TEST_ALERTS] ?: true,
            aiExplanationStyle = AiResponseStyle.entries.firstOrNull { it.name == prefs[Keys.AI_EXPLANATION_STYLE] }
                ?: AiResponseStyle.STEP_BY_STEP,
            aiResponseLength = prefs[Keys.AI_RESPONSE_LENGTH] ?: "Balanced",
            aiPersonalizationEnabled = prefs[Keys.AI_PERSONALIZATION] ?: true,
            guestAiUsageCount = prefs[Keys.GUEST_AI_USAGE] ?: 0,
            studyPlanPaused = prefs[Keys.STUDY_PLAN_PAUSED] ?: false
        )
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.mastreyDataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setProfileSetupCompleted(completed: Boolean) {
        context.mastreyDataStore.edit { it[Keys.PROFILE_SETUP_COMPLETED] = completed }
    }

    suspend fun setGuestModeActive(active: Boolean) {
        context.mastreyDataStore.edit { it[Keys.GUEST_MODE_ACTIVE] = active }
    }

    suspend fun updateThemeMode(mode: ThemeModePreference) {
        context.mastreyDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun updateAccentColor(accent: AccentColorPreference) {
        context.mastreyDataStore.edit { it[Keys.ACCENT_COLOR] = accent.name }
    }

    suspend fun updateDensity(density: InterfaceDensityPreference) {
        context.mastreyDataStore.edit { it[Keys.INTERFACE_DENSITY] = density.name }
    }

    suspend fun updateTextSize(size: TextSizePreference) {
        context.mastreyDataStore.edit { it[Keys.TEXT_SIZE] = size.name }
    }

    suspend fun updateLineSpacing(multiplier: Float) {
        context.mastreyDataStore.edit { it[Keys.LINE_SPACING] = multiplier.coerceIn(0.9f, 1.5f) }
    }

    suspend fun updateReadingComfort(warm: Boolean) {
        context.mastreyDataStore.edit { it[Keys.READING_COMFORT_WARM] = warm }
    }

    suspend fun updateMathHighlight(enabled: Boolean) {
        context.mastreyDataStore.edit { it[Keys.MATH_MONO_HIGHLIGHT] = enabled }
    }

    suspend fun updateReducedMotion(enabled: Boolean) {
        context.mastreyDataStore.edit { it[Keys.REDUCED_MOTION] = enabled }
    }

    suspend fun updateHighContrast(enabled: Boolean) {
        context.mastreyDataStore.edit { it[Keys.HIGH_CONTRAST] = enabled }
    }

    suspend fun updateLargerControls(enabled: Boolean) {
        context.mastreyDataStore.edit { it[Keys.LARGER_CONTROLS] = enabled }
    }

    suspend fun updateNotifications(
        enabled: Boolean? = null,
        reminderHour: Int? = null,
        studyReminders: Boolean? = null,
        revisionAlerts: Boolean? = null,
        testAlerts: Boolean? = null
    ) {
        context.mastreyDataStore.edit { prefs ->
            enabled?.let { prefs[Keys.NOTIFICATIONS_ENABLED] = it }
            reminderHour?.let { prefs[Keys.REMINDER_HOUR] = it.coerceIn(5, 23) }
            studyReminders?.let { prefs[Keys.STUDY_REMINDERS] = it }
            revisionAlerts?.let { prefs[Keys.REVISION_ALERTS] = it }
            testAlerts?.let { prefs[Keys.TEST_ALERTS] = it }
        }
    }

    suspend fun updateAiSettings(
        style: AiResponseStyle? = null,
        length: String? = null,
        personalization: Boolean? = null
    ) {
        context.mastreyDataStore.edit { prefs ->
            style?.let { prefs[Keys.AI_EXPLANATION_STYLE] = it.name }
            length?.let { prefs[Keys.AI_RESPONSE_LENGTH] = it }
            personalization?.let { prefs[Keys.AI_PERSONALIZATION] = it }
        }
    }

    suspend fun incrementGuestAiUsage() {
        context.mastreyDataStore.edit { prefs ->
            prefs[Keys.GUEST_AI_USAGE] = (prefs[Keys.GUEST_AI_USAGE] ?: 0) + 1
        }
    }

    suspend fun setStudyPlanPaused(paused: Boolean) {
        context.mastreyDataStore.edit { it[Keys.STUDY_PLAN_PAUSED] = paused }
    }

    suspend fun resetAllPreferences() {
        context.mastreyDataStore.edit { it.clear() }
    }
}
