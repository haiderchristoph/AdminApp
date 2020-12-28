package com.example.canteenchecker.adminapp.core;

public class Review {
    private final String id;
    private final String creationDate;
    private final String creator;
    private final int rating;
    private final String remark;

    public Review(String id, String creationDate, String creator, int rating, String remark) {
        this.id = id;
        this.creationDate = creationDate;
        this.creator = creator;
        this.rating = rating;
        this.remark = remark;
    }

    public String getId() {
        return id;
    }
    public String getCreationDate() {
        return creationDate;
    }
    public String getCreator() {
        return creator;
    }
    public String getRemark() {
        return remark;
    }
    public int getRating() {
        return rating;
    }
}
