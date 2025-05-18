package com.karpeko.coffee.repositories;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuRepository {

    private final FirebaseFirestore db;
    private static final String MENU_COLLECTION = "menu";
    private static final String CATEGORIES_COLLECTION = "menu_categories";

    public MenuRepository() {
        db = FirebaseFirestore.getInstance();
    }

    // 1. Получение всех категорий меню
    public void getAllCategories(MenuCategoriesCallback callback) {
        db.collection(CATEGORIES_COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<MenuCategory> categories = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MenuCategory category = document.toObject(MenuCategory.class);
                            category.setId(document.getId());
                            categories.add(category);
                        }
                        callback.onSuccess(categories);
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    // 2. Получение всех items определенной категории
    public void getItemsByCategory(String categoryId, MenuItemsCallback callback) {
        db.collection(MENU_COLLECTION)
                .whereEqualTo("category", categoryId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<MenuItem> items = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MenuItem item = document.toObject(MenuItem.class);
                            item.setId(document.getId());
                            items.add(item);
                        }
                        callback.onSuccess(items);
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    // 3. Получение конкретного item по ID
    public void getMenuItemById(String itemId, MenuItemCallback callback) {
        db.collection(MENU_COLLECTION)
                .document(itemId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        MenuItem item = task.getResult().toObject(MenuItem.class);
                        if (item != null) {
                            item.setId(task.getResult().getId());
                        }
                        callback.onSuccess(item);
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    // Интерфейсы обратных вызовов
    public interface MenuCategoriesCallback {
        void onSuccess(List<MenuCategory> categories);
        void onError(Exception e);
    }

    public interface MenuItemsCallback {
        void onSuccess(List<MenuItem> items);
        void onError(Exception e);
    }

    public interface MenuItemCallback {
        void onSuccess(MenuItem item);
        void onError(Exception e);
    }

    // Модели данных
    public static class MenuCategory {
        private String id;
        private String name;
        private String imageUrl;
        private int order;

        // Геттеры и сеттеры
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public int getOrder() { return order; }
        public void setOrder(int order) { this.order = order; }
    }

    public static class MenuItem {
        private String id;
        private String name;
        private String category;
        private double price;
        private String description;
        private String imageUrl;
        private Map<String, Object> options;

        // Геттеры и сеттеры
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public Map<String, Object> getOptions() { return options; }
        public void setOptions(Map<String, Object> options) { this.options = options; }
    }
}
