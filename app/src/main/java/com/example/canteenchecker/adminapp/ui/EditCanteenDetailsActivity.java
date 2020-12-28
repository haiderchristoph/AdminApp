package com.example.canteenchecker.adminapp.ui;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.canteenchecker.adminapp.CanteenCheckerAdminApplication;
import com.example.canteenchecker.adminapp.core.CanteenDetails;
import com.example.canteenchecker.adminapp.proxy.ServiceProxyFactory;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.canteenchecker.adminapp.R;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;

public class EditCanteenDetailsActivity extends AppCompatActivity {
    private static final String TAG = EditCanteenDetailsActivity.class.toString();

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

        // System.out.println(nf.format(12345.124).trim());
        String formattedDishPrice = nf.format(canteenDetails.getDishPrice());
        // remove leading whitespace
        // ToDo: find out why there is a leading whitespace and why .trim() won't remove it
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

    private void updateWaitingTime() {
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... strings) {
                // ToDo check if parameters can be removed (Strings)
                try {
                    // ToDo validating the data, also in the FE
                    String authToken = ((CanteenCheckerAdminApplication) getApplication()).getAuthenticationToken();
                    String waitingTimeString = editWaitingTime.getText().toString();
                    int waitingTime = Integer.parseInt(waitingTimeString);
                    boolean done = ServiceProxyFactory.createProxy().updateCanteenWaitingTime(authToken, waitingTime);
                    // ToDo add a toast or something
                } catch (IOException e) {
                    Log.e(TAG, String.format("Updating waiting time failed"), e);
                }
                // TODO add a toast or something
                return null;
            }
        }.execute();
    }

    private void updateCanteenDish() {
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... strings) {
                // ToDo check if parameters can be removed (Strings)
                try {
                    // ToDo validating the data, also in the FE
                    String authToken = ((CanteenCheckerAdminApplication) getApplication()).getAuthenticationToken();
                    String dishPriceString = editDishPrice.getText().toString();
                    dishPriceString = dishPriceString.replace(',', '.');
                    float dishPrice = Float.parseFloat(dishPriceString);
                    boolean done = ServiceProxyFactory.createProxy().updateCanteenDish(authToken, editDish.getText().toString(), dishPrice);
                    // ToDo add a toast or something
                } catch (IOException e) {
                    Log.e(TAG, String.format("Updating dish failed"), e);
                }
                // TODO add a toast or something
                return null;
            }
        }.execute();
    }

    private void updateCanteenData() {
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... strings) {
                // ToDo check if parameters can be removed (Strings)
                try {
                    String authToken = ((CanteenCheckerAdminApplication) getApplication()).getAuthenticationToken();
                    boolean done = ServiceProxyFactory.createProxy().updateCanteenData(authToken, editName.getText().toString(), editWebsite.getText().toString(), editPhoneNumber.getText().toString(), editLocation.getText().toString());
                    // ToDo add a toast or something
                } catch (IOException e) {
                    Log.e(TAG, String.format("Updating basic data failed"), e);
                }
                // TODO add a toast or something
                return null;
            }
        }.execute();
    }
}