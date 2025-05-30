package com.karpeko.coffee.ui.orders.cart;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karpeko.coffee.R;
import com.karpeko.coffee.ui.menu.lists.item.MenuItem;

import java.util.List;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CartItem item);
    }

    public CartAdapter(List<CartItem> cartItems, OnItemClickListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    public void setCartItems(List<CartItem> newItems) {
        this.cartItems = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && position != RecyclerView.NO_POSITION) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {

        TextView textItemName, textQuantity, textOptions;
        Button buttonIncrease, buttonDecrease, buttonDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            textItemName = itemView.findViewById(R.id.textItemName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textOptions = itemView.findViewById(R.id.textOptions);

            buttonIncrease = itemView.findViewById(R.id.buttonIncrease);
            buttonDecrease = itemView.findViewById(R.id.buttonDecrease);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }

        public void bind(CartItem item) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("menu")
                    .document(item.getItemId())
                    .get()
                    .addOnSuccessListener(document -> {
                        MenuItem menuItem = document.toObject(MenuItem.class);
                        if (menuItem != null) {
                            textItemName.setText(menuItem.getName());
                        } else {
                            textItemName.setText("Название не найдено");
                        }
                        textQuantity.setText(String.valueOf(item.quantity));

                        if (item.customizations != null && !item.customizations.isEmpty()) {
                            StringBuilder optionsBuilder = new StringBuilder("Опции: ");
                            for (Map.Entry<String, List<String>> entry : item.customizations.entrySet()) {
                                optionsBuilder.append(entry.getKey())
                                        .append(": ")
                                        .append(TextUtils.join(", ", entry.getValue()))
                                        .append("; ");
                            }
                            textOptions.setText(optionsBuilder.toString());
                        } else {
                            textOptions.setText("Опции отсутствуют");
                        }
                    })
                    .addOnFailureListener(e -> {
                        textItemName.setText("Ошибка загрузки названия");
                        textQuantity.setText(String.valueOf(item.quantity));
                        textOptions.setText("Опции отсутствуют");
                    });

            // Обработка кнопок
            buttonIncrease.setOnClickListener(v -> {
                updateQuantity(item, 1);
            });

            buttonDecrease.setOnClickListener(v -> {
                if (item.quantity > 1) {
                    updateQuantity(item, -1);
                } else {
                    deleteItem(item);
                }
            });

            buttonDelete.setOnClickListener(v -> {
                deleteItem(item);
            });
        }

        private void updateQuantity(CartItem item, int delta) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Предполагается, что у CartItem есть поле cartItemId и cartId (userId)
            DocumentReference itemRef = db.collection("carts")
                    .document(item.cartId)
                    .collection("cart_items")
                    .document(item.cartItemId);

            itemRef.update("quantity", FieldValue.increment(delta))
                    .addOnSuccessListener(aVoid -> Log.d("CartAdapter", "Quantity updated"))
                    .addOnFailureListener(e -> Log.e("CartAdapter", "Failed to update quantity", e));
        }

        private void deleteItem(CartItem item) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            DocumentReference itemRef = db.collection("carts")
                    .document(item.cartId)
                    .collection("cart_items")
                    .document(item.cartItemId);

            itemRef.delete()
                    .addOnSuccessListener(aVoid -> Log.d("CartAdapter", "Item deleted"))
                    .addOnFailureListener(e -> Log.e("CartAdapter", "Failed to delete item", e));
        }
    }

}
