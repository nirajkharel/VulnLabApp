package com.vulnlab.app.activities;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;

import org.json.JSONObject;

public class PlayIntegrityActivity extends AppCompatActivity {

    private static final String TAG = "VulnPlayIntegrity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_integrity);

        TextView tvOutput = findViewById(R.id.tv_output);

        // NOTE: Actual Play Integrity API call requires Google Play Services.
        // We simulate the vulnerable local-decode pattern with a static token.

        // Simulated token (JWS format: header.payload.sig, base64url-encoded)
        String fakePayload = Base64.encodeToString(
            ("{\"requestDetails\":{\"requestPackageName\":\"com.vulnlab.app\"},"
            + "\"appIntegrity\":{\"appRecognitionVerdict\":\"PLAY_RECOGNIZED\"},"
            + "\"deviceIntegrity\":{\"deviceRecognitionVerdict\":[\"MEETS_DEVICE_INTEGRITY\"]},"
            + "\"accountDetails\":{\"appLicensingVerdict\":\"LICENSED\"}}").getBytes(),
            Base64.URL_SAFE | Base64.NO_PADDING);

        String fakeToken = "eyJhbGciOiJFUzI1NiJ9." + fakePayload + ".FAKE_SIGNATURE";

        Button btnCheck = findViewById(R.id.btn_check_integrity);
        String finalFakeToken = fakeToken;
        btnCheck.setOnClickListener(v -> {
            boolean ok = evaluateVerdictLocally(finalFakeToken);
            tvOutput.setText(ok
                ? "✓ Integrity check passed (LOCAL — BYPASSABLE)\n\nToken decoded locally.\n\n"
                  + "Frida bypass:\n  Java.use('com.vulnlab.app.activities.PlayIntegrityActivity')\n"
                  + "    .evaluateVerdictLocally.implementation = t => true;"
                : "✗ Integrity check failed");
        });
    }

    private boolean evaluateVerdictLocally(String token) {
        try {
            // Split JWS: header.payload.signature
            String[] parts = token.split("\\.");
            if (parts.length < 2) return false;

            // VULN: local base64 decode of the payload — no signature verification
            byte[] payloadBytes = Base64.decode(parts[1],
                Base64.URL_SAFE | Base64.NO_PADDING);
            String payloadJson = new String(payloadBytes);
            Log.d(TAG, "[play-integrity] local decode: " + payloadJson);

            JSONObject payload = new JSONObject(payloadJson);
            JSONObject deviceIntegrity = payload.getJSONObject("deviceIntegrity");
            String verdict = deviceIntegrity.getJSONArray("deviceRecognitionVerdict")
                .getString(0);

            // VULN: gate on locally-read verdict — trivially spoofed
            return "MEETS_DEVICE_INTEGRITY".equals(verdict);
        } catch (Exception e) {
            Log.e(TAG, "Token decode error: " + e.getMessage());
            return false;
        }
    }
}
