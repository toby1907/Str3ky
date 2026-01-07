package com.example.str3ky.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Goal::class,User::class], version = 3)
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
                    .addMigrations(
                        object : androidx.room.migration.Migration(1, 2) {
                            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                                // Add columns with defaults to user_table
                                db.execSQL("ALTER TABLE user_table ADD COLUMN current_streak INTEGER NOT NULL DEFAULT 0")
                                db.execSQL("ALTER TABLE user_table ADD COLUMN last_completed_date INTEGER NOT NULL DEFAULT 0")
                            }
                        },
                        // No-op migration 2 -> 3: the Achievement data shape changed (new field) but it's stored via type converter; keep DB compatible.
                        object : androidx.room.migration.Migration(2, 3) {
                            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                                // No schema changes required; keep for explicitness
                            }
                        }
                    )
                    .build().also { instance = it }
            }
        }
    }
}















