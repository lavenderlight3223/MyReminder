package io.myreminder.ui.new_reminder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.myreminder.data.local.model.ReminderDb
import io.myreminder.data.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class NewReminderViewModel @Inject constructor(
	private val reminderRepository: ReminderRepository
): ViewModel() {
	
	var reminderName by mutableStateOf("")
		private set
	
	fun updateName(name: String) {
		reminderName = name
	}
	
	fun save() {
		viewModelScope.launch(Dispatchers.IO) {
			reminderRepository.insertReminder(
				ReminderDb(
					id = Random(System.currentTimeMillis()).nextInt(),
					name = reminderName,
					hour = 0,
					minute = 0,
					messages = emptyList(),
					repeatOnDays = emptyList(),
					randomMessage = true,
					isActive = false
				)
			)
		}
	}
	
}