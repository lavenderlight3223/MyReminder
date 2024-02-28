package io.myreminder.data.local.di

import android.content.Context
import io.myreminder.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LocalModule {
	
	@Provides
	@Singleton
	fun provideAppDatabase(
		@ApplicationContext context: Context
	): AppDatabase = AppDatabase.getInstance(context)
	
}