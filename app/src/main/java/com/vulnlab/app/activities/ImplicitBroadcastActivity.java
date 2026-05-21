package com.vulnlab.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;

public class ImplicitBroadcastActivity extends AppCompatActivity {

    private static final String TAG = "VulnBroadcast";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_implicit_broadcast);

        TextView tvOutput = findViewById(R.id.tv_output);

        // Button 1: implicit broadcast with sensitive data
        findViewById(R.id.btn_send_implicit).setOnClickListener(v -> {
            // VULN: no permission — any app receives this
            Intent sessionChanged = new Intent("com.vulnlab.app.SESSION_CHANGED");
            sessionChanged.putExtra("token",   "session_token_abc123");
            sessionChanged.putExtra("user_id", "user_42");
            sessionChanged.putExtra("email",   "victim@corp.com");
            Log.d(TAG, "[implicit-broadcast] sending SESSION_CHANGED");
            sendBroadcast(sessionChanged);   // VULN: no permission argument

            Intent loginSuccess = new Intent("com.vulnlab.app.LOGIN_SUCCESS");
            loginSuccess.putExtra("email", "victim@corp.com");
            loginSuccess.putExtra("role",  "ADMIN");
            sendBroadcast(loginSuccess);     // VULN

            tvOutput.setText("Sent implicit broadcasts with session token and email.\n"
                + "Any app with a matching receiver captures them.");
        });

        // Button 2: sticky broadcast — persists until explicitly removed
        findViewById(R.id.btn_send_sticky).setOnClickListener(v -> {
            // VULN: sticky broadcast — persists forever until removeStickyBroadcast()
            Intent tokenRefresh = new Intent("com.vulnlab.app.TOKEN_REFRESH");
            tokenRefresh.putExtra("access_token",  "access_token_xyz789");
            tokenRefresh.putExtra("refresh_token", "refresh_token_uvw456");
            tokenRefresh.putExtra("expires_in",    3600);
            Log.d(TAG, "[sticky-broadcast] sending TOKEN_REFRESH");
            sendStickyBroadcast(tokenRefresh);  // VULN: sticky, no permission

            tvOutput.setText("Sent sticky broadcast with OAuth tokens.\n"
                + "Any app installed later can still receive this broadcast.\n\n"
                + "adb shell dumpsys activity broadcasts | grep TOKEN_REFRESH");
        });
    }
}
