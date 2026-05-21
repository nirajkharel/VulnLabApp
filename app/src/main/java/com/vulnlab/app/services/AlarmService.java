package com.vulnlab.app.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.vulnlab.app.activities.FileWriteActivity;

@SuppressWarnings("deprecation")
public class AlarmService extends IntentService {

    private static final String TAG = "VulnAlarmService";

    public AlarmService() { super("AlarmService"); }

    @Override
    protected void onHandleIntent(Intent intent) {
        Intent target = new Intent(this, FileWriteActivity.class);
        target.putExtra("type", "config");

        // VULN: FLAG_MUTABLE allows the alarm receiver to modify extras
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_MUTABLE;
        }

        PendingIntent pi = PendingIntent.getActivity(this, 100, target, flags);
        Log.d(TAG, "[alarm-service] scheduled mutable PendingIntent: " + pi);

        android.app.AlarmManager am = getSystemService(android.app.AlarmManager.class);
        am.set(android.app.AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 60_000L, pi);
    }
}
