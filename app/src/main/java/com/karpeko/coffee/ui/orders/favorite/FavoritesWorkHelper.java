package com.karpeko.coffee.ui.orders.favorite;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.karpeko.coffee.ui.menu.lists.item.MenuItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FavoritesWorkHelper {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Добавить элемент в избранное
    public void addFavorite(String userId, String itemId) {
        String favoriteId = UUID.randomUUID().toString();
        Map<String, Object> favoriteData = new HashMap<>();
        favoriteData.put("favoriteId", favoriteId);
        favoriteData.put("userId", userId);
        favoriteData.put("itemId", itemId);
        favoriteData.put("addedAt", Timestamp.now());

        db.collection("favorites")
                .document(favoriteId)
                .set(favoriteData)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Добавлено в избранное"))
                .addOnFailureListener(e -> Log.e("Firestore", "Ошибка при добавлении в избранное", e));
    }

    // Удалить элемент из избранного
    public void removeFavorite(String favoriteId) {
        db.collection("favorites")
                .document(favoriteId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Удалено из избранного"))
                .addOnFailureListener(e -> Log.e("Firestore", "Ошибка при удалении из избранного", e));
    }

    // Проверить, есть ли товар в избранном у пользователя
    public void isFavorite(String userId, String itemId, OnCompleteListener<QuerySnapshot> listener) {
        db.collection("favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("itemId", itemId)
                .get()
                .addOnCompleteListener(listener);
    }

    // Получить все избранные товары пользователя
    public void getFavorites(String userId, OnCompleteListener<QuerySnapshot> listener) {
        db.collection("favorites")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(listener);
    }

    public void getFavoriteId(String userId, String itemId, OnCompleteListener<QuerySnapshot> listener) {
        db.collection("favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("itemId", itemId)
                .get()
                .addOnCompleteListener(listener);
    }
}

