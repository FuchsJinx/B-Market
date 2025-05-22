package com.karpeko.coffee.ui.orders.cart;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.karpeko.coffee.R;
import com.karpeko.coffee.ui.orders.order.OrderWorkHelper;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    RecyclerView recyclerView;
    CartAdapter adapter;
    Button buttonOrder;
    CartWorkHelper cartWorkHelper;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        cartWorkHelper = new CartWorkHelper();

        buttonOrder = view.findViewById(R.id.buttonOrder);
        recyclerView = view.findViewById(R.id.recyclerViewCart);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Инициализируем адаптер пустым списком
        adapter = new CartAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Загружаем данные из Firestore
        loadCartItemsFromFirestore();

        SharedPreferences preferences = requireActivity().getSharedPreferences("accountPrefs", MODE_PRIVATE);

        buttonOrder.setOnClickListener(v -> {
            makeOrder();
//            cartWorkHelper.clearCart(preferences.getString("userId", null));
//            adapter.setCartItems(new ArrayList<>());
//            NavController navController = Navigation.findNavController(view);
//            navController.navigate(navController.getCurrentDestination().getId());
        });

        return view;
    }

    private void loadCartItemsFromFirestore() {
        SharedPreferences preferences = requireActivity().getSharedPreferences("accountPrefs", MODE_PRIVATE);
        String userId = preferences.getString("userId", null);

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

    private List<CartItem> getCartItemsFromDatabase() {


        SharedPreferences preferences = requireActivity().getSharedPreferences("accountPrefs", MODE_PRIVATE);
        String userId = preferences.getString("userId", null);
        List<CartItem> cartItems = new ArrayList<>();;
        db.collection("carts")
                .document(userId)  // или cartId, в зависимости от структуры
                .collection("cart_items")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            CartItem item = document.toObject(CartItem.class);
                            cartItems.add(item);
                        }
                        // Передайте cartItems в адаптер RecyclerView для отображения
                        updateRecyclerView(cartItems);
                    } else {
                        Log.w("Firestore", "Ошибка получения данных.", task.getException());
                    }
                });

        return cartItems;
    }

    private void updateRecyclerView(List<CartItem> cartItems) {
        adapter = new CartAdapter(cartItems);
        recyclerView.setAdapter(adapter);
    }

    private void makeOrder() {
        SharedPreferences preferences = requireActivity().getSharedPreferences("accountPrefs", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);
        if (!isLoggedIn) {
            Toast.makeText(getContext(), "Войдите в аккаунт для оформления заказа", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = preferences.getString("userId", null);
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
                                    // TODO: реализовать переход на другое активити, чтобы скрыть пробелы
                                    Toast.makeText(getContext(), "Корзина очищена", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(getContext(), "Ошибка при очистке корзины: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
}
