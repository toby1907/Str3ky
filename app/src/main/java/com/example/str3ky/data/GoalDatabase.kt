package com.example.str3ky.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Goal::class], version = 1)
@TypeConverters(OccurrenceConverter::class, ProgressConverter::class)
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
                    "goal"
                )
                    .build()
            }
        }
    }
}