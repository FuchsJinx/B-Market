package com.karpeko.coffee.ui.orders.cart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
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
import com.karpeko.coffee.notification.OrderNotificationHelper;
import com.karpeko.coffee.ui.menu.lists.item.ItemDetailActivity;
import com.karpeko.coffee.ui.menu.lists.item.ItemEditActivity;
import com.karpeko.coffee.ui.orders.OrderWorkHelper;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.OnItemClickListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    RecyclerView recyclerView;
    CartAdapter adapter;
    Button buttonOrder;
    CartWorkHelper cartWorkHelper;
    UserSessionManager userSessionManager;
    CartItem cartItem;
    private TextView textTotal;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        userSessionManager = new UserSessionManager(getContext());

        cartWorkHelper = new CartWorkHelper();

        textTotal = view.findViewById(R.id.textTotal);
        buttonOrder = view.findViewById(R.id.buttonOrder);
        recyclerView = view.findViewById(R.id.recyclerViewCart);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new CartAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        loadCartItemsFromFirestore();

        buttonOrder.setOnClickListener(v -> makeOrder());

        return view;
    }

    private void loadCartItemsFromFirestore() {
        String userId = userSessionManager.getUserId();

        if (userId == null) {
            Log.w("CartFragment", "userId не найден");
            return;
        }

        db.collection("carts")
                .document(userId)
                .collection("cart_items")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("Firestore", "Listen failed.", error);
                        return;
                    }

                    List<CartItem> cartItems = new ArrayList<>();
                    double total = 0;
                    for (QueryDocumentSnapshot document : value) {
                        CartItem cartItem = document.toObject(CartItem.class);
                        cartItem.setCartItemId(document.getId()); // обязательно!
                        cartItems.add(cartItem);
                        total += cartItem.getPrice() * cartItem.quantity;
                    }
                    adapter.setCartItems(cartItems);
                    textTotal.setText("Итого: " + ((int) total) + " ₽");
                });
    }


    private void makeOrder() {
        // Ваш существующий код оформления заказа
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
                            cartItem = document.toObject(CartItem.class);
                            cartItemsInDatabase.add(cartItem);
                        }

                        if (cartItemsInDatabase.isEmpty()) {
                            Toast.makeText(getContext(), "Корзина пуста. Пожалуйста, добавьте товары.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Вычисляем total
                        double total = cartItemsInDatabase.stream().mapToDouble(item -> item.getPrice() * item.quantity).sum();

                        // Обновляем адаптер (если нужно)
                        adapter.setCartItems(cartItemsInDatabase);
                        adapter.notifyDataSetChanged();

                        // Создаём заказ
                        OrderWorkHelper orderHelper = new OrderWorkHelper();
                        orderHelper.createOrder(userId, total, cartItemsInDatabase, orderId -> {
                            new OrderNotificationHelper(getContext())
                                    .showOrderCreatedNotification(orderId, total, cartItemsInDatabase.size());

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
                        });

                    } else {
                        Log.w("Firestore", "Ошибка получения данных.", task.getException());
                        Toast.makeText(getContext(), "Ошибка загрузки корзины", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public void onItemClick(CartItem item) {
        Intent intent = new Intent(getContext(), ItemEditActivity.class);
        intent.putExtra("item", item.getItemId()); // <-- Должно быть именно item.getItemId()
        intent.putExtra("cartItemId", item.cartItemId);
        // (если нужно) intent.putExtra("selectedOptions", ...);
        intent.putExtra("price", item.getPrice());
        intent.putExtra("quantity", item.quantity);
        intent.putExtra("cartId", item.cartId);
        startActivity(intent);
    }

}
