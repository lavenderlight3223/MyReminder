package io.myreminder.data.repository.di

import androidx.datastore.core.DataStore
import io.myreminder.UserPreferences
import io.myreminder.data.local.AppDatabase
import io.myreminder.data.repository.ReminderRepository
import io.myreminder.data.repository.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {
	
	@Provides
	@Singleton
	fun provideReminderRepository(
		appDatabase: AppDatabase
	): ReminderRepository = ReminderRepository(
		reminderDao = appDatabase.reminderDao()
	)
	
	@Provides
	@Singleton
	fun provideUserPreferencesRepository(
		userPreferencesDataStore: DataStore<UserPreferences>
	): UserPreferencesRepository = UserPreferencesRepository(
		userPreferencesDataStore = userPreferencesDataStore
	)
	
}