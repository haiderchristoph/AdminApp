package com.example.canteenchecker.adminapp.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.canteenchecker.adminapp.CanteenCheckerAdminApplication;
import com.example.canteenchecker.adminapp.R;
import com.example.canteenchecker.adminapp.core.Broadcasting;
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
import java.text.NumberFormat;
import java.util.List;

public class OverviewFragment extends Fragment {
    private static final String TAG = CanteenDetailsActivity.class.toString();
    private static final float DEFAULT_MAP_ZOOM_FACTOR = 15;

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

    private BroadcastReceiver broadcastReceiver = null;

    public OverviewFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        viwProgress = view.findViewById(R.id.viwProgress);
        viwContent = view.findViewById(R.id.viwContent);
        txvName = view.findViewById(R.id.txvName);
        txvLocation = view.findViewById(R.id.txvLocation);
        txvDish = view.findViewById(R.id.txvDish);
        txvDishPrice = view.findViewById(R.id.txvDishPrice);
        txvWaitingTime = view.findViewById(R.id.txvWaitingTime);
        prbWaitingTime = view.findViewById(R.id.prbWaitingTime);
        txvPhoneNumber = view.findViewById(R.id.txvPhoneNumber);
        txvWebsite = view.findViewById(R.id.txvWebsite);


        mpfMap = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mpfMap);
        mpfMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                UiSettings uiSettings = googleMap.getUiSettings();
                uiSettings.setAllGesturesEnabled(false);
                uiSettings.setZoomControlsEnabled(true);
            }
        });

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.getContext().startActivity(EditCanteenDetailsActivity.createIntent(view.getContext()));
            }
        });
        updateCanteenDetails();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
    }

    private void updateCanteenDetails() {
        new AsyncTask<String, Void, CanteenDetails>() {
            @Override
            protected CanteenDetails doInBackground(String... strings) {
                try {
                    String authToken = ((CanteenCheckerAdminApplication) getActivity().getApplication()).getAuthenticationToken();
                    return ServiceProxyFactory.createProxy().getCanteen(authToken);
                } catch (IOException e) {
                    Log.e(TAG, String.format("Downloading of canteen failed."), e);
                }
                // TODO
                return null;
            }
            @Override
            protected void onPostExecute(CanteenDetails canteenDetails) {
                if (canteenDetails != null) {
                    if (broadcastReceiver == null) {
                        broadcastReceiver = new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                String canteenId = canteenDetails.getId();
                                if (canteenId != null && canteenId.equals(Broadcasting.extractCanteenId(intent))) {
                                    updateCanteenDetails();
                                }
                            }
                        };
                        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, Broadcasting.createCanteenChangedBroadcastIntentFilter());
                    }

                    ((CanteenCheckerAdminApplication) getActivity().getApplication()).setCanteenDetails(canteenDetails);
                    // make ui visible
                    viwProgress.setVisibility(View.GONE);
                    viwContent.setVisibility(View.VISIBLE);
                    // display details
                    txvName.setText(canteenDetails.getName());
                    txvLocation.setText(canteenDetails.getLocation());
                    txvDish.setText(canteenDetails.getDish());
                    txvDishPrice.setText(NumberFormat.getCurrencyInstance().format(canteenDetails.getDishPrice()));
                    txvWaitingTime.setText(String.format("%s", canteenDetails.getWaitingTime()));
                    prbWaitingTime.setProgress(canteenDetails.getWaitingTime());
                    String phoneNumber = canteenDetails.getPhoneNumber();
                    String website = canteenDetails.getWebsite();
                    txvPhoneNumber.setText(phoneNumber.trim().isEmpty() ? "-" : phoneNumber);
                    txvWebsite.setText(website.trim().isEmpty() ? "-" : website);

                    new AsyncTask<String, Void, LatLng>() {
                        @Override
                        protected LatLng doInBackground(String... strings) {
                            LatLng location = null;
                            Geocoder geocoder = new Geocoder(getContext());
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
                    Toast.makeText(getContext(), R.string.message_canteen_not_found, Toast.LENGTH_SHORT).show();
                }
                super.onPostExecute(canteenDetails);
            }
        }.execute("asdf");
    }
}