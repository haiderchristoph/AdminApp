package com.example.canteenchecker.adminapp.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.canteenchecker.adminapp.CanteenCheckerAdminApplication;
import com.example.canteenchecker.adminapp.R;
import com.example.canteenchecker.adminapp.proxy.ServiceProxyFactory;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.toString();

    public static Intent createIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }

    private EditText edtUserName;
    private EditText edtPassword;
    private Button btnLogIn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUserName = findViewById(R.id.username);
        edtPassword = findViewById(R.id.password);
        btnLogIn = findViewById(R.id.login);

        btnLogIn.setOnClickListener(v -> {
            // check credentials and store them on success
            setUIEnabled(false);
            new AsyncTask<String, Void, String>() {

                @Override
                protected String doInBackground(String... strings) {
                    try {
                        return ServiceProxyFactory.createProxy().authenticate(strings[0], strings[1]);
                    } catch (IOException e) {
                        Log.e(TAG, String.format("Login failed for user name '%s'.", strings[0]), e);
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(String bearer) {
                    if (bearer != null) {
                        // save bearer globally for application
                        ((CanteenCheckerAdminApplication) getApplication()).setAuthenticationToken(bearer);
                        setResult(RESULT_OK);
                        // go to next activity
                        v.getContext().startActivity(CanteenDetailsActivity.createIntent(v.getContext()));
                        // user should not go back to this activity
                        finish();
                    } else {
                        setUIEnabled(true);
                        edtPassword.setText(null);
                        Toast.makeText(LoginActivity.this, R.string.login_failed, Toast.LENGTH_LONG).show();
                    }
                }
            }.execute(edtUserName.getText().toString(), edtPassword.getText().toString());
        });
    }

    private void setUIEnabled(boolean enabled) {
        btnLogIn.setEnabled(enabled);
        edtUserName.setEnabled(enabled);
        edtPassword.setEnabled(enabled);
    }
}
