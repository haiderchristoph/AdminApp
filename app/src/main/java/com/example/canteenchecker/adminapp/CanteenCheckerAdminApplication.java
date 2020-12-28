package com.example.canteenchecker.adminapp;
import android.app.Application;

import com.example.canteenchecker.adminapp.core.CanteenDetails;

public class CanteenCheckerAdminApplication extends Application {
    private String authenticationToken = null;
    private CanteenDetails canteenDetails = null;

    public synchronized void setAuthenticationToken(String authenticationToken) { this.authenticationToken = authenticationToken; }
    public synchronized String getAuthenticationToken() { return authenticationToken; }
    public synchronized boolean isAuthenticated() { return getAuthenticationToken() != null; }

    public synchronized void setCanteenDetails(CanteenDetails canteenDetails) { this.canteenDetails = canteenDetails; }
    public synchronized CanteenDetails getCanteenDetails() { return canteenDetails; }
}
