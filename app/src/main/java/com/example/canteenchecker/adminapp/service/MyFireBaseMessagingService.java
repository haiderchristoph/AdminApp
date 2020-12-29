package com.example.canteenchecker.adminapp.service;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.example.canteenchecker.adminapp.core.Broadcasting;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFireBaseMessagingService extends FirebaseMessagingService {
    private static final String REMOTE_MESSAGE_CANTEEN_ID_KEY = "canteenId";
    private static final String REMOTE_MESSAGE_TOPIC = "CanteenUpdates";

    public static void subscribeToCanteenUpdates() {
        FirebaseMessaging.getInstance().subscribeToTopic(REMOTE_MESSAGE_TOPIC);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        subscribeToCanteenUpdates();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        if (data.containsKey(REMOTE_MESSAGE_CANTEEN_ID_KEY)) {
            String canteenId = data.get(REMOTE_MESSAGE_CANTEEN_ID_KEY);
            if (canteenId != null) {
                LocalBroadcastManager.getInstance(this).sendBroadcast(Broadcasting.createCanteenChangedBroadcastIntent(canteenId));
            }
        }
        super.onMessageReceived(remoteMessage);
    }
}
