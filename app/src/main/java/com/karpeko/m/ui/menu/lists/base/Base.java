package com.karpeko.m.ui.menu.lists.base;

public class Base {
    private String category;
    private String imageUrl; // URL изображения категории

    public Base(String category, String imageUrl) {
        this.category = category;
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

