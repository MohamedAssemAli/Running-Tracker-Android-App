package com.assem.runningtracker.di

import android.content.Context
import androidx.room.Room
import com.assem.runningtracker.data.db.RunningDatabase
import com.assem.runningtracker.util.Constants.RUNNING_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton


/**
 * Created by Mohamed Assem on 14-Sep-20.
 * mohamed.assem.ali@gmail.com
 */
@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunningDatabase(
        @ApplicationContext app: Context,
    ) = Room.databaseBuilder(
        app,
        RunningDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideRunningDao(db: RunningDatabase) = db.getRunDao()
}