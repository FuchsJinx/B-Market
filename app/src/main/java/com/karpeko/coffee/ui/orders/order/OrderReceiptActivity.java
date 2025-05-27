package com.karpeko.coffee.ui.orders.order;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.karpeko.coffee.R;
import com.karpeko.coffee.ui.menu.lists.item.MenuItem;
import com.karpeko.coffee.ui.orders.Order;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderReceiptActivity extends AppCompatActivity {

    private TextView textOrderNumber, textOrderStatus, textOrderDate, textOrderType, textOrderAddress, textOrderTotal;
    private RecyclerView recyclerOrderItems;
    private OrderItemAdapter orderItemAdapter;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_receipt);

        textOrderNumber = findViewById(R.id.textOrderNumber);
        textOrderStatus = findViewById(R.id.textOrderStatus);
        textOrderDate = findViewById(R.id.textOrderDate);
        textOrderType = findViewById(R.id.textOrderType);
        textOrderAddress = findViewById(R.id.textOrderAddress);
        textOrderTotal = findViewById(R.id.textOrderTotal);
        recyclerOrderItems = findViewById(R.id.recyclerOrderItems);

        recyclerOrderItems.setLayoutManager(new LinearLayoutManager(this));
        orderItemAdapter = new OrderItemAdapter(new ArrayList<>());
        recyclerOrderItems.setAdapter(orderItemAdapter);

        String orderId = getIntent().getStringExtra("orderId");
        String userId = getIntent().getStringExtra("userId"); // если нужно

        if (orderId == null || userId == null) {
            Toast.makeText(this, "Ошибка: не передан orderId или userId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadOrderInfo(userId, orderId);
    }

    private void loadOrderInfo(String userId, String orderId) {
        db.collection("orders")
                .document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Order order = documentSnapshot.toObject(Order.class);
                        if (order != null) {
                            textOrderNumber.setText("Заказ №" + order.getOrderId());
                            textOrderStatus.setText("Статус: " + order.getStatus());
                            textOrderTotal.setText("Итого: " + ((int) order.getTotal()) + " ₽");

                            Timestamp createdAt = order.getCreatedAt();
                            if (createdAt != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                                textOrderDate.setText("Дата: " + sdf.format(createdAt.toDate()));
                            } else {
                                textOrderDate.setText("Дата: -");
                            }

                            if ("delivery".equals(order.getDeliveryType())) {
                                textOrderType.setText("Тип: Доставка");
                                textOrderAddress.setText("Адрес: " + order.getDeliveryAddress());
                            } else {
                                textOrderType.setText("Тип: Самовывоз");
                                textOrderAddress.setText("Кофейня: " + order.getPickupCoffeeShop());
                            }

                            loadOrderItemsByOrderId(orderId);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки заказа: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadOrderItemsByOrderId(String orderId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("order_items")
                .whereEqualTo("orderId", orderId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<OrderItemDisplay> displayList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String itemId = doc.getString("itemId");
                        Long priceLong = doc.getLong("priceSnapshot");
                        int price = priceLong != null ? priceLong.intValue() : 0;

                        Long quantityLong = doc.getLong("quantity");
                        int quantity = quantityLong != null ? quantityLong.intValue() : 0;

                        Map<String, List<String>> customizations = (Map<String, List<String>>) doc.get("customizations");

                        // Получаем название товара по itemId (если нужно)
                        db.collection("menu")
                                .document(itemId)
                                .get()
                                .addOnSuccessListener(menuDoc -> {
                                    String name = "Товар";
                                    if (menuDoc.exists()) {
                                        MenuItem menuItem = menuDoc.toObject(MenuItem.class);
                                        if (menuItem != null) {
                                            name = menuItem.getName();
                                        }
                                    }
                                    // Формируем строку опций
                                    StringBuilder optionsStr = new StringBuilder();
                                    if (customizations != null && !customizations.isEmpty()) {
                                        for (Map.Entry<String, List<String>> entry : customizations.entrySet()) {
                                            optionsStr.append(entry.getKey()).append(": ");
                                            optionsStr.append(android.text.TextUtils.join(", ", entry.getValue())).append("; ");
                                        }
                                    }
                                    displayList.add(new OrderItemDisplay(name, price, quantity, optionsStr.toString()));
                                    // Обновляем адаптер после добавления каждого элемента (или после цикла)
                                     orderItemAdapter.notifyDataSetChanged();
                                });
                    }
                    // После окончания цикла можно обновить адаптер (лучше, чем по одному)
                    orderItemAdapter = new OrderItemAdapter(displayList);
                    recyclerOrderItems.setAdapter(orderItemAdapter);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки товаров заказа", Toast.LENGTH_SHORT).show());
    }

}

