package com.vulnlab.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;

public class OAuthCallbackActivity extends AppCompatActivity {

    private static final String TAG = "VulnOAuth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth_callback);

        TextView tvOutput = findViewById(R.id.tv_output);
        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data == null) {
            tvOutput.setText("No callback URI received.");
            return;
        }

        // VULN: authorization code logged — any app with READ_LOGS captures it
        String code  = data.getQueryParameter("code");
        String state = data.getQueryParameter("state");
        String error = data.getQueryParameter("error");

        Log.d(TAG, "OAuth callback received. code=" + code + " state=" + state);

        if (error != null) {
            tvOutput.setText("OAuth error: " + error);
            return;
        }

        // VULN: state parameter never validated against the originally sent state
        // CSRF is possible — attacker tricks the user into completing an
        // authorization flow for the attacker's account.
        if (state == null) {
            Log.w(TAG, "No state returned — CSRF check skipped");
        }

        // Simulate token exchange — fake access token for demo purposes
        String accessToken = "fake_access_token_abc123xyz789_" + code;
        String refreshToken = "fake_refresh_token_def456uvw012";

        // VULN: tokens logged — logcat leakage
        Log.d(TAG, "Access token: " + accessToken);
        Log.d(TAG, "Refresh token: " + refreshToken);

        // VULN: jwt-token-fragment-leak — token appended to URL fragment and
        //        passed to WebViewActivity which loads it in a WebView
        String webUrl = "https://app.vulnlabapp.example.com/home#token=" + accessToken;
        Intent webIntent = new Intent(this, WebViewActivity.class);
        webIntent.putExtra("url", webUrl);
        startActivity(webIntent);

        tvOutput.setText("OAuth code: " + code + "\nToken: " + accessToken);
    }
}
