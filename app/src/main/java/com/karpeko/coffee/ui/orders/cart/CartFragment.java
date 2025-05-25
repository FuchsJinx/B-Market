package com.karpeko.coffee.ui.orders.cart;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.karpeko.coffee.R;
import com.karpeko.coffee.account.UserSessionManager;
import com.karpeko.coffee.ui.orders.OrderWorkHelper;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    RecyclerView recyclerView;
    CartAdapter adapter;
    Button buttonOrder;
    CartWorkHelper cartWorkHelper;
    UserSessionManager userSessionManager;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        userSessionManager = new UserSessionManager(getContext());

        cartWorkHelper = new CartWorkHelper();

        buttonOrder = view.findViewById(R.id.buttonOrder);
        recyclerView = view.findViewById(R.id.recyclerViewCart);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Инициализируем адаптер пустым списком
        adapter = new CartAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Загружаем данные из Firestore
        loadCartItemsFromFirestore();

        buttonOrder.setOnClickListener(v -> makeOrder());

        return view;
    }

    private void loadCartItemsFromFirestore() {
        String userId = userSessionManager.getUserId();

        if (userId == null) {
            Log.w("CartFragment", "userId не найден в SharedPreferences");
            return;
        }

        db.collection("carts")
                .document(userId)
                .collection("cart_items")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<CartItem> cartItems = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            CartItem item = document.toObject(CartItem.class);
                            cartItems.add(item);
                        }
                        // Обновляем данные в адаптере
                        adapter.setCartItems(cartItems);
                    } else {
                        Log.w("Firestore", "Ошибка получения данных.", task.getException());
                    }
                });
    }

    private void makeOrder() {
        boolean isLoggedIn = userSessionManager.isLoggedIn();
        if (!isLoggedIn) {
            Toast.makeText(getContext(), "Войдите в аккаунт для оформления заказа", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = userSessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(getContext(), "Пользователь не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("carts")
                .document(userId)
                .collection("cart_items")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<CartItem> cartItemsInDatabase = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            CartItem item = document.toObject(CartItem.class);
                            cartItemsInDatabase.add(item);
                        }

                        if (cartItemsInDatabase.isEmpty()) {
                            Toast.makeText(getContext(), "Корзина пуста. Пожалуйста, добавьте товары.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Вычисляем total
                        double total = 0;
                        for (CartItem item : cartItemsInDatabase) {
                            total += item.getPrice() * item.quantity;
                        }

                        // Обновляем адаптер (если нужно)
                        adapter.setCartItems(cartItemsInDatabase);
                        adapter.notifyDataSetChanged();

                        // Создаём заказ
                        OrderWorkHelper orderHelper = new OrderWorkHelper();
                        orderHelper.createOrder(userId, total, cartItemsInDatabase, orderId -> {
                            Toast.makeText(getContext(), "Заказ оформлен! Номер заказа: " + orderId, Toast.LENGTH_LONG).show();
                            // Очистка корзины после успешного заказа
                            cartWorkHelper.clearCart(userId, new CartWorkHelper.OnCartClearedListener() {
                                @SuppressLint("NotifyDataSetChanged")
                                @Override
                                public void onCartCleared() {
                                    // Корзина успешно очищена — обновляем UI
                                    adapter.setCartItems(new ArrayList<>());
                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Log.e("CartClear", "Ошибка очистки корзины", e);
                                }
                            });
                            requireActivity().finish(); //TODO: придумать получше вариант
                        });

                    } else {
                        Log.w("Firestore", "Ошибка получения данных.", task.getException());
                        Toast.makeText(getContext(), "Ошибка загрузки корзины", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
