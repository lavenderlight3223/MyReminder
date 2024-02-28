package io.myreminder.ui.remind_me

import io.myreminder.UserPreferences
import io.myreminder.data.model.Reminder

data class RemindMeUiState(
	val reminders: List<Reminder> = emptyList(),
	val userPreferences: UserPreferences = UserPreferences(),
	val selectedReminder: Reminder? = null,
	val isDetailOnlyOpen: Boolean = false,
	val showDeleteConfirmationDialog: Boolean = false,
	val error: String? = null
)
