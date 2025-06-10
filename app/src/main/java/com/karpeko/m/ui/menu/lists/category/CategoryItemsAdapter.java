package com.karpeko.m.ui.menu.lists.category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.karpeko.m.R;
import com.karpeko.m.ui.menu.lists.item.MenuItem;

import java.util.List;

import android.widget.ImageView;
import com.bumptech.glide.Glide;

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
                .inflate(R.layout.item_menu, parent, false);
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
        private final ImageView imageView;

        public CategoryItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.itemName);
            priceTextView = itemView.findViewById(R.id.itemPrice);
            imageView = itemView.findViewById(R.id.itemImage);
        }

        public void bind(MenuItem item) {
            nameTextView.setText(item.getName());
            priceTextView.setText(item.getPrice() + " руб.");

            String imageUrl = item.getImageUrl(); // Предполагается, что в MenuItem есть getImageUrl()
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(imageView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_foreground) // Заглушка
                        .error(R.drawable.ic_launcher_background) // Ошибка загрузки
                        .centerCrop()
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_foreground);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
