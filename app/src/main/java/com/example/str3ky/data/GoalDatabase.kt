package com.example.str3ky.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Goal::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): GoalDao
}

abstract class GoalDatabase : RoomDatabase() {

    abstract fun goalDao(): GoalDao

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
                    "goal.db"
                )
                    .build()
            }
        }
    }
}