package com.vulnlab.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WeakPermissionReceiver extends BroadcastReceiver {

    private static final String TAG = "VulnWeakPermReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "[weak-permission] ADMIN_COMMAND received from: "
            + intent.getExtras());

        // In a real app this would execute an admin action:
        String command = intent.getStringExtra("command");
        Log.d(TAG, "[weak-permission] executing admin command: " + command);

        // Simulated privileged actions
        if ("clear_users".equals(command)) {
            Log.d(TAG, "[weak-permission] would clear all users from DB");
        } else if ("dump_tokens".equals(command)) {
            String token = context.getSharedPreferences("auth_prefs", 0)
                .getString("session_token", "none");
            Log.d(TAG, "[weak-permission] dumping token: " + token);
        }
    }
}
