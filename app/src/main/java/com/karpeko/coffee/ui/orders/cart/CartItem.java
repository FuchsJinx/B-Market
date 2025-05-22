package com.karpeko.coffee.ui.orders.cart;

import java.util.List;
import java.util.Map;

public class CartItem {
    public String cartItemId;
    public String cartId;
    public String itemId;
    public int quantity;
    public Map<String, List<String>> customizations;
    private String deliveryType;       // "delivery" или "pickup"
    private String deliveryAddress;    // адрес доставки, если delivery
    private String pickupCoffeeShop;   // выбранная кофейня, если pickup

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
    // Конструктор, геттеры/сеттеры по необходимости
    // Геттеры и сеттеры
    public String getDeliveryType() { return deliveryType; }
    public void setDeliveryType(String deliveryType) { this.deliveryType = deliveryType; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getPickupCoffeeShop() { return pickupCoffeeShop; }
    public void setPickupCoffeeShop(String pickupCoffeeShop) { this.pickupCoffeeShop = pickupCoffeeShop; }
}


