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
    private String composition;
    private String allergens;
    private boolean checked; // состояние чекбокса

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

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getAllergens() {
        return allergens;
    }

    public void setAllergens(String allergens) {
        this.allergens = allergens;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getComposition() {
        return composition;
    }

    public void setComposition(String composition) {
        this.composition = composition;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}