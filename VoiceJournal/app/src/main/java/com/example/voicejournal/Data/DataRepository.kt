package com.example.voicejournal.Data

import com.example.voicejournal.Data.RepositoryImpl.VoiceJournalRepository
import com.example.voicejournal.Data.model.VoiceJournal
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.Executors

class VoiceJournalRepositoryImpl (private val voiceJournalDao: VoiceJournalDao):
    VoiceJournalRepository {
   /* companion object

    @Volatile
    private var instance: VoiceJournalRepository? = null

    fun getInstance(context: Context): VoiceJournalRepository {
        return instance ?: synchronized(VoiceJournalRepository::class.java) {
            if (instance == null) {
                val database = VoiceJournalDatabase.getInstance(context)
                instance = VoiceJournalRepository(database.voiceJournalDao())
            }
            return instance as VoiceJournalRepository
        }
    }


    }*/
    override  fun getNote(id: Int): Flow<VoiceJournal?> {
        return voiceJournalDao.getLetter(id)
    }
    private val SINGLE_EXECUTOR = Executors.newSingleThreadExecutor()
    fun executeThread(f: () -> Unit) {
        SINGLE_EXECUTOR.execute(f)
    }
    override fun delete(voiceJournal: VoiceJournal) = executeThread{
        voiceJournalDao.delete(voiceJournal)
    }
    override suspend fun save(voiceJournal: VoiceJournal)= executeThread {
        voiceJournalDao.insert(voiceJournal)
    }
    override suspend fun update(voiceJournal: VoiceJournal) = executeThread {
        voiceJournalDao.update(voiceJournal)
    }
    override fun  getAllVoiceJournals() :Flow< List<VoiceJournal>> {
        return   voiceJournalDao.getAllLetters()
    }



}