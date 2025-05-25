package com.karpeko.coffee.ui.orders.cart;

import java.util.List;
import java.util.Map;

public class CartItem {
    public String cartItemId;
    public String cartId;
    public String itemId;
    public int quantity;
    public Map<String, List<String>> customizations;

    private int price;

    public CartItem() {} // Пустой конструктор для Firestore
    public CartItem(String cartItemId, String cartId, String itemId, Map<String, List<String>> customizations) {
        this.cartItemId = cartItemId;
        this.cartId = cartId;
        this.itemId = itemId;
        this.quantity = 1;
        this.customizations = customizations;
    }

    public void setPrice(int price) {this.price = price;}

    public int getPrice() {
        return price;
    }
}


