package com.karpeko.coffee.ui.menu.lists.item;

import java.util.List;
import java.util.Map;

public class MenuItem {

    String id;
    public String name;
    private String category;
    private String description;
    public int price;
    private Map<String, List<String>> options; // Точное соответствие Firestore
    public String imageUrl; // Добавьте это поле
    private String optionsName;

    public MenuItem() {}


    public String getImageUrl() {
        return imageUrl;
    }

    // Геттеры
    public String getName() { return name; }
    public Map<String, List<String>> getOptions() { return options; }
    public int getPrice() { return price; }
    // ... остальные геттеры
    public String getOptionsName() {
        return optionsName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}