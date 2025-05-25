package com.karpeko.coffee.ui.orders.favorite;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;  // Для загрузки изображений
import com.karpeko.coffee.R;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    private List<FavoriteItem> favoriteItems;

    public FavoriteAdapter(List<FavoriteItem> favoriteItems) {
        this.favoriteItems = favoriteItems;
    }

    public void setFavoriteItems(List<FavoriteItem> favoriteItems) {
        this.favoriteItems = favoriteItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        FavoriteItem item = favoriteItems.get(position);

        holder.textViewItemName.setText(item.itemName != null ? item.itemName : "Название отсутствует");
        holder.textViewItemPrice.setText(item.itemPrice != null ? "₽ " + item.itemPrice : "Цена отсутствует");

        // Загрузка изображения через Glide, если есть URL
        if (item.itemImageUrl != null && !item.itemImageUrl.isEmpty()) {
            Glide.with(holder.imageViewItem.getContext())
                    .load(item.itemImageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground) // плейсхолдер
                    .into(holder.imageViewItem);
        } else {
            holder.imageViewItem.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    @Override
    public int getItemCount() {
        return favoriteItems != null ? favoriteItems.size() : 0;
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewItem;
        TextView textViewItemName;
        TextView textViewItemPrice;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewItem = itemView.findViewById(R.id.imageViewItem);
            textViewItemName = itemView.findViewById(R.id.textViewItemName);
            textViewItemPrice = itemView.findViewById(R.id.textViewItemPrice);
        }
    }
}
