package com.karpeko.coffee.ui.orders;

import com.google.firebase.Timestamp;

public class Order {
    private String orderId;
    private String userId;
    private String status;
    private double total;
    private Timestamp createdAt;
    private Timestamp completedAt;

    private String deliveryType;        // "delivery" или "pickup"
    private String deliveryAddress;     // адрес доставки (если delivery)
    private Double deliveryLatitude;    // широта (если delivery)
    private Double deliveryLongitude;   // долгота (если delivery)
    private String pickupCoffeeShop;    // выбранная кофейня (если pickup)

    public Order() {
        // Пустой конструктор для Firestore
    }

    // Геттеры и сеттеры для всех полей

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Double getDeliveryLatitude() {
        return deliveryLatitude;
    }

    public void setDeliveryLatitude(Double deliveryLatitude) {
        this.deliveryLatitude = deliveryLatitude;
    }

    public Double getDeliveryLongitude() {
        return deliveryLongitude;
    }

    public void setDeliveryLongitude(Double deliveryLongitude) {
        this.deliveryLongitude = deliveryLongitude;
    }

    public String getPickupCoffeeShop() {
        return pickupCoffeeShop;
    }

    public void setPickupCoffeeShop(String pickupCoffeeShop) {
        this.pickupCoffeeShop = pickupCoffeeShop;
    }
}

