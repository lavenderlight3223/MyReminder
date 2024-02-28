package io.myreminder.extension

import io.myreminder.data.local.model.ReminderDb
import io.myreminder.data.model.Reminder

fun ReminderDb.toReminder(): Reminder {
	return Reminder(
		id = id,
		name = name,
		hour = hour,
		minute = minute,
		messages = messages,
		repeatOnDays = repeatOnDays,
		randomMessage = randomMessage,
		isActive = isActive
	)
}
