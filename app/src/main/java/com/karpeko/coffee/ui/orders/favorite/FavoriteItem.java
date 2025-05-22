package com.karpeko.coffee.ui.orders.favorite;

import com.google.firebase.Timestamp;

public class FavoriteItem {
    public String favoriteId;
    public String userId;
    public String itemId;
    public Timestamp addedAt;

    // Дополнительные поля для отображения
    public String itemName;
    public String itemPrice;
    public String itemImageUrl;

    public FavoriteItem() {} // Для Firestore

    public FavoriteItem(String favoriteId, String userId, String itemId, Timestamp addedAt) {
        this.favoriteId = favoriteId;
        this.userId = userId;
        this.itemId = itemId;
        this.addedAt = addedAt;
    }
}
