package com.example.canteenchecker.adminapp.proxy;

import com.example.canteenchecker.adminapp.core.CanteenDetails;
import com.example.canteenchecker.adminapp.core.Review;
import com.example.canteenchecker.adminapp.core.ReviewData;

import java.io.IOException;
import java.util.Collection;

public interface ServiceProxy {
    String authenticate(String userName, String password) throws IOException;
    CanteenDetails getCanteen(String authToken) throws IOException;
    void updateCanteenData(String authToken, String name, String website, String phoneNumber, String address) throws IOException;
    void updateCanteenDish(String authToken, String dish, float dishPrice) throws IOException;
    void updateCanteenWaitingTime(String authToken, int waitingTime) throws IOException;
    ReviewData getCanteenReviewsData(String authToken) throws IOException;
    Collection<Review> getCanteenReviews(String authToken) throws IOException;
    void removeCanteenReview(String authToken, String reviewId) throws IOException;
}
