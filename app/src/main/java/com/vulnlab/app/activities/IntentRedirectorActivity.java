package com.vulnlab.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;

public class IntentRedirectorActivity extends AppCompatActivity {

    private static final String TAG = "VulnIntentRedirect";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intent_redirector);

        TextView tvOutput = findViewById(R.id.tv_output);
        Intent incoming = getIntent();

        // ── Pattern 1: raw nested Intent forwarding ───────────────────────
        if (incoming.hasExtra("next_intent")) {
            // VULN: forwards any intent, including to non-exported activities
            Intent next = incoming.getParcelableExtra("next_intent");
            if (next != null) {
                Log.d(TAG, "[intent-redirection] forwarding to: " + next.getComponent());
                tvOutput.setText("Forwarding intent to: " + next.getComponent());
                try {
                    startActivity(next);
                } catch (Exception e) {
                    tvOutput.setText("Error forwarding: " + e.getMessage());
                }
                finish();
                return;
            }
        }

        // ── Pattern 2: Parcelable-driven intent construction ─────────────
        if (incoming.hasExtra("pending_action")) {
            // VULN: parcelable-redirection — deserialize and trust caller's data
            com.vulnlab.app.models.UserAction action =
                incoming.getParcelableExtra("pending_action");
            if (action != null) {
                Log.d(TAG, "[parcelable-redirect] action: target=" + action.targetActivity
                    + " data=" + action.payloadData);
                try {
                    Intent constructed = new Intent();
                    constructed.setClassName(getPackageName(), action.targetActivity);
                    constructed.putExtra("data", action.payloadData);
                    startActivity(constructed);
                } catch (Exception e) {
                    tvOutput.setText("Error: " + e.getMessage());
                }
                finish();
                return;
            }
        }

        // ── Pattern 3: target string dispatch ──────────────────────────
        String target = incoming.getStringExtra("target");
        if (target != null) {
            // VULN: DONE-beyond-webview-redirect — arbitrary class name, forwarded
            try {
                Class<?> cls = Class.forName("com.vulnlab.app.activities." + target);
                Intent next = new Intent(this, cls);
                next.putExtras(incoming);  // VULN: forwards ALL extras including sensitive ones
                startActivity(next);
            } catch (ClassNotFoundException e) {
                tvOutput.setText("Unknown target: " + target);
            }
            finish();
            return;
        }

        tvOutput.setText("IntentRedirectorActivity: no target specified");
    }
}
