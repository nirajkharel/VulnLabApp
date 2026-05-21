package com.vulnlab.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;

import java.io.File;
import java.io.FileOutputStream;

public class FileWriteActivity extends AppCompatActivity {

    private static final String TAG = "VulnFileWrite";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_write);

        TextView tvOutput = findViewById(R.id.tv_output);
        Intent intent = getIntent();

        // ── Pattern 1: filename from extra ───────────────────────────────
        if (intent.hasExtra("filename") && intent.hasExtra("content")) {
            String filename = intent.getStringExtra("filename");
            String content  = intent.getStringExtra("content");

            try {
                // VULN: no canonicalization — "../databases/main.db" escapes filesDir
                File outFile = new File(getFilesDir(), filename);
                Log.d(TAG, "[pattern1] writing to: " + outFile.getAbsolutePath());
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    fos.write(content.getBytes());
                }
                tvOutput.setText("Pattern 1 written: " + outFile.getAbsolutePath());
            } catch (Exception e) {
                tvOutput.setText("Error: " + e.getMessage());
            }
            return;
        }

        // ── Pattern 2: type-switch with attacker-controlled payload ─────
        if (intent.hasExtra("type") && intent.hasExtra("payload")) {
            String type    = intent.getStringExtra("type");
            byte[] payload = intent.getByteArrayExtra("payload");
            File outFile;

            switch (type != null ? type : "") {
                case "avatar":  outFile = new File(getFilesDir(), "avatar.dat"); break;
                // VULN: writing to session.json overwrites the active session
                case "session": outFile = new File(getFilesDir(), "session.json"); break;
                case "config":  outFile = new File(getFilesDir(), "remote_config.json"); break;
                default:
                    tvOutput.setText("Unknown type: " + type);
                    return;
            }

            try {
                Log.d(TAG, "[pattern2] writing to: " + outFile.getAbsolutePath());
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    fos.write(payload);
                }
                tvOutput.setText("Pattern 2 written: " + outFile.getAbsolutePath());
            } catch (Exception e) {
                tvOutput.setText("Error: " + e.getMessage());
            }
            return;
        }

        // ── Pattern 3: URI copy with path traversal ───────────────────
        if (intent.hasExtra("src_uri") && intent.hasExtra("out_path")) {
            android.net.Uri srcUri = intent.getParcelableExtra("src_uri");
            String outPath = intent.getStringExtra("out_path");

            try {
                byte[] data = getContentResolver()
                    .openInputStream(srcUri).readAllBytes();
                // VULN: outPath not canonicalized — "../shared_prefs/auth.xml" possible
                File outFile = new File(getFilesDir(), outPath);
                Log.d(TAG, "[pattern3] copying URI to: " + outFile.getAbsolutePath());
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    fos.write(data);
                }
                tvOutput.setText("Pattern 3 written: " + outFile.getAbsolutePath());
            } catch (Exception e) {
                tvOutput.setText("Error: " + e.getMessage());
            }
            return;
        }

        tvOutput.setText("Send 'filename'+'content', 'type'+'payload', or 'src_uri'+'out_path' extras.");
    }
}
