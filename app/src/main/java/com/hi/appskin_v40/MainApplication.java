package com.hi.appskin_v40;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/PIXELADE.TTF")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

        MobileAds.initialize(this, initializationStatus -> {});
        FirebaseApp.initializeApp(this);
    }
}
