package com.example.canteenchecker.adminapp.proxy;

import com.example.canteenchecker.adminapp.core.CanteenDetails;
import com.example.canteenchecker.adminapp.core.Review;
import com.example.canteenchecker.adminapp.core.ReviewData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
public class ServiceProxyImpl implements ServiceProxy {
    private static final String SERVICE_BASE_URL = "https://moc5.projekte.fh-hagenberg.at/CanteenChecker/api/admin/";

    private final Proxy proxy = new Retrofit.Builder()
            .baseUrl(SERVICE_BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Proxy.class);

    @Override
    public String authenticate(String userName, String password) throws IOException {
        return proxy.authenticate(userName, password).execute().body();
    }

    @Override
    public CanteenDetails getCanteen(String authToken) throws IOException {
        Proxy_CanteenDetails canteen = proxy.getCanteen(getFormattedBearerToken(authToken)).execute().body();
        return canteen != null ? canteen.toCanteenDetails() : null;
    }

    @Override
    public void updateCanteenData(String authToken, String name, String website, String phoneNumber, String address) throws IOException {
        proxy.updateCanteenData(getFormattedBearerToken(authToken), name, website, phoneNumber, address).execute().headers();
    }

    @Override
    public void updateCanteenDish(String authToken, String dish, float dishPrice) throws IOException {
        proxy.updateCanteenDish(getFormattedBearerToken(authToken), dish, dishPrice).execute().body();
    }

    @Override
    public void updateCanteenWaitingTime(String authToken, int waitingTime) throws IOException {
        proxy.updateCanteenWaitingTime(getFormattedBearerToken(authToken), waitingTime).execute().body();
    }

    @Override
    public ReviewData getCanteenReviewsData(String authToken) throws IOException {
        Proxy_CanteenReviewStatistics reviewData = proxy.getCanteenReviewStatistics(getFormattedBearerToken(authToken)).execute().body();
        return reviewData != null ? reviewData.toReviewData() : null;
    }

    @Override
    public Collection<Review> getCanteenReviews(String authToken) throws IOException {
        Collection<Proxy_CanteenReview> reviews = proxy.getCanteenReviews(getFormattedBearerToken(authToken)).execute().body();
        if (reviews == null) {
            return null;
        }
        Collection<Review> result = new ArrayList<>(reviews.size());
        for (Proxy_CanteenReview review : reviews) {
            result.add(review.toReview());
        }
        return result;
    }

    @Override
    public void removeCanteenReview(String authToken, String reviewId) throws IOException {
        proxy.removeCanteenReview(getFormattedBearerToken(authToken), reviewId).execute().body();
    }

    private String getFormattedBearerToken(String token) {
        return String.format("Bearer %s", token);
    }

    private interface Proxy {
        @POST("authenticate")
        Call<String> authenticate(@Query("userName") String userName, @Query("password") String password);

        @GET("canteen")
        Call<Proxy_CanteenDetails> getCanteen(@Header("Authorization") String authenticationToken);

        @PUT("canteen/data")
        Call<Void> updateCanteenData(@Header("Authorization") String authenticationToken, @Query("name") String name, @Query("website") String website, @Query("phoneNumber") String phoneNumber, @Query("address") String address);

        @PUT("canteen/dish")
        Call<Void> updateCanteenDish(@Header("Authorization") String authenticationToken, @Query("dish") String dish, @Query("dishPrice") float dishPrice);

        @PUT("canteen/waiting-time")
        Call<Void> updateCanteenWaitingTime(@Header("Authorization") String authenticationToken, @Query("waitingTime") int waitingTime);

        @GET("canteen/review-statistics")
        Call<Proxy_CanteenReviewStatistics> getCanteenReviewStatistics(@Header("Authorization") String authenticationToken);

        @GET("canteen/reviews")
        Call<Collection<Proxy_CanteenReview>> getCanteenReviews(@Header("Authorization") String authenticationToken);

        @DELETE("canteen/reviews/{reviewId}")
        Call<Void> removeCanteenReview(@Header("Authorization") String authenticationToken, @Path("reviewId") String reviewId);
    }

    private static class Proxy_CanteenDetails {
        String id;
        String name;
        String address;
        String phoneNumber;
        String website;
        String dish;
        float dishPrice;
        int waitingTime;

        CanteenDetails toCanteenDetails() {
            return new CanteenDetails(id, name, phoneNumber, website, dish, dishPrice, address, waitingTime);
        }
    }

    private static class Proxy_CanteenReviewStatistics {
        int countOneStar;
        int countTwoStars;
        int countThreeStars;
        int countFourStars;
        int countFiveStars;

        ReviewData toReviewData() {
            return new ReviewData(countOneStar, countTwoStars, countThreeStars, countFourStars, countFiveStars);
        }
    }

    private static class Proxy_CanteenReview {
        String id;
        String creationDate;
        String creator;
        int rating;
        String remark;

        Review toReview() {
            return new Review(id, creationDate, creator, rating, remark);
        }
    }
}
