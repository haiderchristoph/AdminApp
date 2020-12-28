package com.example.canteenchecker.adminapp.proxy;

import com.example.canteenchecker.adminapp.core.CanteenDetails;
import com.example.canteenchecker.adminapp.core.Review;
import com.example.canteenchecker.adminapp.core.ReviewData;

import java.io.IOException;
import java.util.Collection;

public interface ServiceProxy {
    String authenticate(String userName, String password) throws IOException;
    CanteenDetails getCanteen(String authToken) throws IOException;
    boolean updateCanteenData(String authToken, String name, String website, String phoneNumber, String address) throws IOException;
    boolean updateCanteenDish(String authToken, String dish, float dishPrice) throws IOException;
    boolean updateCanteenWaitingTime(String authToken, int waitingTime) throws IOException;
    ReviewData getCanteenReviewsData(String authToken) throws IOException;
    Collection<Review> getCanteenReviews(String authToken) throws IOException;
    boolean removeCanteenReview(String authToken, String reviewId) throws IOException;
}
