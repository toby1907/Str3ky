package com.example.whatsappclonei

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class WhatsappCloneiApplication:Application()
{

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        EmojiManager.install(GoogleEmojiProvider())

    }

}