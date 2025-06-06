package com.karpeko.coffee.ui.orders;

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

    // Обновлённый метод создания заказа
    public void createOrder(String userId,
                            double total,
                            List<CartItem> cartItems,
                            String deliveryType,
                            String deliveryAddress,
                            double deliveryLat,    // новый параметр
                            double deliveryLng,    // новый параметр
                            String pickupCafeId,
                            OnSuccessListener<String> listener) {

        String orderId = UUID.randomUUID().toString();

        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setStatus("Создан");
        order.setTotal(total);
        order.setCreatedAt(Timestamp.now());
        order.setCompletedAt(null);
        order.setDeliveryType(deliveryType);

        // Добавляем координаты в объект Order
        if("delivery".equals(deliveryType)) {
            order.setDeliveryAddress(deliveryAddress);
            order.setDeliveryLat(deliveryLat);
            order.setDeliveryLng(deliveryLng);
        } else {
            order.setPickupCafeId(pickupCafeId);
        }

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId);
        orderData.put("userId", userId);
        orderData.put("status", "Создан");
        orderData.put("total", total);
        orderData.put("createdAt", Timestamp.now());
        orderData.put("deliveryType", deliveryType);

        // Структурируем данные по типу доставки
        if("delivery".equals(deliveryType)) {
            orderData.put("deliveryAddress", deliveryAddress);
            orderData.put("deliveryGeo", new HashMap<String, Object>() {{
                put("lat", deliveryLat);
                put("lng", deliveryLng);
            }});
        } else {
            orderData.put("pickupCafeId", pickupCafeId);
        }

        db.collection("orders")
                .document(orderId)
                .set(orderData)
                .addOnSuccessListener(aVoid -> {
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
            orderItemData.put("itemId", cartItem.getItemId());
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
