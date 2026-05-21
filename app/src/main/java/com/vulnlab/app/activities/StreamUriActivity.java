package com.vulnlab.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;

import java.io.InputStream;

public class StreamUriActivity extends AppCompatActivity {

    private static final String TAG = "VulnStreamUri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_uri);

        TextView tvOutput = findViewById(R.id.tv_output);
        Intent intent = getIntent();

        Uri targetUri = intent.getParcelableExtra("read_uri");
        if (targetUri == null && intent.getData() != null) {
            targetUri = intent.getData();
        }

        if (targetUri == null) {
            tvOutput.setText("No 'read_uri' extra supplied.\n"
                + "Send a content:// or file:// URI to read.");
            return;
        }

        try {
            // VULN: opens any URI the caller specifies — no allowlist, no path check
            Log.d(TAG, "[stream-uri-read] opening: " + targetUri);
            InputStream is = getContentResolver().openInputStream(targetUri);
            if (is == null) {
                tvOutput.setText("openInputStream returned null for: " + targetUri);
                return;
            }
            byte[] bytes = is.readAllBytes();
            is.close();

            String preview = new String(bytes, 0, Math.min(bytes.length, 512));
            Log.d(TAG, "[stream-uri-read] read " + bytes.length + " bytes");
            tvOutput.setText("Read " + bytes.length + " bytes from: " + targetUri
                + "\n\nPreview:\n" + preview);
        } catch (Exception e) {
            tvOutput.setText("Error reading URI: " + e.getMessage());
        }
    }
}
