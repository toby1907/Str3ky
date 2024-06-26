package com.example.str3ky.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Goal::class,User::class], version = 1)
@TypeConverters(OccurrenceSelectionConverter::class, ProgressConverter::class, DurationTypeConverter::class,Converters::class)
abstract class GoalDatabase : RoomDatabase() {

    abstract fun goalDao(): GoalDao
    abstract fun userDao(): UserDao
    companion object {

        @Volatile
        private var instance: GoalDatabase? = null

        /**
         * Returns an instance of Room Database.
         *
         * @param context application context
         * @return The singleton LetterDatabase
         */
        fun getInstance(context: Context): GoalDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context,
                    GoalDatabase::class.java,
                    "user_goals_database"
                )
                    .fallbackToDestructiveMigration() // Add this line if you want to handle migrations by destroying and recreating the database
                    .build().also { instance = it }
                   // .build()
            }
        }
    }
}