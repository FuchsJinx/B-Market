package com.karpeko.coffee.ui.orders.cart;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);
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

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            textItemName = itemView.findViewById(R.id.textItemName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textOptions = itemView.findViewById(R.id.textOptions);
        }

        public void bind(CartItem item) {
            FirebaseFirestore.getInstance()
                    .collection("menu")
                    .document(item.getItemId())
                    .get()
                    .addOnSuccessListener(document -> {
                        MenuItem menuItem = document.toObject(MenuItem.class);
                        if (menuItem != null) {
                            textItemName.setText(menuItem.getName());
                        } else {
                            textItemName.setText("Название не найдено");
                        }
                        textQuantity.setText("Количество: " + item.quantity);

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
                        textQuantity.setText("Количество: " + item.quantity);
                        textOptions.setText("Опции отсутствуют");
                    });
        }
    }
}
