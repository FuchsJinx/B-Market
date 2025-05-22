package com.karpeko.coffee.ui.orders.order;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karpeko.coffee.ui.orders.cart.CartItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrderWorkHelper {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Метод для создания нового заказа
    public void createOrder(String userId, double total, List<CartItem> cartItems, OnSuccessListener<String> listener) {
        String orderId = UUID.randomUUID().toString();

        // Формируем данные заказа
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId);
        orderData.put("userId", userId);
        orderData.put("status", "Готовится"); // или другой статус
        orderData.put("total", total);
        orderData.put("createdAt", Timestamp.now());
        orderData.put("completedAt", null);

        // Сохраняем заказ
        db.collection("orders")
                .document(orderId)
                .set(orderData)
                .addOnSuccessListener(aVoid -> {
                    // После успешного создания заказа добавляем позиции заказа
                    addOrderItems(orderId, cartItems);
                    if (listener != null) listener.onSuccess(orderId);
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Ошибка создания заказа", e));
    }

    // Метод для добавления позиций заказа
    private void addOrderItems(String orderId, List<CartItem> cartItems) {
        for (CartItem cartItem : cartItems) {
            String orderItemId = UUID.randomUUID().toString();
            Map<String, Object> orderItemData = new HashMap<>();
            orderItemData.put("orderItemId", orderItemId);
            orderItemData.put("orderId", orderId);
            orderItemData.put("itemId", cartItem.itemId);
            orderItemData.put("quantity", cartItem.quantity);
            orderItemData.put("customizations", cartItem.customizations);
            orderItemData.put("priceSnapshot", cartItem.getPrice()); // предполагается, что в CartItem есть поле price

            db.collection("order_items")
                    .document(orderItemId)
                    .set(orderItemData)
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Позиция заказа добавлена"))
                    .addOnFailureListener(e -> Log.e("Firestore", "Ошибка при добавлении позиции заказа", e));
        }
    }

}

