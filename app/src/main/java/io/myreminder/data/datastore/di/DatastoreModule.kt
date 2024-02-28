package io.myreminder.data.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import io.myreminder.UserPreferences
import io.myreminder.data.Constants
import io.myreminder.data.repository.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.myreminder.datastore.UserPreferencesSerializer
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatastoreModule {
	
	@Provides
	@Singleton
    fun providesUserPreferencesDataStore(
		@ApplicationContext context: Context
	): DataStore<UserPreferences> {
		return DataStoreFactory.create(
			serializer = UserPreferencesSerializer,
			corruptionHandler = UserPreferencesRepository.corruptionHandler,
			produceFile = { context.dataStoreFile(Constants.USER_PREFERENCE_DATA_STORE_NAME) }
		)
	}
	
}