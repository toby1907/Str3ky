package com.example.voicejournal.Data.RepositoryImpl

import com.example.voicejournal.Data.model.VoiceJournal
import kotlinx.coroutines.flow.Flow

interface VoiceJournalRepository {
   fun getNote(id: Int): Flow<VoiceJournal?>
 fun delete(voiceJournal: VoiceJournal)
  suspend  fun save(voiceJournal: VoiceJournal)
  suspend fun update(voiceJournal: VoiceJournal)
    fun getAllVoiceJournals() : Flow<List<VoiceJournal>>
}
