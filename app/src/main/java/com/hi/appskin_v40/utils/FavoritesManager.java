package com.hi.appskin_v40.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.hi.appskin_v40.model.Skin;

import java.util.HashSet;
import java.util.Set;

public class FavoritesManager {
    private static final String STORAGE_NAME = "FavoritesLocalStorage";
    private static final String KEY_FAVORITES = "favorites";

    private Set<String> favorites;

    private static FavoritesManager instance;
    public static FavoritesManager getInstance() {
        if (instance == null)
            instance = new FavoritesManager();

        return instance;
    }

    public boolean isFavorite(Context context, Skin skin) {
        initData(context);

        for (String item : favorites)
            if (item.equalsIgnoreCase(skin.getTitle()))
                return true;

        return false;
    }

    public void addToFavorite(Context context, Skin skin) {
        initData(context);

        if (!isFavorite(context, skin)) {
            favorites.add(skin.getTitle());
            saveToStorage(context, favorites);
        }
    }

    public void removeFavorite(Context context, Skin skin) {
        initData(context);

        if (isFavorite(context, skin)) {
            favorites.remove(skin.getTitle());
            saveToStorage(context, favorites);
        }
    }

    private void initData(Context context) {
        if (favorites == null)
            favorites = readFromStorage(context);
    }

    private static @NonNull Set<String> readFromStorage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
        Set<String> data = preferences.getStringSet(KEY_FAVORITES, new HashSet<>());

        return data.isEmpty() ? new HashSet<>() : new HashSet<>(data);
    }

    private static void saveToStorage(Context context, Set<String> values) {
        SharedPreferences preferences = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet(KEY_FAVORITES, values);

        editor.apply();
    }
}
