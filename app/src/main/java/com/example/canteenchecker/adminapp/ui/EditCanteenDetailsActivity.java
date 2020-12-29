package com.example.canteenchecker.adminapp.ui;

import android.content.Context;
import android.content.Intent;
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

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

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