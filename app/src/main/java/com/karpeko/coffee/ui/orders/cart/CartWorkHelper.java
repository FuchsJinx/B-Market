package com.karpeko.coffee.ui.orders.cart;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.karpeko.coffee.ui.menu.lists.item.MenuItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CartWorkHelper {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addOrUpdateCart(String userId, Timestamp lastUpdated) {
        Map<String, Object> cartData = new HashMap<>();
        cartData.put("userId", userId);
        cartData.put("lastUpdated", lastUpdated);

        db.collection("carts")
                .document(userId)
                .set(cartData)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Корзина обновлена"))
                .addOnFailureListener(e -> Log.e("Firestore", "Ошибка при обновлении корзины", e));
    }

    public void addOrUpdateCartItem(String userId, CartItem item, MenuItem menuItem) {
        // Ссылка на подколлекцию cart_items внутри документа корзины
        CollectionReference itemsRef = db.collection("carts")
                .document(userId)
                .collection("cart_items");

        // Проверяем, есть ли такой товар с такими же опциями
        itemsRef.whereEqualTo("itemId", item.getItemId())
                .whereEqualTo("customizations", item.customizations)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Такой товар уже есть, увеличиваем количество
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        long currentQty = doc.getLong("quantity") != null ? doc.getLong("quantity") : 0;
                        doc.getReference().update("quantity", currentQty + 1);
                    } else {
                        // Добавляем новый товар
                        String cartItemId = UUID.randomUUID().toString();
                        Map<String, Object> itemData = new HashMap<>();
                        itemData.put("cartItemId", cartItemId);
                        itemData.put("cartId", userId);
                        itemData.put("itemId", item.getItemId());
                        itemData.put("quantity", 1);
                        itemData.put("customizations", item.customizations);
                        itemData.put("price", menuItem.getPrice());

                        itemsRef.document(cartItemId).set(itemData);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Ошибка при добавлении товара", e));
    }

    public void getCartItems(String userId, OnCompleteListener<QuerySnapshot> listener) {
        db.collection("carts")
                .document(userId)
                .collection("cart_items")
                .get()
                .addOnCompleteListener(listener);
    }


    public void clearCart(String userId, OnCartClearedListener listener) {
        CollectionReference itemsRef = db.collection("carts")
                .document(userId)
                .collection("cart_items");

        itemsRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("Firestore", "Корзина уже пуста");
                        return;
                    }

                    // Создаем батч
                    WriteBatch batch = db.batch();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }

                    // Коммитим батч
                    batch.commit()
                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Корзина успешно очищена"))
                            .addOnFailureListener(e -> Log.e("Firestore", "Ошибка при очистке корзины", e));
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Ошибка при получении товаров корзины для очистки", e));
    }

    public interface OnCartClearedListener {
        void onCartCleared();
        void onFailure(Exception e);
    }


}
