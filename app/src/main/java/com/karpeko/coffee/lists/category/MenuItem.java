package com.karpeko.coffee.lists.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MenuItem {

    String id;
    private String name;
    private String category;
    private String description;
    private int price;
    private Map<String, List<String>> options; // Точное соответствие Firestore
    private String imageUrl; // Добавьте это поле
    private String optionsName;


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