package com.karpeko.coffee.lists.item;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.karpeko.coffee.R;
import com.karpeko.coffee.ui.orders.cart.CartItem;
import com.karpeko.coffee.ui.orders.cart.CartWorkHelper;
import com.karpeko.coffee.ui.orders.favorite.FavoritesWorkHelper;
import com.karpeko.coffee.ui.orders.order.OrderWorkHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemDetailActivity extends AppCompatActivity {
    private OptionsAdapter adapter;
    RecyclerView optionsRecycler;
    ProgressBar progressBar;
    MenuItem item;
    Button addToCartButton;
    CheckBox favorite;
    String itemId;
    CartWorkHelper cartWorkHelper;
    FavoritesWorkHelper favoritesWorkHelper;

    RadioGroup radioGroupDelivery;
    EditText editTextAddress;
    Spinner spinnerCoffeeShops;
    List<String> coffeeShopNames = new ArrayList<>(); // названия кофеен для выбора
    String selectedCoffeeShop = null;


    @SuppressLint({"MissingInflatedId", "CommitPrefEdits"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        cartWorkHelper = new CartWorkHelper();
        favoritesWorkHelper = new FavoritesWorkHelper();

        itemId = getIntent().getStringExtra("item");
        if (itemId == null) {
            finish();
            return;
        }
        SharedPreferences prefs = getSharedPreferences("inFavorite", MODE_PRIVATE);

        optionsRecycler = findViewById(R.id.options_recycler);
        progressBar = findViewById(R.id.progressBar);
        addToCartButton = findViewById(R.id.add_to_cart_btn);
        favorite = findViewById(R.id.favorite);

        addToCartButton.setOnClickListener(v -> addToCart());
        String key = "favorite_" + itemId;

        boolean isFavorite = prefs.getBoolean(key, false);
        updateFavoriteCheckbox(isFavorite);

        loadItem(itemId);

        radioGroupDelivery = findViewById(R.id.radioGroupDelivery);
        editTextAddress = findViewById(R.id.editTextAddress);
        spinnerCoffeeShops = findViewById(R.id.spinnerCoffeeShops);

// Загрузите список кофеен из Firestore или задайте статически
        loadCoffeeShops();

// Обработчик выбора в RadioGroup
        radioGroupDelivery.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioDelivery) {
                editTextAddress.setVisibility(View.VISIBLE);
                spinnerCoffeeShops.setVisibility(View.GONE);
            } else if (checkedId == R.id.radioPickup) {
                editTextAddress.setVisibility(View.GONE);
                spinnerCoffeeShops.setVisibility(View.VISIBLE);
            }
        });

    }

    // Предположим, у вас есть метод обновления UI с состоянием чекбокса
    private void updateFavoriteCheckbox(boolean isFavorite) {
        SharedPreferences prefs = getSharedPreferences("inFavorite", MODE_PRIVATE);
        String key = "favorite_" + itemId;
        favorite.setOnCheckedChangeListener(null);  // отключаем слушатель
        favorite.setChecked(isFavorite);            // устанавливаем состояние
        favorite.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                addToFavorite();
                prefs.edit().putBoolean(key, true).apply();
            } else {
                removeFromFavorite();
                prefs.edit().putBoolean(key, false).apply();
            }
        });
    }


    private void loadItem(String itemId) {
        FirebaseFirestore.getInstance()
                .collection("menu")
                .document(itemId)
                .get()
                .addOnSuccessListener(document -> {
                    item = document.toObject(MenuItem.class);
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


//    private void addToCart() {
//        SharedPreferences preferences = getSharedPreferences("accountPrefs", MODE_PRIVATE);
//        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);
//        if (isLoggedIn) {
//            String userId = preferences.getString("userId", null);
//            cartWorkHelper.addOrUpdateCart(userId, Timestamp.now());
//
//            CartItem cartItem = new CartItem(item.getId() + "_" + userId, userId, itemId, convert(adapter.getSelectedOptions()));
//            cartWorkHelper.addOrUpdateCartItem(userId, cartItem, item);
//        }
//    }

    private void addToCart() {
        SharedPreferences preferences = getSharedPreferences("accountPrefs", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);
        if (!isLoggedIn) {
            Toast.makeText(this, "Войдите в аккаунт для оформления заказа", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = preferences.getString("userId", null);

        // Получаем выбранный способ доставки
        int checkedId = radioGroupDelivery.getCheckedRadioButtonId();
        String deliveryType;
        String deliveryAddress = null;
        String pickupCoffeeShop = null;

        if (checkedId == R.id.radioDelivery) {
            deliveryType = "delivery";
            deliveryAddress = editTextAddress.getText().toString().trim();
            if (deliveryAddress.isEmpty()) {
                Toast.makeText(this, "Введите адрес доставки", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (checkedId == R.id.radioPickup) {
            deliveryType = "pickup";
            if (selectedCoffeeShop == null) {
                Toast.makeText(this, "Выберите кофейню для самовывоза", Toast.LENGTH_SHORT).show();
                return;
            }
            pickupCoffeeShop = selectedCoffeeShop;
        } else {
            Toast.makeText(this, "Выберите способ получения заказа", Toast.LENGTH_SHORT).show();
            return;
        }

        cartWorkHelper.addOrUpdateCart(userId, Timestamp.now());

        CartItem cartItem = new CartItem(item.getId() + "_" + userId, userId, itemId, convert(adapter.getSelectedOptions()));

        // Передаем также информацию о способе доставки (можно добавить в CartItem или в заказ)
        cartItem.setDeliveryType(deliveryType);
        cartItem.setDeliveryAddress(deliveryAddress);
        cartItem.setPickupCoffeeShop(pickupCoffeeShop);

        cartWorkHelper.addOrUpdateCartItem(userId, cartItem, item);

        Toast.makeText(this, "Товар добавлен в корзину", Toast.LENGTH_SHORT).show();
    }


    public static HashMap<String, List<String>> convert(Map<String, String> sourceMap) {
        HashMap<String, List<String>> result = new HashMap<>();

        for (Map.Entry<String, String> entry : sourceMap.entrySet()) {
            List<String> list = new ArrayList<>();
            list.add(entry.getValue());
            result.put(entry.getKey(), list);
        }

        return result;
    }

    private void addToFavorite() {
        if (getFlagToLogin()) {
            String userId = getSaveUserId();
            // Добавить в избранное
            favoritesWorkHelper.getFavoriteId(userId, itemId, task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    String favoriteId = task.getResult().getDocuments().get(0).getString("favoriteId");
                    Toast.makeText(this,"Ваш любимый товар!", Toast.LENGTH_SHORT).show();
                }
                else {
                    favoritesWorkHelper.addFavorite(userId, itemId);
                }
            });
        }
    }

    private void removeFromFavorite() {
        if (getFlagToLogin()) {
            String userId = getSaveUserId();
            favoritesWorkHelper.getFavoriteId(userId, itemId, task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    String favoriteId = task.getResult().getDocuments().get(0).getString("favoriteId");
                    favoritesWorkHelper.removeFavorite(favoriteId);
                }
            });
        }
    }

    private boolean getFlagToLogin() {
        SharedPreferences preferences = getSharedPreferences("accountPrefs", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);
        return isLoggedIn;
    }

    private String getSaveUserId() {
        SharedPreferences preferences = getSharedPreferences("accountPrefs", MODE_PRIVATE);
        String userId = preferences.getString("userId", null);
        return userId;
    }

    private void loadCoffeeShops() {
        FirebaseFirestore.getInstance()
                .collection("coffee_shops")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    coffeeShopNames.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        if (name != null) coffeeShopNames.add(name);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, coffeeShopNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCoffeeShops.setAdapter(adapter);

                    spinnerCoffeeShops.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedCoffeeShop = coffeeShopNames.get(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedCoffeeShop = null;
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки кофеен", Toast.LENGTH_SHORT).show();
                });
    }

}