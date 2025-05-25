package com.karpeko.coffee.ui.menu.lists.category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.karpeko.coffee.R;
import com.karpeko.coffee.ui.menu.lists.item.MenuItem;

import java.util.List;

public class CategoryItemsAdapter extends RecyclerView.Adapter<CategoryItemsAdapter.CategoryItemViewHolder> {

    private final List<MenuItem> items;
    private OnItemClickListener listener;

    public CategoryItemsAdapter(List<MenuItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(MenuItem item);
    }

    @NonNull
    @Override
    public CategoryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.menu_item, parent, false);
        return new CategoryItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryItemViewHolder holder, int position) {
        MenuItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class CategoryItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView priceTextView;

        public CategoryItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.itemName);
            priceTextView = itemView.findViewById(R.id.itemPrice);
        }

        public void bind(MenuItem item) {
            nameTextView.setText(item.getName());
            priceTextView.setText(item.getPrice() + " руб.");

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}