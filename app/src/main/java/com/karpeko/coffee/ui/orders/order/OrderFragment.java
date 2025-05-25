package com.karpeko.coffee.ui.orders.order;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.karpeko.coffee.R;
import com.karpeko.coffee.account.UserSessionManager;
import com.karpeko.coffee.ui.orders.Order;
import com.karpeko.coffee.ui.orders.OrdersAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrderFragment extends Fragment {

    RecyclerView recyclerView;
    OrdersAdapter adapter;
    UserSessionManager userSessionManager;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);

        userSessionManager = new UserSessionManager(getContext());

        recyclerView = view.findViewById(R.id.recyclerViewOrders);
        adapter = new OrdersAdapter(new ArrayList<>(), order -> {
            // Обработка клика по заказу
            Toast.makeText(getContext(), "Выбран заказ: " + order.getOrderId(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        setupOrders();

        return view;
    }

    private void setupOrders() {
        String userId = userSessionManager.getUserId();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("orders")
                .whereEqualTo("userId", userId)
                .whereIn("status", Arrays.asList("Создан", "Готовится"))
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Order> activeOrders = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = doc.toObject(Order.class);
                        activeOrders.add(order);
                    }
                    // Передать activeOrders в адаптер RecyclerView для отображения
                    adapter.setOrders(activeOrders);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Ошибка загрузки активных заказов", e);
                    Toast.makeText(getContext(), "Ошибка загрузки заказов", Toast.LENGTH_SHORT).show();
                });
    }
}