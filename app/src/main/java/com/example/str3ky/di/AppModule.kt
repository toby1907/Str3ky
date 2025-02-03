package com.example.str3ky.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.example.str3ky.core.notification.TimerServiceManager
import com.example.str3ky.data.GoalDatabase
import com.example.str3ky.dataStore
import com.example.str3ky.repository.GoalRepositoryImpl
import com.example.str3ky.repository.SettingsRepository
import com.example.str3ky.repository.UserRepositoryImpl
import com.example.str3ky.use_case.AddGoal
import com.example.str3ky.use_case.DeleteGoal
import com.example.str3ky.use_case.GetGoals
import com.example.str3ky.use_case.GoalUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {


    @Singleton
    @Provides
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    @Singleton
    @Provides
    fun provideContext(application: Application): Context = application.applicationContext

    @Provides
    @Singleton
    fun provideGoalDatabase(app: Application): GoalDatabase {
        return Room.databaseBuilder(app.applicationContext, GoalDatabase::class.java, "user_goals_database")
            .build()
    }
    @Provides
    @Singleton
    fun provideTimerServiceManager(application: Application): TimerServiceManager {
        return TimerServiceManager(application.applicationContext)
    }
    @Provides
    @Singleton
    fun provideGoalRepository(db: GoalDatabase,application: Application): GoalRepositoryImpl{
        return GoalRepositoryImpl(db.goalDao(), application.applicationContext)
    }
    @Provides
    @Singleton
    fun provideUserRepository(db: GoalDatabase): UserRepositoryImpl{
        return UserRepositoryImpl(db.userDao())
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(dataStore: DataStore<Preferences>): SettingsRepository {
        return SettingsRepository(dataStore)
    }

    @Provides
    @Singleton
    fun provideNoteUseCases(repository: GoalRepositoryImpl): GoalUseCases {
        return GoalUseCases(
            getGoals = GetGoals(repository),
            deleteGoal = DeleteGoal(repository),
            addGoal = AddGoal(repository),
        )
    }

}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope