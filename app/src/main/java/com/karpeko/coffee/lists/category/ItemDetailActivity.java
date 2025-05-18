package com.karpeko.coffee.lists.category;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karpeko.coffee.R;

public class ItemDetailActivity extends AppCompatActivity {
    private OptionsAdapter adapter;
    RecyclerView optionsRecycler;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        String itemId = getIntent().getStringExtra("item");
        if (itemId == null) {
            finish();
            return;
        }

        optionsRecycler = findViewById(R.id.options_recycler);
        progressBar = findViewById(R.id.progressBar);

        loadItem(itemId);
    }

    private void loadItem(String itemId) {
        FirebaseFirestore.getInstance()
                .collection("menu")
                .document(itemId)
                .get()
                .addOnSuccessListener(document -> {
                    MenuItem item = document.toObject(MenuItem.class);
                    if (item != null) {
                        setupViews(item);
                    }
                });
    }

    private void setupViews(MenuItem item) {
        ImageView imageView = findViewById(R.id.item_image);
        TextView nameView = findViewById(R.id.item_name);
        TextView priceView = findViewById(R.id.item_price);

        Glide.with(this)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_launcher_foreground) // Заглушка на время загрузки
                .error(R.drawable.ic_launcher_background) // Если ошибка загрузки
                .into(imageView);

        nameView.setText(item.getName());
        priceView.setText(item.getPrice() + "₽");

        adapter = new OptionsAdapter();
        optionsRecycler.setLayoutManager(new LinearLayoutManager(this));
        optionsRecycler.setAdapter(adapter);
        adapter.setOptions(item.getOptions());
    }
}