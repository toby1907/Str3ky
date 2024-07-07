package com.example.voicejournal.Data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.voicejournal.Data.model.VoiceJournal


@Database(
    entities = [VoiceJournal::class],
    version = 4, // Increment the version number
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2), // Keep the existing auto-migration
        AutoMigration(from = 2, to = 3), // Add a new auto-migration
        AutoMigration(from = 3, to = 4)
    ]
)

abstract class VoiceJournalDatabase : RoomDatabase() {

    abstract fun voiceJournalDao(): VoiceJournalDao

    companion object {

        @Volatile
        private var instance: VoiceJournalDatabase? = null

        /**
         * Returns an instance of Room Database.
         *
         * @param context application context
         * @return The singleton LetterDatabase
         */
        fun getInstance(context: Context): VoiceJournalDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context,
                    VoiceJournalDatabase::class.java,
                    "voice.db"
                )
                    .build()
            }
        }
    }
}