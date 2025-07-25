package com.karpeko.m.ui.orders.favorite;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FieldPath;
import com.karpeko.m.R;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.karpeko.m.account.UserSessionManager;
import com.karpeko.m.ui.menu.lists.item.ItemDetailActivity;
import com.karpeko.m.ui.menu.lists.item.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment implements FavoriteAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private FavoriteAdapter adapter;
    private List<MenuItem> favoriteItems = new ArrayList<>();
    private UserSessionManager userSessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        userSessionManager = new UserSessionManager(getContext());

        recyclerView = view.findViewById(R.id.recyclerViewFavorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new FavoriteAdapter(favoriteItems, this);
        recyclerView.setAdapter(adapter);

        loadOnlyFavoriteItems(userSessionManager.getUserId());

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadOnlyFavoriteItems(String userId) {
        FavoritesWorkHelper favoritesHelper = new FavoritesWorkHelper();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        favoritesHelper.getFavorites(userId, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<String> favoriteItemIds = new ArrayList<>();
                for (var doc : task.getResult().getDocuments()) {
                    String itemId = doc.getString("itemId");
                    if (itemId != null) {
                        favoriteItemIds.add(itemId);
                    }
                }

                if (favoriteItemIds.isEmpty()) {
                    // Нет избранных, очищаем список и обновляем адаптер
                    favoriteItems.clear();
                    adapter.notifyDataSetChanged();
                    return;
                }

                favoriteItems.clear();

                // Firestore ограничивает whereIn до 10 элементов
                int batchSize = 10;
                int totalBatches = (favoriteItemIds.size() + batchSize - 1) / batchSize;

                for (int i = 0; i < totalBatches; i++) {
                    int start = i * batchSize;
                    int end = Math.min(start + batchSize, favoriteItemIds.size());
                    List<String> batchIds = favoriteItemIds.subList(start, end);

                    db.collection("list")
                            .whereIn(FieldPath.documentId(), batchIds)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                for (var doc : querySnapshot.getDocuments()) {
                                    MenuItem item = doc.toObject(MenuItem.class);
                                    item.setId(doc.getId());
                                    item.setChecked(true); // т.к. это избранное
                                    favoriteItems.add(item);
                                }
                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                // Обработка ошибки загрузки
                            });
                }
            } else {
                // Обработка ошибки загрузки избранного
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onItemClick(MenuItem item) {
        Intent intent = new Intent(getContext(), ItemDetailActivity.class);
        intent.putExtra("item", item.getId());
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }
}
