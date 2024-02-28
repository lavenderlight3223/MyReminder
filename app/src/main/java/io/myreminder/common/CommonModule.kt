package io.myreminder.common

import android.content.Context
import io.myreminder.common.RemindMeNotificationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CommonModule {

	@Provides
	@Singleton
	fun provideRemindMeAlarmManager(
		@ApplicationContext context: Context
	): RemindMeAlarmManager = RemindMeAlarmManager(context)
	
	@Provides
	@Singleton
	fun provideRemindMeNotificationManager(
		@ApplicationContext context: Context
	): RemindMeNotificationManager = RemindMeNotificationManager(context)
	
}