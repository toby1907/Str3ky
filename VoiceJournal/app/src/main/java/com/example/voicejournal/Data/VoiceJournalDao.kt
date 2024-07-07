package com.example.voicejournal.Data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.voicejournal.Data.model.VoiceJournal
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceJournalDao {
    @Query("SELECT* FROM `voice.db`WHERE voice_id =:voiceId")
     fun getLetter(voiceId: Int): Flow<VoiceJournal?>
    @Query("SELECT * FROM 'voice.db' ORDER By voice_id ASC" )
    fun getRecentLetter(): LiveData<List<VoiceJournal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(voiceJournal: VoiceJournal):Long
    @Insert
    fun insertAll(vararg voiceJournal: VoiceJournal)

    @Update
    fun update(voiceJournal: VoiceJournal)

    @Delete
    fun delete(voiceJournal: VoiceJournal)

    @Query("SELECT * FROM `voice.db`")
    fun getAllLetters(): Flow<List<VoiceJournal>>
}