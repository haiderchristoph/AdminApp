package com.example.canteenchecker.adminapp.core;

public class ReviewData {
    private final int ratingsOne;
    private final int ratingsTwo;
    private final int ratingsThree;
    private final int ratingsFour;
    private final int ratingsFive;
    private final int totalRatings;
    private final float averageRating;

    public ReviewData(int ratingsOne, int ratingsTwo, int ratingsThree, int ratingsFour, int ratingsFive) {
        this.ratingsOne = ratingsOne;
        this.ratingsTwo = ratingsTwo;
        this.ratingsThree = ratingsThree;
        this.ratingsFour = ratingsFour;
        this.ratingsFive = ratingsFive;
        totalRatings = ratingsOne + ratingsTwo + ratingsThree + ratingsFour + ratingsFive;
        averageRating = totalRatings == 0 ? 0 : (ratingsOne + ratingsTwo * 2 + ratingsThree * 3 + ratingsFour * 4 + ratingsFive * 5) / (float)totalRatings;
    }

    public int getRatingsOne() {
        return ratingsOne;
    }

    public int getRatingsTwo() {
        return ratingsTwo;
    }

    public int getRatingsThree() {
        return ratingsThree;
    }

    public int getRatingsFour() {
        return ratingsFour;
    }

    public int getRatingsFive() {
        return ratingsFive;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public int getTotalRatings() {
        return totalRatings;
    }
}
