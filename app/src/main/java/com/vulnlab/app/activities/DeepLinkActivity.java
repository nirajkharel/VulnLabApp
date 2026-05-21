package com.vulnlab.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;

public class DeepLinkActivity extends AppCompatActivity {

    private static final String TAG = "VulnDeepLink";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_link);

        TextView tvOutput = findViewById(R.id.tv_output);
        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data == null) {
            tvOutput.setText("No URI. Send:\n"
                + "  https://app.vulnlabapp.example.com/open?token=xxx&redirect=xxx");
            return;
        }

        // VULN: token from URI query param — logged (logcat leakage)
        String token    = data.getQueryParameter("token");
        String redirect = data.getQueryParameter("redirect");
        String action   = data.getQueryParameter("action");

        Log.d(TAG, "[deep-link] token=" + token + " redirect=" + redirect
            + " action=" + action);

        // VULN: open redirect — redirect URL not validated
        if (redirect != null) {
            Intent webIntent = new Intent(this, WebViewActivity.class);
            webIntent.putExtra("url", redirect);   // VULN: any URL including javascript:
            startActivity(webIntent);
        }

        tvOutput.setText("Deep link received (no autoVerify):\n" + data.toString()
            + "\n\nToken: " + token
            + "\n\nAny app can intercept this URL."
            + "\nCheck adb logcat for token leakage.");
    }
}
