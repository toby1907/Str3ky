package com.example.str3ky.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton
import com.example.str3ky.data.GoalDatabase
import com.example.str3ky.data.GoalDao
import com.example.str3ky.data.UserDao
import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.str3ky.repository.GoalRepository
import com.example.str3ky.repository.GoalRepositoryImpl
import com.example.str3ky.repository.SettingsRepository
import com.example.str3ky.repository.UserRepository
import com.example.str3ky.use_case.AddGoal
import com.example.str3ky.use_case.DeleteGoal
import com.example.str3ky.use_case.GetGoals
import com.example.str3ky.use_case.GoalUseCases
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("timer_service_state", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideGoalDatabase(@ApplicationContext context: Context): GoalDatabase = GoalDatabase.getInstance(context)

    @Provides
    @Singleton
    fun provideGoalDao(db: GoalDatabase): GoalDao = db.goalDao()

    @Provides
    @Singleton
    fun provideUserDao(db: GoalDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun provideGoalRepository(goalDao: GoalDao, @ApplicationContext context: Context): GoalRepository {
        return com.example.str3ky.repository.GoalRepositoryImpl(goalDao, context)
    }

    @Provides
    @Singleton
    fun provideGoalRepositoryImpl(goalDao: GoalDao, @ApplicationContext context: Context): GoalRepositoryImpl {
        return com.example.str3ky.repository.GoalRepositoryImpl(goalDao, context)
    }

    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao): UserRepository {
        return com.example.str3ky.repository.UserRepositoryImpl(userDao)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(dataStore: DataStore<Preferences>): SettingsRepository {
        return SettingsRepository(dataStore)
    }

    @Provides
    @Singleton
    fun provideGoalUseCases(repository: com.example.str3ky.repository.GoalRepositoryImpl): GoalUseCases {
        return GoalUseCases(
            getGoals = GetGoals(repository),
            deleteGoal = DeleteGoal(repository),
            addGoal = AddGoal(repository)
        )
    }

    @Provides
    @Singleton
    fun provideNotificationAdapter(helper: com.florianwalther.incentivetimer.core.notification.DefaultNotificationHelper): com.example.str3ky.core.notification.NotificationAdapter {
        return com.example.str3ky.core.notification.DefaultNotificationAdapter(helper)
    }

    @Provides
    @Singleton
    fun provideTimerServiceAdapter(manager: com.example.str3ky.core.notification.TimerServiceManager): com.example.str3ky.core.notification.TimerServiceAdapter {
        return com.example.str3ky.core.notification.DefaultTimerServiceAdapter(manager)
    }

    // Fallback: provide an unqualified CoroutineScope so injection works even if @ApplicationScope is omitted
    @Provides
    @Singleton
    fun provideDefaultCoroutineScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

}
















