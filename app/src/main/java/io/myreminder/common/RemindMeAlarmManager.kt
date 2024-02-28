package io.myreminder.common

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import io.myreminder.data.AlarmAction
import io.myreminder.data.DayOfWeek
import io.myreminder.data.model.Reminder
import io.myreminder.extension.calendarDayOfWeekFromRemindMeDayOfWeek
import io.myreminder.extension.calendarDayOfWeekToRemindMeDayOfWeek
import io.myreminder.extension.next
import io.myreminder.receiver.ReminderReceiver
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

class RemindMeAlarmManager @Inject constructor(
	private val context: Context
) {

	private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
	
	private fun getPendingIntent(reminder: Reminder): PendingIntent? {
		return PendingIntent.getBroadcast(
			context,
			reminder.id,
			Intent(
				context,
				ReminderReceiver::class.java
			).apply {
				action = AlarmAction.ACTION_NOTIFY
				putExtra(
					"bundle",
					Bundle().apply {
						setExtrasClassLoader(Reminder::class.java.classLoader)
						putParcelable("reminder", reminder)
					}
				)
			},
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
			} else PendingIntent.FLAG_CANCEL_CURRENT
		)
	}
	
	private fun isPendingIntentExists(reminder: Reminder): PendingIntent? {
		return PendingIntent.getBroadcast(
			context,
			reminder.id,
			Intent(
				context,
				ReminderReceiver::class.java
			).apply {
				action = AlarmAction.ACTION_NOTIFY
				putExtra(
					"bundle",
					Bundle().apply {
						setExtrasClassLoader(Reminder::class.java.classLoader)
						putParcelable("reminder", reminder)
					}
				)
			},
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
			} else PendingIntent.FLAG_NO_CREATE
		)
	}
	
	fun startReminder(reminder: Reminder, triggerAt: Long) {
		alarmManager.setExactAndAllowWhileIdle(
			AlarmManager.RTC_WAKEUP,
			triggerAt,
			getPendingIntent(reminder)
		)
	}
	
	fun validateAndStart(reminder: Reminder) {
		val nextTrigger =  Calendar.getInstance().apply {
			set(Calendar.HOUR_OF_DAY, reminder.hour)
			set(Calendar.MINUTE, reminder.minute)
			set(Calendar.SECOND, 0)
			
			when {
				System.currentTimeMillis() < timeInMillis -> {}
				reminder.repeatOnDays.isEmpty() -> {}
				reminder.repeatOnDays.size == DayOfWeek.values().size -> {
					add(Calendar.DAY_OF_MONTH, 1)
				}
				else -> {
					val currentDay = calendarDayOfWeekToRemindMeDayOfWeek(get(Calendar.DAY_OF_WEEK))
					var nextWeek = false
					val nextDay = run {
						var day = currentDay
						
						do {
							day = day.next()
							
							if (day in reminder.repeatOnDays) {
								Timber.i("return day: ${day.name}")
								
								nextWeek = currentDay == day
								
								return@run day
							}
						} while (day !in reminder.repeatOnDays)
						
						return@run currentDay  // Default value
					}
					
					Timber.i("cur day: $currentDay, next day: $nextDay")
					
					if (nextDay.ordinal < currentDay.ordinal || nextWeek) {
						// Jika hari selanjutnya < hari sekarang
						// Berarti next triggernya minggu besok
						add(Calendar.WEEK_OF_MONTH, 1)
						
						Timber.i("trigger next week!")
					}
					
					set(Calendar.DAY_OF_WEEK, calendarDayOfWeekFromRemindMeDayOfWeek(nextDay))
				}
			}
		}.timeInMillis
		
		startReminder(reminder, nextTrigger)
	}
	
	fun cancelReminder(reminder: Reminder) {
		alarmManager.cancel(getPendingIntent(reminder))
	}
}