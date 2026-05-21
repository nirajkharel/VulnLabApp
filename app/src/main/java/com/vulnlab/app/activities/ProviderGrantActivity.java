package com.vulnlab.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;

public class ProviderGrantActivity extends AppCompatActivity {

    private static final String TAG = "VulnProviderGrant";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_grant);

        TextView tvOutput = findViewById(R.id.tv_output);
        Intent intent = getIntent();

        Uri dataUri = intent.getData();
        if (dataUri == null) dataUri = intent.getParcelableExtra("uri");

        if (dataUri == null) {
            tvOutput.setText("No URI in intent. Send with FLAG_GRANT_PERSISTABLE_URI_PERMISSION.");
            return;
        }

        int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;

        try {
            // VULN: takePersistableUriPermission without any URI allowlist check
            // An attacker URI (e.g. content://com.attacker.app/evil) gets persisted
            Log.d(TAG, "[provider-grant] persisting URI: " + dataUri);
            getContentResolver().takePersistableUriPermission(dataUri, flags);
            tvOutput.setText("Persistable permission taken for:\n" + dataUri
                + "\n\nThis permission survives reboot. "
                + "App will later read this URI in background sync.");
        } catch (SecurityException e) {
            // VULN note: we silently continue even if the take fails
            Log.e(TAG, "takePersistable failed (already held or not offered): " + e.getMessage());
            tvOutput.setText("Error (URI may not have offered persistable grant): "
                + e.getMessage());
        }
    }
}
