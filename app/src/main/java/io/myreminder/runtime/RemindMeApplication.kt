package io.myreminder.runtime

import android.app.Application
import android.os.Build
import io.myreminderapp.BuildConfig
import io.myreminder.common.RemindMeNotificationManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class RemindMeApplication: Application() {
	
	@Inject lateinit var remindMeNotificationManager: RemindMeNotificationManager
	override fun onCreate() {
		super.onCreate()
		if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			remindMeNotificationManager.createChannel()
		}
	}
}