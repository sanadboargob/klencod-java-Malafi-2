package com.malafi.app;

import android.app.Application;

public class MalafiApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Theme.apply(Store.dark(this));
        Notifs.channels(this);
    }
}
