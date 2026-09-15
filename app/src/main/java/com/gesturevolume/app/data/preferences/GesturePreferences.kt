package com.gesturevolume.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gesturevolume.app.data.model.GestureConfig
import com.gesturevolume.app.data.model.HudPosition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gesture_volume_prefs")

class GesturePreferences(private val context: Context) {

    companion object {
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_CIRCLE_SENSITIVITY = floatPreferencesKey("circle_sensitivity")
        private val KEY_TIMEOUT_SECONDS = intPreferencesKey("timeout_seconds")
        private val KEY_SWIPE_SENSITIVITY = floatPreferencesKey("swipe_sensitivity")
        private val KEY_SHOW_PERCENTAGE = booleanPreferencesKey("show_percentage")
        private val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        private val KEY_HUD_POSITION = stringPreferencesKey("hud_position")
        private val KEY_EDGE_SIDE = stringPreferencesKey("edge_side")
        private val KEY_EDGE_OPACITY = floatPreferencesKey("edge_opacity")
        private val KEY_EDGE_LENGTH_DP = intPreferencesKey("edge_length_dp")
        private val KEY_EDGE_Y_OFFSET_DP = intPreferencesKey("edge_y_offset_dp")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val configFlow: Flow<GestureConfig> = context.dataStore.data.map { preferences ->
        val posString = preferences[KEY_HUD_POSITION] ?: HudPosition.CENTER.name
        val position = try {
            HudPosition.valueOf(posString)
        } catch (_: Exception) {
            HudPosition.CENTER
        }

        val sideString = preferences[KEY_EDGE_SIDE] ?: com.gesturevolume.app.data.model.EdgeSide.RIGHT.name
        val edgeSide = try {
            com.gesturevolume.app.data.model.EdgeSide.valueOf(sideString)
        } catch (_: Exception) {
            com.gesturevolume.app.data.model.EdgeSide.RIGHT
        }

        GestureConfig(
            circleSensitivity = preferences[KEY_CIRCLE_SENSITIVITY] ?: 0.70f,
            timeoutSeconds = preferences[KEY_TIMEOUT_SECONDS] ?: 3,
            swipeSensitivity = preferences[KEY_SWIPE_SENSITIVITY] ?: 1.0f,
            showPercentage = preferences[KEY_SHOW_PERCENTAGE] ?: true,
            hapticFeedback = preferences[KEY_HAPTIC_FEEDBACK] ?: true,
            hudPosition = position,
            edgeSide = edgeSide,
            edgeOpacity = preferences[KEY_EDGE_OPACITY] ?: 0.55f,
            edgeLengthDp = preferences[KEY_EDGE_LENGTH_DP] ?: 120,
            edgeYOffsetDp = preferences[KEY_EDGE_Y_OFFSET_DP] ?: 0
        )
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun updateConfig(config: GestureConfig) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CIRCLE_SENSITIVITY] = config.circleSensitivity
            preferences[KEY_TIMEOUT_SECONDS] = config.timeoutSeconds
            preferences[KEY_SWIPE_SENSITIVITY] = config.swipeSensitivity
            preferences[KEY_SHOW_PERCENTAGE] = config.showPercentage
            preferences[KEY_HAPTIC_FEEDBACK] = config.hapticFeedback
            preferences[KEY_HUD_POSITION] = config.hudPosition.name
            preferences[KEY_EDGE_SIDE] = config.edgeSide.name
            preferences[KEY_EDGE_OPACITY] = config.edgeOpacity
            preferences[KEY_EDGE_LENGTH_DP] = config.edgeLengthDp
            preferences[KEY_EDGE_Y_OFFSET_DP] = config.edgeYOffsetDp
        }
    }
}
