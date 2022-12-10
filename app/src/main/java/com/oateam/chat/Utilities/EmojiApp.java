package com.oateam.chat.Utilities;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

public class EmojiApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        EmojiManager.install(new IosEmojiProvider());
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
