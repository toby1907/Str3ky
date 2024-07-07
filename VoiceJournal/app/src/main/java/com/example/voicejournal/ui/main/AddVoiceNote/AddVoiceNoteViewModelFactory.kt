package com.example.voicejournal.ui.main.AddVoiceNote

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.voicejournal.Data.VoiceJournalRepositoryImpl

class AddVoiceNoteViewModelFactory(val context: Context) : ViewModelProvider.NewInstanceFactory(){
/*override fun <T : ViewModel> create(modelClass: Class<T>): T {
    try {
        return modelClass.getConstructor(VoiceJournalRepositoryImpl::class.java)
            .newInstance(VoiceJournalRepositoryImpl.getInstance(context))
    } catch (e: InstantiationException) {
        throw RuntimeException("Cannot create an instance of $modelClass", e)
    } catch (e: IllegalAccessException) {
        throw RuntimeException("Cannot create an instance of $modelClass", e)
    }
}*/
}



/*class DataViewModelFactory(val context: Context) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        try {
            return modelClass.getConstructor(VoiceJournalRepository::class.java)
                .newInstance(VoiceJournalRepository.getInstance(context))
        } catch (e: InstantiationException) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        }
    }*/
