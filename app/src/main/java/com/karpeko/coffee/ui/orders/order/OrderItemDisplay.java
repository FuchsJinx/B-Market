package com.karpeko.coffee.ui.orders.order;

public class OrderItemDisplay {
    private String name;
    private int price;
    private int quantity;
    private String options; // строка, например: "Топпинг: Без топпинга"

    public OrderItemDisplay(String name, int price, int quantity, String options) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.options = options;
    }

    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getOptions() { return options; }
}

