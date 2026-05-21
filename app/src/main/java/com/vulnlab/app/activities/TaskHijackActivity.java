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
            "Task Hijack (StrandHogg)\n\n"
            + "This activity's taskAffinity is set to:\n"
            + "  com.target.banking.app\n\n"
            + "launchMode: singleTask\n\n"
            + "When a user opens the banking app after this activity has run, "
            + "Android routes them here instead.\n\n"
            + "In a real attack this screen would mimic the banking app's login UI."
        );
    }
}
