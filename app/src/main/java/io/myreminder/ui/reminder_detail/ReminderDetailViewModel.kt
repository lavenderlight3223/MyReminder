package io.myreminder.ui.reminder_detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.myreminder.common.RemindMeAlarmManager
import io.myreminder.data.DayOfWeek
import io.myreminder.data.HourClockType
import io.myreminder.data.ReminderMessageType
import io.myreminder.data.TimeType
import io.myreminder.data.model.Reminder
import io.myreminder.data.repository.ReminderRepository
import io.myreminder.data.repository.UserPreferencesRepository
import io.myreminder.extension.convert12HourTo24Hour
import io.myreminder.extension.convert24HourTo12Hour
import io.myreminder.extension.isHourAm
import io.myreminder.extension.toReminderDb
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ReminderDetailViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val remindMeAlarmManager: RemindMeAlarmManager,
    private val reminderRepository: ReminderRepository
): ViewModel() {
	
	var reminderName by mutableStateOf("")
		private set
	
	var hours by mutableStateOf(0)
		private set
	
	var minutes by mutableStateOf(0)
		private set
	
	var clockPositionValue by mutableStateOf(0)
		private set
	
	var animateClockPositionValue by mutableStateOf(false)
	    private set
	
	var isReminderActive by mutableStateOf(false)
		private set
	
	var isReminderMessageRandom by mutableStateOf(true)
		private set
	
	var is24Hour by mutableStateOf(false)
		private set
	
	var autoSave by mutableStateOf(false)
		private set
	
	var selectedTimeType by mutableStateOf(TimeType.Hours)
		private set
	
	var hourClockType by mutableStateOf(HourClockType.AM)
		private set
	
	var currentReminder by mutableStateOf(Reminder.Null)
		private set
	
	var messages = mutableStateListOf<Pair<String, ReminderMessageType>>()
		private set
	
	var repeatOnDays = mutableStateListOf<DayOfWeek>()
		private set
	
	private val _currentReminderId = MutableStateFlow(Reminder.Null.id)
	private val currentReminderId: StateFlow<Int> = _currentReminderId
	
	init {
		viewModelScope.launch {
			userPreferencesRepository.getUserPreferences.collect { preferences ->
				is24Hour = preferences.is24Hour
				autoSave = preferences.autoSave
			}
		}
		
		viewModelScope.launch {
			currentReminderId.collect { id ->
				reminderRepository.getReminderById(id).collect(this@ReminderDetailViewModel::updateWithReminder)
			}
		}
	}
	
	fun updateWithReminder(reminder: Reminder) {
		updateReminderName(reminder.name, false)
		updateIsReminderActive(reminder.isActive, false)
		updateIsReminderReminderMessageRandom(reminder.randomMessage, false)
		
		hours = reminder.hour
		minutes = reminder.minute
		currentReminder = reminder
		hourClockType = if (isHourAm(reminder.hour)) HourClockType.AM else HourClockType.PM
		clockPositionValue = if (selectedTimeType == TimeType.Hours) {
			if (isHourAm(reminder.hour)) hours + 1
			else convert24HourTo12Hour(reminder.hour).first.toInt() + 1
		} else minutes + 1
		repeatOnDays.apply {
			clear()
			addAll(reminder.repeatOnDays)
		}
		messages.apply {
			clear()
			addAll(reminder.messages.map { it to ReminderMessageType.Fixed })
		}
		
		viewModelScope.launch {
			_currentReminderId.emit(reminder.id)
		}
	}
	
	fun updateClockPosition(pos: Int, save: Boolean = true) {
		clockPositionValue = pos
		
		when (selectedTimeType) {
			TimeType.Hours -> {
				hours = when (hourClockType) {
					HourClockType.AM -> convert12HourTo24Hour((pos - 1).coerceAtLeast(0), "am")
					HourClockType.PM -> convert12HourTo24Hour((pos - 1).coerceAtLeast(0), "pm")
				}
			}
			TimeType.Minutes -> {
				minutes = (pos - 1).coerceAtLeast(0)
			}
		}
		
		if (autoSave && save) saveReminder()
	}
	
	fun updateTimeType(type: TimeType) {
		selectedTimeType = type
		
		clockPositionValue = when (type) {
			TimeType.Hours -> convert24HourTo12Hour(hours).first.toInt() + 1
			TimeType.Minutes -> minutes + 1
		}
		
		animateClockPositionValue = true
	}
	
	fun updateHourClockType(type: HourClockType, save: Boolean = true) {
		hourClockType = type
		
		hours = when (type) {
			HourClockType.AM -> convert12HourTo24Hour(hours, "am")
			HourClockType.PM -> convert12HourTo24Hour(hours, "pm")
		}
		
		clockPositionValue = convert24HourTo12Hour(hours).first.toInt()
		
		if (autoSave && save) saveReminder()
	}
	
	fun updateAnimateClockPositionValue(animate: Boolean) {
		animateClockPositionValue = animate
	}
	
	fun updateRepeatOnDays(days: List<DayOfWeek>, save: Boolean = true) {
		repeatOnDays.apply {
			clear()
			addAll(days)
		}
		
		if (autoSave && save) saveReminder()
	}
	
	fun updateReminderName(name: String, save: Boolean = true) {
		reminderName = name
		
		if (autoSave && save) saveReminder()
	}
	
	fun updateIsReminderActive(active: Boolean, save: Boolean = true) {
		isReminderActive = active
		
		if (autoSave && save) saveReminder()
	}
	
	fun updateIsReminderReminderMessageRandom(random: Boolean, save: Boolean = true) {
		isReminderMessageRandom = random
		
		if (autoSave && save) saveReminder()
	}
	
	/**
	 * @return true if saved, false otherwise
	 */
	fun saveReminder(): Result<Boolean> {
		if (reminderName.isBlank()) return Result.failure(
			Throwable("Reminder name cannot be empty!")
		)
		
		val mReminder = currentReminder.copy(
			name = reminderName,
			hour = hours,
			minute = minutes,
			messages = messages.map { it.first },
			repeatOnDays = repeatOnDays,
			randomMessage = isReminderMessageRandom,
			isActive = isReminderActive
		)
		
		viewModelScope.launch(Dispatchers.IO) {
			reminderRepository.updateReminder(
				mReminder.toReminderDb()
			)
		}
		
		if (isReminderActive) remindMeAlarmManager.validateAndStart(mReminder)
		else remindMeAlarmManager.cancelReminder(mReminder)
		
		return Result.success(true)
	}
	
}