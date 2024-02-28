package io.myreminder.ui.remind_me

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.myreminder.common.RemindMeAlarmManager
import io.myreminder.data.repository.ReminderRepository
import io.myreminder.data.repository.UserPreferencesRepository

class RemindMeViewModelFactory(
    private val reminderRepository: ReminderRepository,
    private val remindMeAlarmManager: RemindMeAlarmManager,
    private val userPreferencesRepository: UserPreferencesRepository
): ViewModelProvider.Factory {
	
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (RemindMeViewModel::class.java.isAssignableFrom(modelClass)) {
			return RemindMeViewModel(reminderRepository, remindMeAlarmManager, userPreferencesRepository) as T
		}
		
		return modelClass
			.getConstructor(
				ReminderRepository::class.java,
				RemindMeAlarmManager::class.java,
				UserPreferencesRepository::class.java
			)
			.newInstance(reminderRepository, remindMeAlarmManager, userPreferencesRepository)
	}
}