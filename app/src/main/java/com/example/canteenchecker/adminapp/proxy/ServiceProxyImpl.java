package com.example.canteenchecker.adminapp.proxy;

import com.example.canteenchecker.adminapp.core.CanteenDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
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
        Proxy_CanteenDetails canteen = proxy.getCanteen(String.format("Bearer %s", authToken)).execute().body();
        return canteen != null ? canteen.toCanteenDetails() : null;
    }

    @Override
    public boolean updateCanteenData(String authToken, String name, String website, String phoneNumber, String address) throws IOException {
        proxy.updateCanteenData(String.format("Bearer %s", authToken), name, website, phoneNumber, address).execute().body();
        return true;
    }

    @Override
    public boolean updateCanteenDish(String authToken, String dish, float dishPrice) throws IOException {
        proxy.updateCanteenDish(String.format("Bearer %s", authToken), dish, dishPrice).execute().body();
        return true;
    }

    @Override
    public boolean updateCanteenWaitingTime(String authToken, int waitingTime) throws IOException {
        // ToDo: check if header can be read for result
        proxy.updateCanteenWaitingTime(String.format("Bearer %s", authToken), waitingTime).execute().body();
        return true;
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

        /*@GET("canteens")
        Call<Collection<Proxy_CanteenData>> getCanteens(@Query("name") String name);

        @GET("canteens/{canteenId}/review-statistics")
        Call<Proxy_CanteenReviewStatistics> getReviewStatisticsForCanteen(@Path("canteenId") String canteenId);

        @POST("canteens/{canteenId}/reviews")
        Call<Void> postCanteenReview(@Header("Authorization") String authenticationToken, @Path("canteenId") String canteenId, @Query("rating") int rating, @Query("remark") String remark);

         */
    }

    private static class Proxy_CanteenDetails {
        String name;
        String address;
        String phoneNumber;
        String website;
        String dish;
        float dishPrice;
        int waitingTime;

        CanteenDetails toCanteenDetails() {
            return new CanteenDetails(name, phoneNumber, website, dish, dishPrice, address, waitingTime);
        }
    }
}
