package com.karpeko.coffee.ui.menu.lists.item;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.karpeko.coffee.R;
import com.karpeko.coffee.account.UserSessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ItemEditActivity extends AppCompatActivity {

    private TextView nameView, priceView, descriptionView, compositionView, allergensView;
    private ImageView imageView;
    private RecyclerView optionsRecyclerView;
    private OptionsAdapter optionsAdapter;
    private Button buttonSaveChanges;

    private String itemId;
    private String cartItemId;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    UserSessionManager userSessionManager;

    // Переданный выбранный элемент добавки (опции)
    private Map<String, String> selectedOptionsFromIntent;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_edit);
        userSessionManager = new UserSessionManager(this);

        nameView = findViewById(R.id.itemName);
        priceView = findViewById(R.id.itemPrice);
        imageView = findViewById(R.id.itemImage);
        descriptionView = findViewById(R.id.itemDescription);
        compositionView = findViewById(R.id.itemComposition);
        allergensView = findViewById(R.id.itemAllergens);
        optionsRecyclerView = findViewById(R.id.optionsRecyclerView);
        buttonSaveChanges = findViewById(R.id.buttonSaveChanges);

        optionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemId = getIntent().getStringExtra("itemId");
        cartItemId = getIntent().getStringExtra("cartItemId");
        selectedOptionsFromIntent = (Map<String, String>) getIntent().getSerializableExtra("selectedOptions");

        if (itemId == null) {
            Toast.makeText(this, "Ошибка: отсутствует ID товара", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadItemDetails(itemId);
        Log.d("ID", itemId);

        buttonSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void loadItemDetails(String itemId) {
        db.collection("menu").document(itemId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        MenuItem item = documentSnapshot.toObject(MenuItem.class);
                        if (item != null) {
                            // Заполняем UI
                            nameView.setText(item.getName());
                            priceView.setText(item.getPrice() + "₽");

                            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                                Glide.with(this)
                                        .load(item.getImageUrl())
                                        .placeholder(R.drawable.ic_launcher_foreground)
                                        .error(R.drawable.ic_launcher_background)
                                        .into(imageView);
                            } else {
                                imageView.setImageResource(R.drawable.ic_launcher_foreground);
                            }

                            descriptionView.setText(item.getDescription() != null ? item.getDescription() : "Нет описания");
                            compositionView.setText(item.getComposition() != null ? "Состав: " + item.getComposition() : "Состав не указан");
                            allergensView.setText(item.getAllergens() != null ? "Аллергены: " + item.getAllergens() : "Нет информации об аллергенах");

                            // Настраиваем адаптер опций
                            Map<String, List<String>> optionsMap = item.getOptions();
                            if (optionsMap != null && !optionsMap.isEmpty()) {
                                optionsAdapter = new OptionsAdapter(optionsMap);

                                // Если есть выбранные опции из intent, установим их в адаптер
                                if (selectedOptionsFromIntent != null && !selectedOptionsFromIntent.isEmpty()) {
                                    optionsAdapter.setSelectedOptions(selectedOptionsFromIntent);
                                }

                                optionsRecyclerView.setAdapter(optionsAdapter);
                            }
                        }
                    } else {
                        Toast.makeText(this, "Товар не найден", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки данных: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void saveChanges() {
        if (cartItemId == null) {
            Toast.makeText(this, "Ошибка: отсутствует ID элемента корзины", Toast.LENGTH_SHORT).show();
            return;
        }
        if (optionsAdapter == null) {
            Toast.makeText(this, "Ошибка: нет данных опций", Toast.LENGTH_SHORT).show();
            return;
        }

        // Получаем выбранные опции
        Map<String, String> selectedOptions = optionsAdapter.getSelectedOptions();

        // Приводим к Map<String, List<String>>, как в Firestore
        Map<String, List<String>> customizations = new HashMap<>();
        for (Map.Entry<String, String> entry : selectedOptions.entrySet()) {
            customizations.put(entry.getKey(), java.util.Collections.singletonList(entry.getValue()));
        }

        // Получите текущие значения других полей (например, price, quantity)
        // Обычно их нужно передавать в Intent или хранить в переменных класса
        int price = getIntent().getIntExtra("price", 0);
        int quantity = getIntent().getIntExtra("quantity", 1);
        String cartId = getIntent().getStringExtra("cartId");

        Map<String, Object> updatedCartItemData = new HashMap<>();
        updatedCartItemData.put("cartId", cartId);
        updatedCartItemData.put("cartItemId", cartItemId);
        updatedCartItemData.put("customizations", customizations);
        updatedCartItemData.put("itemId", itemId);
        updatedCartItemData.put("price", price);
        updatedCartItemData.put("quantity", quantity);
        updatedCartItemData.put("updatedAt", com.google.firebase.Timestamp.now());

        String userId = userSessionManager.getUserId();
        // ВАЖНО: путь должен быть carts/{userId}/cart_items/{cartItemId}
        Log.d("ItemEditActivity", "userId: " + userId);
        Log.d("ItemEditActivity", "cartItemId: " + cartItemId);
        db.collection("carts")
                .document(userId)
                .collection("cart_items")
                .document(cartItemId)
                .set(updatedCartItemData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Элемент успешно изменён", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка при обновлении элемента: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
