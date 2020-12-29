package com.example.canteenchecker.adminapp.core;

public class CanteenDetails {
    private final String id;
    private final String name;
    private final String phoneNumber;
    private final String website;
    private final String dish;
    private final float dishPrice;
    private final String location;
    private final int waitingTime;

    public CanteenDetails(String id, String name, String phoneNumber, String website, String dish, float dishPrice, String location, int waitingTime) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.website = website;
        this.dish = dish;
        this.dishPrice = dishPrice;
        this.location = location;
        this.waitingTime = waitingTime;
    }

    public String getId() { return id; }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getWebsite() {
        return website;
    }

    public String getDish() {
        return dish;
    }

    public float getDishPrice() {
        return dishPrice;
    }

    public String getLocation() {
        return location;
    }

    public int getWaitingTime() {
        return waitingTime;
    }
}
