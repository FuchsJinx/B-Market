package com.karpeko.coffee.ui.menu.lists.item;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karpeko.coffee.R;
import com.karpeko.coffee.account.UserSessionManager;
import com.karpeko.coffee.ui.orders.cart.CartItem;
import com.karpeko.coffee.ui.orders.cart.CartWorkHelper;
import com.karpeko.coffee.ui.orders.favorite.FavoritesWorkHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemDetailActivity extends AppCompatActivity {

    private OptionsAdapter adapter;
    private RecyclerView optionsRecycler;
    private ProgressBar progressBar;
    private MenuItem item;
    private Button addToCartButton;
    private CheckBox favorite;
    private String itemId;

    private CartWorkHelper cartWorkHelper;
    private FavoritesWorkHelper favoritesWorkHelper;
    private UserSessionManager sessionManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        sessionManager = new UserSessionManager(this);
        cartWorkHelper = new CartWorkHelper();
        favoritesWorkHelper = new FavoritesWorkHelper();

        itemId = getIntent().getStringExtra("item");
        if (itemId == null) {
            finish();
            return;
        }

        optionsRecycler = findViewById(R.id.options_recycler);
        progressBar = findViewById(R.id.progressBar);
        addToCartButton = findViewById(R.id.add_to_cart_btn);
        favorite = findViewById(R.id.favorite);

        addToCartButton.setOnClickListener(v -> addToCart());

        setupFavoriteCheckbox();

        loadItem(itemId);
    }

    private void setupFavoriteCheckbox() {
        boolean isFavorite = sessionManager.isFavorite(itemId);

        favorite.setOnCheckedChangeListener(null); // отключаем слушатель, чтобы избежать рекурсии
        favorite.setChecked(isFavorite);

        favorite.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setFavorite(itemId, isChecked);
            if (isChecked) {
                addToFavorite();
            } else {
                removeFromFavorite();
            }
        });
    }

    private void loadItem(String itemId) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection("menu")
                .document(itemId)
                .get()
                .addOnSuccessListener(document -> {
                    progressBar.setVisibility(View.GONE);
                    item = document.toObject(MenuItem.class);
                    if (item != null) {
                        setupViews(item);
                    } else {
                        Toast.makeText(this, "Товар не найден", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Ошибка загрузки товара", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void setupViews(MenuItem item) {
        ImageView imageView = findViewById(R.id.item_image);
        TextView nameView = findViewById(R.id.item_name);
        TextView priceView = findViewById(R.id.item_price);

        Glide.with(this)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_background)
                .into(imageView);

        nameView.setText(item.getName());
        priceView.setText(item.getPrice() + "₽");

        adapter = new OptionsAdapter();
        optionsRecycler.setLayoutManager(new LinearLayoutManager(this));
        optionsRecycler.setAdapter(adapter);
        adapter.setOptions(item.getOptions());
    }

    private void addToCart() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Войдите в аккаунт для оформления заказа", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Ошибка пользователя, попробуйте войти снова", Toast.LENGTH_SHORT).show();
            return;
        }

        cartWorkHelper.addOrUpdateCart(userId, Timestamp.now());

        CartItem cartItem = new CartItem(
                item.getId() + "_" + userId,
                userId,
                itemId,
                convert(adapter.getSelectedOptions())
        );

        cartWorkHelper.addOrUpdateCartItem(userId, cartItem, item);
        Toast.makeText(this, "Товар добавлен в корзину", Toast.LENGTH_SHORT).show();
    }

    public static HashMap<String, List<String>> convert(Map<String, String> sourceMap) {
        HashMap<String, List<String>> result = new HashMap<>();
        for (Map.Entry<String, String> entry : sourceMap.entrySet()) {
            result.put(entry.getKey(), Collections.singletonList(entry.getValue()));
        }
        return result;
    }

    private void addToFavorite() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Войдите в аккаунт, чтобы добавить в избранное", Toast.LENGTH_SHORT).show();
            favorite.setChecked(false);
            return;
        }

        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Ошибка пользователя, попробуйте войти снова", Toast.LENGTH_SHORT).show();
            favorite.setChecked(false);
            return;
        }

        favoritesWorkHelper.getFavoriteId(userId, itemId, task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                Toast.makeText(this, "Ваш любимый товар!", Toast.LENGTH_SHORT).show();
            } else {
                favoritesWorkHelper.addFavorite(userId, itemId);
                sessionManager.setFavorite(itemId, true);
            }
        });
    }

    private void removeFromFavorite() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Войдите в аккаунт, чтобы удалить из избранного", Toast.LENGTH_SHORT).show();
            favorite.setChecked(true);
            return;
        }

        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Ошибка пользователя, попробуйте войти снова", Toast.LENGTH_SHORT).show();
            favorite.setChecked(true);
            return;
        }

        favoritesWorkHelper.getFavoriteId(userId, itemId, task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                String favoriteId = task.getResult().getDocuments().get(0).getString("favoriteId");
                favoritesWorkHelper.removeFavorite(favoriteId);
                sessionManager.setFavorite(itemId, false);
            }
        });
    }
}
