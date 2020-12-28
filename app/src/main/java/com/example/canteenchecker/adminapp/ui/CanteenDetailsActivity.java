package com.example.canteenchecker.adminapp.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
//import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/*import com.example.canteenchecker.consumerapp.R;
import com.example.canteenchecker.consumerapp.core.Broadcasting;
import com.example.canteenchecker.consumerapp.core.CanteenDetails;
import com.example.canteenchecker.consumerapp.proxy.ServiceProxyFactory;
*/
import com.example.canteenchecker.adminapp.CanteenCheckerAdminApplication;
import com.example.canteenchecker.adminapp.R;
import com.example.canteenchecker.adminapp.core.CanteenDetails;
import com.example.canteenchecker.adminapp.proxy.ServiceProxyFactory;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.List;

public class CanteenDetailsActivity extends AppCompatActivity {

    private static final String TAG = CanteenDetailsActivity.class.toString();
    private static final String CANTEEN_ID_KEY = "CanteenId";
    private static final float DEFAULT_MAP_ZOOM_FACTOR = 15;
    private static final int MAKE_PHONE_CALL_REQUEST_CODE = 12345;

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, CanteenDetailsActivity.class);
        //intent.putExtra(CANTEEN_ID_KEY, canteenId);
        return intent;
    }

    /*
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String canteenId = getCanteenId();
            if (canteenId != null && canteenId.equals(Broadcasting.extractCanteenId(intent))) {
                updateCanteenDetails();
            }
        }
    };

     */

    private CanteenDetails canteen = null;

    private View viwProgress;
    private View viwContent;

    private TextView txvName;
    private TextView txvLocation;
    private TextView txvDish;
    private TextView txvDishPrice;
    private TextView txvWaitingTime;
    private TextView txvPhoneNumber;
    private TextView txvWebsite;
    private ProgressBar prbWaitingTime;
    private SupportMapFragment mpfMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_canteen_details);
        viwProgress = findViewById(R.id.viwProgress);
        viwContent = findViewById(R.id.viwContent);
        txvName = findViewById(R.id.txvName);
        txvLocation = findViewById(R.id.txvLocation);
        txvDish = findViewById(R.id.txvDish);
        txvDishPrice = findViewById(R.id.txvDishPrice);
        txvWaitingTime = findViewById(R.id.txvWaitingTime);
        prbWaitingTime = findViewById(R.id.prbWaitingTime);
        txvPhoneNumber = findViewById(R.id.txvPhoneNumber);
        txvWebsite = findViewById(R.id.txvWebsite);


        mpfMap = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mpfMap);
        mpfMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                UiSettings uiSettings = googleMap.getUiSettings();
                uiSettings.setAllGesturesEnabled(false);
                uiSettings.setZoomControlsEnabled(true);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.getContext().startActivity(EditCanteenDetailsActivity.createIntent(view.getContext()));
            }
        });

        //getFragmentManager().beginTransaction().replace(R.id.lnlReviews, ReviewsFragment.create(getCanteenId())).commit();

        //LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, Broadcasting.createCanteenChangedBroadcastIntentFilter());

        updateCanteenDetails();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @SuppressLint("StaticFieldLeak")
    private void updateCanteenDetails() {
        new AsyncTask<String, Void, CanteenDetails>() {
            @Override
            protected CanteenDetails doInBackground(String... strings) {
                try {
                    String authToken = ((CanteenCheckerAdminApplication) getApplication()).getAuthenticationToken();
                    CanteenDetails d = ServiceProxyFactory.createProxy().getCanteen(authToken);
                    return d;
                } catch (IOException e) {
                    Log.e(TAG, String.format("Downloading of canteen failed."), e);
                }
                // TODO
                return null;
            }
            @Override
            protected void onPostExecute(CanteenDetails canteenDetails) {
                canteen = canteenDetails;
                invalidateOptionsMenu();
                if (canteenDetails != null) {
                    ((CanteenCheckerAdminApplication) getApplication()).setCanteenDetails(canteenDetails);
                    // make ui visible
                    viwProgress.setVisibility(View.GONE);
                    viwContent.setVisibility(View.VISIBLE);
                    // display details
                    setTitle(canteenDetails.getName());
                    txvName.setText(canteenDetails.getName());
                    txvLocation.setText(canteenDetails.getLocation());
                    txvDish.setText(canteenDetails.getDish());
                    txvDishPrice.setText(NumberFormat.getCurrencyInstance().format(canteenDetails.getDishPrice()));
                    txvWaitingTime.setText(String.format("%s", canteenDetails.getWaitingTime()));
                    prbWaitingTime.setProgress(canteenDetails.getWaitingTime());
                    txvPhoneNumber.setText(canteenDetails.getPhoneNumber());
                    txvWebsite.setText(canteenDetails.getWebsite());

                    new AsyncTask<String, Void, LatLng>() {
                        @Override
                        protected LatLng doInBackground(String... strings) {
                            LatLng location = null;
                            Geocoder geocoder = new Geocoder(CanteenDetailsActivity.this);
                            try {
                                List<Address> addresses = strings[0] == null ? null : geocoder.getFromLocationName(strings[0], 1);
                                if (addresses != null && addresses.size() > 0) {
                                    Address address = addresses.get(0);
                                    location = new LatLng(address.getLatitude(), address.getLongitude());
                                } else {
                                    // TODO Logging
                                    Log.w(TAG, String.format("No locations found for '%s'", strings[0]));
                                }
                            } catch (IOException e) {
                                // TODO Logging
                                Log.w(TAG, String.format("Locations lookup for '%s' failed", strings[0]));
                            }
                            return location;
                        }

                        @Override
                        protected void onPostExecute(final LatLng latLng) {
                            mpfMap.getMapAsync(googleMap -> {
                                googleMap.clear();
                                if (latLng != null) {
                                    googleMap.addMarker(new MarkerOptions().position(latLng));
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_MAP_ZOOM_FACTOR));
                                } else {
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0,0), 0));
                                }
                            });
                        }
                    }.execute(canteenDetails.getLocation());
                } else {
                    // TODO what shall we do when canteen is not found?!
                    Toast.makeText(CanteenDetailsActivity.this, R.string.message_canteen_not_found, Toast.LENGTH_SHORT).show();
                    finish();
                }
                super.onPostExecute(canteenDetails);
            }
        }.execute("asdf");
    }
    // getIntent().getStringExtra(CANTEEN_ID_KEY)
}
