package com.vulnlab.app.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;

public class StickyBroadcastActivity extends AppCompatActivity {

    private static final String TAG = "VulnStickyReceive";
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticky_broadcast);

        TextView tvOutput = findViewById(R.id.tv_output);

        // VULN: register for sticky broadcast — gets last sent intent immediately
        IntentFilter filter = new IntentFilter("com.vulnlab.app.TOKEN_REFRESH");
        Intent stickyIntent = registerReceiver(null, filter);

        if (stickyIntent != null) {
            // Received the previously-sent sticky broadcast immediately
            String accessToken  = stickyIntent.getStringExtra("access_token");
            String refreshToken = stickyIntent.getStringExtra("refresh_token");
            Log.d(TAG, "[sticky-eavesdrop] access_token=" + accessToken);
            Log.d(TAG, "[sticky-eavesdrop] refresh_token=" + refreshToken);
            tvOutput.setText("Captured sticky broadcast immediately on register:\n\n"
                + "access_token:  " + accessToken  + "\n"
                + "refresh_token: " + refreshToken + "\n\n"
                + "These were sent before this activity existed.");
        } else {
            tvOutput.setText("No sticky broadcast found yet.\n"
                + "First press 'Send Sticky' in ImplicitBroadcastActivity,\n"
                + "then re-open this screen.");
        }

        // Button: dynamic registration to catch future sticky broadcasts
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                Log.d(TAG, "[sticky-eavesdrop] received: " + intent.getExtras());
                tvOutput.setText("Received: " + intent.getExtras().toString());
            }
        };
        registerReceiver(receiver, new IntentFilter("com.vulnlab.app.SESSION_CHANGED"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) unregisterReceiver(receiver);
    }
}
