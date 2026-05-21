package com.vulnlab.app.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;
import com.vulnlab.app.VulnApplication;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "VulnLabLogin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText emailView    = findViewById(R.id.et_email);
        EditText passwordView = findViewById(R.id.et_password);
        Button   loginButton  = findViewById(R.id.btn_login);

        loginButton.setOnClickListener(v -> {
            String email    = emailView.getText().toString();
            String password = passwordView.getText().toString();

            // VULN: Log.d is stripped in release builds in well-configured apps.
            // Here it is not stripped — Log.d is always compiled in.
            Log.d(TAG, "Login attempt: email=" + email + " password=" + password);

            String fakeToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIiLCJlbWFpbCI6IiIsInJvbGUiOiJ1c2VyIn0.FAKE";
            Log.d(TAG, "Session token issued: " + fakeToken);
            Log.d(TAG, "User PII: DOB=1990-01-15, SSN=123-45-6789, CC=4111111111111111");

            // VULN: password stored in plaintext SharedPreferences
            SharedPreferences prefs = getSharedPreferences(
                VulnApplication.PREFS_AUTH, MODE_PRIVATE);
            prefs.edit()
                .putString(VulnApplication.KEY_EMAIL,    email)
                .putString(VulnApplication.KEY_PASSWORD, password)
                .putString(VulnApplication.KEY_TOKEN,    fakeToken)
                .putString(VulnApplication.KEY_API_KEY,  "sk-prod-8f3k2j9x0q1w5e6r")
                .apply();

            Toast.makeText(this, "Logged in as: " + email, Toast.LENGTH_SHORT).show();
        });
    }
}
