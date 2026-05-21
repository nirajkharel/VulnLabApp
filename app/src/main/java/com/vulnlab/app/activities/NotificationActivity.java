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

        // VULN: notification-title-spoofing — pre-fill from intent extras
        Intent intent = getIntent();
        if (intent.hasExtra("title")) etTitle.setText(intent.getStringExtra("title"));
        if (intent.hasExtra("body"))  etBody.setText(intent.getStringExtra("body"));

        // Button 1: mutable PendingIntent notification
        findViewById(R.id.btn_mutable_pi).setOnClickListener(v -> {
            Intent targetIntent = new Intent(this, FileWriteActivity.class);
            targetIntent.putExtra("_original_extra", "safe_value");

            // VULN: FLAG_MUTABLE — receiver can modify extras before firing
            int piFlags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                // On pre-31, FLAG_MUTABLE is default
                piFlags = PendingIntent.FLAG_UPDATE_CURRENT;
            }
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
