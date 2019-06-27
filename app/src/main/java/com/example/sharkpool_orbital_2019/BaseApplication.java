package com.example.sharkpool_orbital_2019;

import android.app.Application;
import com.sendbird.android.SendBird;

public class BaseApplication extends Application {
    private static final String APP_ID = "66DABAEB-1666-4113-A3BF-BDAD6D9AA990"; //App ID for Sharkpool
    //public static final String VERSION = "3.0.40"; // to be checked

    @Override
    public void onCreate() {
        super.onCreate();
        SendBird.init(APP_ID, getApplicationContext());
    }
}
