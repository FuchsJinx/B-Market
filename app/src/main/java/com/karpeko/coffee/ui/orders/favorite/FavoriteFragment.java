package com.karpeko.coffee.ui.orders.favorite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.karpeko.coffee.R;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.karpeko.coffee.account.UserSessionManager;
import com.karpeko.coffee.ui.menu.lists.item.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoriteFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    RecyclerView recyclerView;
    FavoriteAdapter adapter;
    UserSessionManager userSessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        userSessionManager = new UserSessionManager(getContext());

        recyclerView = view.findViewById(R.id.recyclerViewFavorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new FavoriteAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        loadFavoriteItemsFromFirestore();

        return view;
    }

    private void loadFavoriteItemsFromFirestore() {
        String userId = userSessionManager.getUserId();

        if (userId == null) {
            Log.w("FavoritesFragment", "userId не найден в SharedPreferences");
            return;
        }

        db.collection("favorites")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<FavoriteItem> favoriteItems = new ArrayList<>();
                        List<String> itemIds = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            FavoriteItem favorite = document.toObject(FavoriteItem.class);
                            favoriteItems.add(favorite);
                            itemIds.add(favorite.itemId);
                        }

                        if (itemIds.isEmpty()) {
                            adapter.setFavoriteItems(new ArrayList<>());
                            adapter.notifyDataSetChanged();
                            return;
                        }

                        Map<String, MenuItem> menuItemsMap = new HashMap<>();
                        final int totalItems = itemIds.size();
                        final int[] processedCount = {0};

                        for (String itemId : itemIds) {
                            db.collection("menu")
                                    .document(itemId)
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            MenuItem menuItem = documentSnapshot.toObject(MenuItem.class);
                                            menuItemsMap.put(itemId, menuItem);
                                        } else {
                                            Log.w("Firestore", "Документ menu с ID " + itemId + " не найден");
                                        }

                                        processedCount[0]++;
                                        if (processedCount[0] == totalItems) {
                                            // Все документы получены, объединяем данные
                                            List<FavoriteItem> enrichedFavorites = new ArrayList<>();
                                            for (FavoriteItem favorite : favoriteItems) {
                                                MenuItem menuItem = menuItemsMap.get(favorite.itemId);
                                                if (menuItem != null) {
                                                    favorite.itemName = menuItem.getName();
                                                    favorite.itemPrice = String.valueOf(menuItem.getPrice());
                                                    favorite.itemImageUrl = menuItem.getImageUrl();
                                                }
                                                enrichedFavorites.add(favorite);
                                            }
                                            adapter.setFavoriteItems(enrichedFavorites);
                                            adapter.notifyDataSetChanged();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w("Firestore", "Ошибка получения документа menu с ID " + itemId, e);
                                        processedCount[0]++;
                                        if (processedCount[0] == totalItems) {
                                            // Обработка после всех запросов, даже если были ошибки
                                            List<FavoriteItem> enrichedFavorites = new ArrayList<>();
                                            for (FavoriteItem favorite : favoriteItems) {
                                                MenuItem menuItem = menuItemsMap.get(favorite.itemId);
                                                if (menuItem != null) {
                                                    favorite.itemName = menuItem.getName();
                                                    favorite.itemPrice = String.valueOf(menuItem.getPrice());
                                                    favorite.itemImageUrl = menuItem.getImageUrl();
                                                }
                                                enrichedFavorites.add(favorite);
                                            }
                                            adapter.setFavoriteItems(enrichedFavorites);
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                        }

                    } else {
                        Log.w("Firestore", "Ошибка получения избранного.", task.getException());
                    }
                });
    }
}
