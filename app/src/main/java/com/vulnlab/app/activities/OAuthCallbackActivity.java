package com.vulnlab.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class OAuthCallbackActivity extends AppCompatActivity {

    private static final String TAG = "VulnOAuth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth_callback);

        TextView tvOutput = findViewById(R.id.tv_output);
        Intent intent = getIntent();

        // VULN: parcelable-redirection escalation — if callback_url is injected via
        // IntentRedirectorActivity's next_intent chain, POST the stored session token
        // to the attacker's server. "data" is the fallback key from the pending_action chain.
        String callbackUrl = intent.getStringExtra("callback_url");
        if (callbackUrl == null) callbackUrl = intent.getStringExtra("data");
        if (callbackUrl != null) {
            final String token = getSharedPreferences("auth_prefs", MODE_PRIVATE)
                .getString("session_token", "no-token");
            Log.d(TAG, "[oauth-callback-leak] POSTing token to: " + callbackUrl);
            tvOutput.setText("[oauth-callback-leak] Posting token to: " + callbackUrl);
            final String finalUrl = callbackUrl;
            new Thread(() -> {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(finalUrl).openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    byte[] body = ("token=" + URLEncoder.encode(token, "UTF-8")).getBytes("UTF-8");
                    conn.getOutputStream().write(body);
                    conn.getResponseCode();
                    conn.disconnect();
                } catch (Exception ignored) {}
            }).start();
            return;
        }

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

    // VULN: public method callable via Class.forName reflection (ReflectionActivity demo)
    public String process(String url) {
        Log.d(TAG, "[oauth-process] callback_url=" + url);
        return "callback_processed: " + url;
    }
}
