package com.mywordsmyway.data.repository

import android.content.Context
import com.mywordsmyway.data.model.BorromeanWordCalculationProgress
import com.mywordsmyway.data.model.BorromeanWordCalculationUiState
import org.json.JSONArray

class BorromeanCalculationStateRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun loadUiState(): BorromeanWordCalculationUiState {
        val logs = runCatching {
            val array = JSONArray(preferences.getString(KEY_LOGS, "[]"))
            List(array.length()) { index -> array.optString(index) }
        }.getOrDefault(emptyList())
        return BorromeanWordCalculationUiState(
            isRunning = preferences.getBoolean(KEY_IS_RUNNING, false),
            message = preferences.getString(KEY_MESSAGE, "").orEmpty(),
            logLines = logs,
            error = preferences.getString(KEY_ERROR, "").orEmpty(),
        )
    }

    fun saveUiState(state: BorromeanWordCalculationUiState) {
        val logs = JSONArray().also { array ->
            state.logLines.takeLast(MAX_LOG_LINES).forEach { array.put(it) }
        }
        preferences.edit()
            .putBoolean(KEY_IS_RUNNING, state.isRunning)
            .putString(KEY_MESSAGE, state.message)
            .putString(KEY_LOGS, logs.toString())
            .putString(KEY_ERROR, state.error)
            .apply()
    }

    fun loadProgress(): BorromeanWordCalculationProgress =
        BorromeanWordCalculationProgress(
            isActive = preferences.getBoolean(KEY_PROGRESS_ACTIVE, false),
            completedSteps = preferences.getInt(KEY_PROGRESS_COMPLETED, 0),
            totalSteps = preferences.getInt(KEY_PROGRESS_TOTAL, 0),
            currentStep = preferences.getString(KEY_PROGRESS_STEP, "").orEmpty(),
            elapsedMillis = preferences.getLong(KEY_PROGRESS_ELAPSED, 0L),
            estimatedRemainingMillis = preferences.getLong(KEY_PROGRESS_REMAINING, NO_REMAINING)
                .takeIf { it != NO_REMAINING },
        )

    fun saveProgress(progress: BorromeanWordCalculationProgress) {
        preferences.edit()
            .putBoolean(KEY_PROGRESS_ACTIVE, progress.isActive)
            .putInt(KEY_PROGRESS_COMPLETED, progress.completedSteps)
            .putInt(KEY_PROGRESS_TOTAL, progress.totalSteps)
            .putString(KEY_PROGRESS_STEP, progress.currentStep)
            .putLong(KEY_PROGRESS_ELAPSED, progress.elapsedMillis)
            .putLong(KEY_PROGRESS_REMAINING, progress.estimatedRemainingMillis ?: NO_REMAINING)
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "borromean_calculation_state"
        const val KEY_IS_RUNNING = "is_running"
        const val KEY_MESSAGE = "message"
        const val KEY_LOGS = "logs"
        const val KEY_ERROR = "error"
        const val KEY_PROGRESS_ACTIVE = "progress_active"
        const val KEY_PROGRESS_COMPLETED = "progress_completed"
        const val KEY_PROGRESS_TOTAL = "progress_total"
        const val KEY_PROGRESS_STEP = "progress_step"
        const val KEY_PROGRESS_ELAPSED = "progress_elapsed"
        const val KEY_PROGRESS_REMAINING = "progress_remaining"
        const val NO_REMAINING = -1L
        const val MAX_LOG_LINES = 300
    }
}
