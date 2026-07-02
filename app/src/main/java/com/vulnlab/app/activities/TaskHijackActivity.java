package com.vulnlab.app.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;

public class TaskHijackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_hijack);

        TextView tvInfo = findViewById(R.id.tv_task_hijack_info);
        tvInfo.setText(
            "Task Hijacking (StrandHogg)\n\n"
            + "Vulnerability: MainActivity declares no taskAffinity.\n"
            + "Android assigns the default: com.vulnlab.app\n\n"
            + "Any attacker app that sets:\n"
            + "  taskAffinity=\"com.vulnlab.app\"\n"
            + "  launchMode=\"singleTask\"\n\n"
            + "can plant itself in this app's task.\n"
            + "On Android <= 10 the next launcher tap routes to the\n"
            + "attacker's screen instead of this app.\n\n"
            + "Fix: android:taskAffinity=\"\" on MainActivity"
        );
    }
}
