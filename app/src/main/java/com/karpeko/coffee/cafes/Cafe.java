package com.karpeko.coffee.cafes;

public class Cafe {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    // геттеры/сеттеры


    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

