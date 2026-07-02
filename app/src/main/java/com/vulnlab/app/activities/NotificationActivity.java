package com.vulnlab.app.activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.vulnlab.app.R;

public class NotificationActivity extends AppCompatActivity {

    private static final String TAG     = "VulnNotification";
    private static final String CHANNEL = "vulnlab_channel";
    private static final int    NOTIF_ID = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        EditText etTitle = findViewById(R.id.et_notif_title);
        EditText etBody  = findViewById(R.id.et_notif_body);
        TextView tvOutput = findViewById(R.id.tv_output);

        createChannel();

        // VULN: notification-title-spoofing — extras fire notification directly, no button click needed
        Intent intent = getIntent();
        if (intent.hasExtra("title") && intent.hasExtra("body")) {
            String t = intent.getStringExtra("title");
            String b = intent.getStringExtra("body");
            etTitle.setText(t);
            etBody.setText(b);
            Log.d(TAG, "[notif-spoof] title=" + t + " body=" + b);
            Notification auto = new NotificationCompat.Builder(this, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(t)
                .setContentText(b)
                .build();
            getSystemService(NotificationManager.class).notify(NOTIF_ID + 1, auto);
        } else {
            if (intent.hasExtra("title")) etTitle.setText(intent.getStringExtra("title"));
            if (intent.hasExtra("body"))  etBody.setText(intent.getStringExtra("body"));
        }

        // VULN: mutable-pending-intent-hijack (Tier 2). Post a notification whose
        // contentIntent is built from an EMPTY base intent + FLAG_MUTABLE. A holder
        // (e.g. a NotificationListenerService) can fillIn the action/data/package and
        // a URI grant flag, steering the fire to their own component and stealing a
        // grant on our private SecretProvider. Trigger: --ez post_empty_pi true
        if (intent.getBooleanExtra("post_empty_pi", false)) {
            PendingIntent leak = PendingIntent.getActivity(
                this, 2, new Intent(),   // VULN: empty base — attacker fills it all in
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

            Notification n = new NotificationCompat.Builder(this, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("VulnLabApp: Sync required")
                .setContentText("Tap to sync your account.")
                .setContentIntent(leak)
                .build();
            getSystemService(NotificationManager.class).notify(NOTIF_ID + 2, n);
            tvOutput.setText("Empty-base mutable PendingIntent posted (Tier 2). "
                + "A holder can redirect it and steal a grant on com.vulnlab.app.secret.");
        }

        // Button 1: mutable PendingIntent notification
        findViewById(R.id.btn_mutable_pi).setOnClickListener(v -> {
            Intent targetIntent = new Intent(this, FileWriteActivity.class);
            targetIntent.putExtra("_original_extra", "safe_value");

            // VULN: FLAG_MUTABLE — receiver can modify extras before firing.
            // Set explicitly on every API so the flag is honest to tooling
            // (pre-31 is mutable-by-default; bit 0x02000000 is a no-op there).
            int piFlags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
            PendingIntent pi = PendingIntent.getActivity(this, 1, targetIntent, piFlags);
            Log.d(TAG, "[mutable-PI] created mutable PendingIntent for FileWriteActivity");

            Notification n = new NotificationCompat.Builder(this, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("VulnLabApp: Action Required")
                .setContentText("Tap to complete the action.")
                .setContentIntent(pi)
                .build();

            getSystemService(NotificationManager.class).notify(NOTIF_ID, n);
            tvOutput.setText("Mutable PendingIntent notification posted. "
                + "Any holder can inject extras into FileWriteActivity.");
        });

        // Button 2: spoofed notification title/body
        findViewById(R.id.btn_spoof_notif).setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            String body  = etBody.getText().toString();

            // VULN: notification-title-spoofing — no sanitization
            Log.d(TAG, "[notif-spoof] title=" + title + " body=" + body);
            Notification n = new NotificationCompat.Builder(this, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)   // VULN: attacker-controlled
                .setContentText(body)     // VULN: attacker-controlled
                .build();

            getSystemService(NotificationManager.class).notify(NOTIF_ID + 1, n);
            tvOutput.setText("Spoofed notification: [" + title + "] " + body);
        });
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                CHANNEL, "VulnLab", NotificationManager.IMPORTANCE_DEFAULT);
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }
}
