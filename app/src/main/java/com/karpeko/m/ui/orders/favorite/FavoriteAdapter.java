package com.karpeko.m.ui.orders.favorite;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;  // Для загрузки изображений
import com.karpeko.m.R;
import com.karpeko.m.account.UserSessionManager;
import com.karpeko.m.ui.menu.lists.item.MenuItem;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    private List<MenuItem> favoriteItems;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MenuItem item);
    }

    public FavoriteAdapter(List<MenuItem> favoriteItems, OnItemClickListener listener) {
        this.favoriteItems = favoriteItems;
        this.listener = listener;
    }

    public void setFavoriteItems(List<MenuItem> newItems) {
        this.favoriteItems = newItems;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        MenuItem item = favoriteItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return favoriteItems.size();
    }

    class FavoriteViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        ImageView imageViewItem;
        TextView textViewItemPrice;
        CheckBox checkBox;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewItemName);
            imageViewItem = itemView.findViewById(R.id.imageViewItem);
            textViewItemPrice = itemView.findViewById(R.id.textViewItemPrice);
            checkBox = itemView.findViewById(R.id.favoriteItemCheckbox);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(favoriteItems.get(pos));
                }
            });
        }

        public void bind(MenuItem item) {
            nameTextView.setText(item.getName());
            textViewItemPrice.setText(item.getPrice() + " руб.");

            String imageUrl = item.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(imageViewItem.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_foreground) // Заглушка
                        .error(R.drawable.ic_launcher_background) // Ошибка загрузки
                        .centerCrop()
                        .into(imageViewItem);
            } else {
                imageViewItem.setImageResource(R.drawable.ic_launcher_foreground);
            }

            // Предполагается, что в MenuItem есть поле isChecked или аналогичное
            boolean checked = item.isChecked(); // или другой метод получения состояния

            if (checked) {
                checkBox.setVisibility(View.VISIBLE);
                checkBox.setChecked(true);
            } else {
                checkBox.setVisibility(View.GONE);
            }


            checkBox.setOnCheckedChangeListener(null); // Отключаем слушатель перед установкой состояния, чтобы избежать рекурсии
            checkBox.setChecked(item.isChecked());
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setChecked(isChecked);

                UserSessionManager userSessionManager = new UserSessionManager(itemView.getContext());
                FavoritesWorkHelper favoritesHelper = new FavoritesWorkHelper();
                String userId = userSessionManager.getUserId();
                String itemId = item.getId();

                if (isChecked) {
                    // Добавляем в избранное
                    favoritesHelper.addFavorite(userId, itemId);
                } else {
                    // Удаляем из избранного — нужно получить favoriteId
                    favoritesHelper.getFavoriteId(userId, itemId, task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            String favoriteId = task.getResult().getDocuments().get(0).getString("favoriteId");
                            favoritesHelper.removeFavorite(favoriteId);
                            userSessionManager.setFavorite(itemId, false);
                        }
                    });
                }
                notifyDataSetChanged();
            });
        }
    }
}
