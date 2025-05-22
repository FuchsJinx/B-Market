package com.karpeko.coffee.lists.category;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.karpeko.coffee.R;
import com.karpeko.coffee.lists.item.ItemDetailActivity;
import com.karpeko.coffee.lists.item.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CategoryActivity extends AppCompatActivity implements CategoryItemsAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private CategoryItemsAdapter adapter;
    private final List<MenuItem> itemList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // Получаем название категории из Intent
        String categoryName = getIntent().getStringExtra("category");
        if (categoryName == null || categoryName.isEmpty()) {
            showErrorAndFinish("Не указана категория");
            return;
        }

        setTitle(categoryName);

        initializeViews();
        loadCategoryItems(categoryName);
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        // Настройка RecyclerView один раз при инициализации
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryItemsAdapter(this, itemList, item -> {
            Intent intent = new Intent(this, ItemDetailActivity.class);
            intent.putExtra("item", item.getId()); // Передаем весь объект
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadCategoryItems(@NonNull String categoryName) {
        showLoading(true);

        db.collection("menu")
                .whereEqualTo("category", categoryName)
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        itemList.clear();
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            MenuItem item = document.toObject(MenuItem.class);
                            item.setId(document.getId());
                            itemList.add(item);
                            Log.d("tag", "get data");
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        showError("Ошибка загрузки данных: " +
                                Objects.requireNonNull(task.getException()).getMessage());
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showError("Ошибка: " + e.getMessage());
                });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showErrorAndFinish(String message) {
        showError(message);
        finish();
    }

    @Override
    public void onItemClick(MenuItem item) {
        Intent intent = new Intent(this, ItemDetailActivity.class);
        intent.putExtra("item", item.getId()); // Передаем весь объект
        startActivity(intent);
    }
}