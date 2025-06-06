package com.karpeko.coffee.ui.orders;

import com.google.firebase.Timestamp;

public class Order {
    private String orderId;
    private String userId;
    private String status;
    private double total;
    private Timestamp createdAt;
    private Timestamp completedAt;

    private String deliveryType; // "delivery" или "pickup"
    private String deliveryAddress;
    private String pickupCafeId;

    private double deliveryLat;
    private double deliveryLng;

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

    public void setPickupCafeId(String pickupCafeId) {
        this.pickupCafeId = pickupCafeId;
    }

    public String getPickupCafeId() {
        return pickupCafeId;
    }

    public double getDeliveryLat() { return deliveryLat; }
    public void setDeliveryLat(double deliveryLat) { this.deliveryLat = deliveryLat; }

    public double getDeliveryLng() { return deliveryLng; }
    public void setDeliveryLng(double deliveryLng) { this.deliveryLng = deliveryLng; }
}

