package com.hi.appskin_v40.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.hi.appskin_v40.model.Skin;

public class LocalStorage {
    private static final String PREFERENCES_FILE = "preferences_file";
    private static final String KEY_OPENS_WITHOUT_ADD = "opens_without_ad";
    private static final String KEY_DONT_SHOW_AGAIN = "don`t_show_again";


    public static long getIdForModInfo(Context context, Skin modInfo) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return preferences.getLong(modInfo.generateKey(), 0);
    }

    public static void saveDownloadId(Context context, Skin modInfo, long id) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putLong(modInfo.generateKey(), id);
        editor.apply();
    }

    public static void setOpensWithoutAd(Context context, int count) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(KEY_OPENS_WITHOUT_ADD, count);
        editor.apply();
    }

    public static int getOpensWithoutAd(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return preferences.getInt(KEY_OPENS_WITHOUT_ADD, 0);
    }

    public static void setNeverShowRateDialogAgain(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(KEY_DONT_SHOW_AGAIN, true);
        editor.apply();
    }

    public static boolean isShowRateDialogAgain(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return !preferences.getBoolean(KEY_DONT_SHOW_AGAIN, false);
    }
}
