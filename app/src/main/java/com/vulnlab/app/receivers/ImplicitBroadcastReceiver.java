package com.vulnlab.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ImplicitBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "VulnImplicitReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Received broadcast: " + action);
        Log.d(TAG, "Extras: " + intent.getExtras());

        // Log every sensitive extra — visible in adb logcat
        if ("com.vulnlab.app.SESSION_CHANGED".equals(action)) {
            Log.d(TAG, "[leak] token=" + intent.getStringExtra("token"));
            Log.d(TAG, "[leak] user_id=" + intent.getStringExtra("user_id"));
            Log.d(TAG, "[leak] email=" + intent.getStringExtra("email"));
        } else if ("com.vulnlab.app.LOGIN_SUCCESS".equals(action)) {
            Log.d(TAG, "[leak] email=" + intent.getStringExtra("email"));
            Log.d(TAG, "[leak] role=" + intent.getStringExtra("role"));
        } else if ("com.vulnlab.app.TOKEN_REFRESH".equals(action)) {
            Log.d(TAG, "[leak] access_token="  + intent.getStringExtra("access_token"));
            Log.d(TAG, "[leak] refresh_token=" + intent.getStringExtra("refresh_token"));
        }
    }
}
