package com.example.canteenchecker.adminapp.ui;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class EditCanteenDetailsActivity extends AppCompatActivity {
    private static final String TAG = EditCanteenDetailsActivity.class.toString();
    private static final float DEFAULT_MAP_ZOOM_FACTOR = 15;

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, EditCanteenDetailsActivity.class);
        return intent;
    }

    private CanteenDetails canteenDetails = null;
    private View editViewProgress;
    private View editViewContent;

    private EditText editName;
    private EditText editWebsite;
    private EditText editPhoneNumber;
    private EditText editLocation;
    private SupportMapFragment mpfMap;
    Button btnUpdateBasicData;

    private EditText editDish;
    private EditText editDishPrice;
    Button btnUpdateDish;

    private EditText editWaitingTime;
    Button btnUpdateWaitingTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_canteen_details);
        canteenDetails = ((CanteenCheckerAdminApplication) getApplication()).getCanteenDetails();

        editViewProgress = findViewById(R.id.editViewProgress);
        editViewContent = findViewById(R.id.editViewContent);

        // ==== Basic Data ====
        editName = findViewById(R.id.editName);
        editWebsite = findViewById(R.id.editWebsite);
        editPhoneNumber = findViewById(R.id.editPhoneNumber);
        editLocation = findViewById(R.id.editLocation);

        mpfMap = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mpfMap);
        mpfMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                UiSettings uiSettings = googleMap.getUiSettings();
                uiSettings.setAllGesturesEnabled(false);
                uiSettings.setZoomControlsEnabled(true);
            }
        });

        new AsyncTask<String, Void, LatLng>() {
            @Override
            protected LatLng doInBackground(String... strings) {
                LatLng location = null;
                Geocoder geocoder = new Geocoder(EditCanteenDetailsActivity.this);
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
                        // add draggable(true) to enable drag of the marker
                        googleMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_MAP_ZOOM_FACTOR));
                    } else {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0,0), 0));
                    }
                    googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                        @Override
                        public void onMarkerDragStart(Marker marker) {
                        }

                        @Override
                        public void onMarkerDrag(Marker marker) {
                        }

                        @Override
                        public void onMarkerDragEnd(Marker marker) {
                            LatLng latLng = marker.getPosition();
                            Geocoder geocoder = new Geocoder(getApplication(), Locale.getDefault());
                            try {
                                // get and set actual address
                                editLocation.setText(geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0).getAddressLine(0));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                });
            }
        }.execute(canteenDetails.getLocation());

        editName.setText(canteenDetails.getName().trim());
        editWebsite.setText(canteenDetails.getWebsite().trim());
        editPhoneNumber.setText(canteenDetails.getPhoneNumber().trim());
        editLocation.setText(canteenDetails.getLocation().trim());

        editViewProgress.setVisibility(View.GONE);
        editViewContent.setVisibility(View.VISIBLE);

        btnUpdateBasicData = findViewById(R.id.btnUpdateBasicData);
        btnUpdateBasicData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCanteenData();
            }
        });


        // ==== Dish ====
        editDish = findViewById(R.id.editDish);
        editDishPrice = findViewById(R.id.editDishPrice);
        btnUpdateDish = findViewById(R.id.btnUpdateDish);

        editDish.setText(canteenDetails.getDish().trim());
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        DecimalFormatSymbols decimalFormatSymbols = ((DecimalFormat) nf).getDecimalFormatSymbols();
        decimalFormatSymbols.setCurrencySymbol(""); // no symbol
        ((DecimalFormat) nf).setDecimalFormatSymbols(decimalFormatSymbols);

        String formattedDishPrice = nf.format(canteenDetails.getDishPrice());
        // remove leading whitespace
        formattedDishPrice = formattedDishPrice.replaceAll("\\s+", "");
        editDishPrice.setText(formattedDishPrice);

        btnUpdateDish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCanteenDish();
            }
        });

        // ==== Waiting Time ====
        editWaitingTime = findViewById(R.id.editWaitingTime);
        btnUpdateWaitingTime = findViewById(R.id.btnUpdateWaitingTime);
        String waitingTimeString = String.format("%s", canteenDetails.getWaitingTime());

        editWaitingTime.setText(waitingTimeString.trim());

        btnUpdateWaitingTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateWaitingTime();
            }
        });
    }

    private boolean isValidInteger(String value) {
        try {
            int parsedValue = Integer.parseInt(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidFloat(String value) {
        try {
            float parsedValue = Float.parseFloat(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void updateWaitingTime() {
        new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... strings) {
                try {
                    // validating the data, also in the FE
                    if (isValidInteger(strings[1])) {
                        int waitingTime = Integer.parseInt(strings[1]);
                        ServiceProxyFactory.createProxy().updateCanteenWaitingTime(strings[0], waitingTime);
                        return true;
                    } else {
                        // invalid data
                        return false;
                    }
                } catch (IOException e) {
                    Log.e(TAG, String.format("Updating waiting time failed"), e);
                }
                Toast.makeText(EditCanteenDetailsActivity.this, R.string.update_failed, Toast.LENGTH_SHORT).show();
                return false;
            }
            @Override
            protected void onPostExecute(Boolean success) {
                if (!success) {
                    // invalid data, call setError
                    editWaitingTime.setError(getResources().getString(R.string.invalid_waiting_time));
                } else {
                    Toast.makeText(EditCanteenDetailsActivity.this, R.string.update_successful, Toast.LENGTH_SHORT).show();
                }
                super.onPostExecute(success);
            }
        }.execute(((CanteenCheckerAdminApplication) getApplication()).getAuthenticationToken(), editWaitingTime.getText().toString());
    }

    private void updateCanteenDish() {
        new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... strings) {
                try {
                    // validating the data, also in the FE
                    String dishPriceString = strings[1];
                    // normalize string to fit for parseFloat method
                    dishPriceString = dishPriceString.replace(',', '.');
                    if (isValidFloat(dishPriceString)) {
                        float dishPrice = Float.parseFloat(dishPriceString);
                        boolean done = ServiceProxyFactory.createProxy().updateCanteenDish(strings[0], strings[2], dishPrice);
                        return true;
                    } else {
                        return false;
                    }
                } catch (IOException e) {
                    Log.e(TAG, String.format("Updating dish failed"), e);
                }
                Toast.makeText(EditCanteenDetailsActivity.this, R.string.update_failed, Toast.LENGTH_SHORT).show();
                return false;
            }
            @Override
            protected void onPostExecute(Boolean success) {
                if (!success) {
                    // invalid data, call setError
                    editDishPrice.setError(getResources().getString(R.string.invalid_dish_price));
                } else {
                    Toast.makeText(EditCanteenDetailsActivity.this, R.string.update_successful, Toast.LENGTH_SHORT).show();
                }
                super.onPostExecute(success);
            }
        }.execute(((CanteenCheckerAdminApplication) getApplication()).getAuthenticationToken(), editDishPrice.getText().toString(), editDish.getText().toString());
    }

    private void updateCanteenData() {
        new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... strings) {
                try {
                    ServiceProxyFactory.createProxy().updateCanteenData(strings[0], strings[1], strings[2], strings[3], strings[4]);
                    return true;
                } catch (IOException e) {
                    Log.e(TAG, String.format("Updating basic data failed"), e);
                }
                return false;
            }
            @Override
            protected void onPostExecute(Boolean success) {
                if (!success) {
                    Toast.makeText(EditCanteenDetailsActivity.this, R.string.update_failed, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EditCanteenDetailsActivity.this, R.string.update_successful, Toast.LENGTH_SHORT).show();
                }
                super.onPostExecute(success);
            }
        }.execute(((CanteenCheckerAdminApplication) getApplication()).getAuthenticationToken(), editName.getText().toString(), editWebsite.getText().toString(), editPhoneNumber.getText().toString(), editLocation.getText().toString());
    }
}