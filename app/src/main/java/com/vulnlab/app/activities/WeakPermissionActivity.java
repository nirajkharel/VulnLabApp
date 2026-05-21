package com.vulnlab.app.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;

public class WeakPermissionActivity extends AppCompatActivity {

    private static final String TAG = "VulnWeakPerm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weak_permission);

        Log.d(TAG, "[weak-permission] accessed by: " + getCallingPackage());

        TextView tv = findViewById(R.id.tv_weak_perm_info);
        tv.setText(
            "Weak Custom Permission\n\n"
            + "This activity requires:\n"
            + "  com.vulnlab.app.SENSITIVE_ACTION\n\n"
            + "Protection level: normal\n\n"
            + "Any app can gain this permission by adding to its manifest:\n"
            + "  <uses-permission android:name=\"com.vulnlab.app.SENSITIVE_ACTION\" />\n\n"
            + "Caller package: " + getCallingPackage() + "\n\n"
            + "Fix: Change protectionLevel to 'signature'."
        );
    }
}
