package com.karpeko.coffee.ui.menu.lists.item;

import java.util.List;
import java.util.Map;

public class MenuItem {

    private String id;
    private String name;
    private String category;
    private String description;
    private int price;
    private Map<String, List<String>> options; // Дополнительные параметры
    private String imageUrl;
    private String optionsName;
    private String composition;
    private String allergens;
    private boolean checked;

    public MenuItem() {}

    // Геттеры и сеттеры

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public Map<String, List<String>> getOptions() { return options; }
    public void setOptions(Map<String, List<String>> options) { this.options = options; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getOptionsName() { return optionsName; }
    public void setOptionsName(String optionsName) { this.optionsName = optionsName; }

    public String getComposition() { return composition; }
    public void setComposition(String composition) { this.composition = composition; }

    public String getAllergens() { return allergens; }
    public void setAllergens(String allergens) { this.allergens = allergens; }

    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }
}
