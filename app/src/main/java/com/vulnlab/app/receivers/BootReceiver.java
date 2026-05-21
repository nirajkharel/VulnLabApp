package com.vulnlab.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "VulnBootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        String token = context.getSharedPreferences("auth_prefs", 0)
            .getString("session_token", null);

        if (token != null) {
            // VULN: sticky broadcast with session token on every boot
            Intent sticky = new Intent("com.vulnlab.app.TOKEN_REFRESH");
            sticky.putExtra("access_token",  token);
            sticky.putExtra("refresh_token", "refresh_" + token);
            Log.d(TAG, "[boot] sending sticky TOKEN_REFRESH with token: " + token);
            context.sendStickyBroadcast(sticky);
        }
    }
}
