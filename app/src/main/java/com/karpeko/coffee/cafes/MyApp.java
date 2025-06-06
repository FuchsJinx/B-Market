package com.karpeko.coffee.cafes;

import android.app.Application;

import com.yandex.mapkit.MapKitFactory;

// MyApp.java
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MapKitFactory.setApiKey("fa38298f-9b77-4fb5-9a2f-73fcd4a69960");
        MapKitFactory.initialize(this);
    }
}

