package com.hi.appskin_v40.model;

import com.google.firebase.firestore.FirebaseFirestore;
import com.hi.appskin_v40.BuildConfig;

import java.util.ArrayList;
import java.util.List;

public class SkinsRepository {
    public interface IDataLoaded {
        void onLoaded();
    }

    public interface IDataLoadedError {
        void onError(String error);
    }

    private static final List<Skin> items = new ArrayList<>();

    public static List<Skin> getItems() {
        return items;
    }

    public static Skin getItemById(String itemId) {
        for (Skin info : items) {
            if (info.getId().equals(itemId))
                return info;
        }
        return null;
    }

    public static void loadData(IDataLoaded success, IDataLoadedError error) {

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("mods")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    items.clear();
                    List<Skin> allskin = queryDocumentSnapshots.toObjects(Skin.class);
                    for (Skin item: allskin) {
                        if (BuildConfig.showedCategory.equalsIgnoreCase(item.getCategory()))
                            items.add(item);
                    }

                    if (success != null)
                        success.onLoaded();
                })
                .addOnFailureListener(e -> {
                    if (error != null)
                        error.onError(e.getMessage());
                });
    }
}
