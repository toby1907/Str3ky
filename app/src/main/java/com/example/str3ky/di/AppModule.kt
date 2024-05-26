package com.example.str3ky.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.str3ky.data.GoalDatabase
import com.example.str3ky.repository.GoalRepository
import com.example.str3ky.repository.GoalRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideContext(application: Application): Context = application.applicationContext

    @Provides
    @Singleton
    fun provideGoalDatabase(app: Application): GoalDatabase {
        return Room.databaseBuilder(app.applicationContext, GoalDatabase::class.java, "goal")
            .build()
    }

    @Provides
    @Singleton
    fun provideGoalRepository(db: GoalDatabase): GoalRepositoryImpl{
        return GoalRepositoryImpl(db.goalDao())
    }


}