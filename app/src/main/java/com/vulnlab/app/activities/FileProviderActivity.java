package com.vulnlab.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.vulnlab.app.R;

import java.io.File;

public class FileProviderActivity extends AppCompatActivity {

    private static final String TAG      = "VulnFileProvider";
    private static final String AUTHORITY = "com.vulnlab.app.fileprovider";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_provider);

        EditText etPath = findViewById(R.id.et_share_path);
        TextView tvOutput = findViewById(R.id.tv_output);

        // VULN: attacker can inject file_path via intent extra — no validation
        Intent intent = getIntent();
        if (intent.hasExtra("file_path")) {
            etPath.setText(intent.getStringExtra("file_path"));
        } else {
            etPath.setText("/data/data/com.vulnlab.app/shared_prefs/auth_prefs.xml");
        }

        findViewById(R.id.btn_share).setOnClickListener(v -> {
            String path = etPath.getText().toString();
            try {
                File file = new File(path);
                // VULN: FileProvider's root-path is "/" so ANY file resolves to a valid URI
                Uri contentUri = FileProvider.getUriForFile(this, AUTHORITY, file);
                Log.d(TAG, "[file-provider] sharing URI: " + contentUri + " for path: " + path);

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("*/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Share via"));

                tvOutput.setText("Share intent sent for URI:\n" + contentUri);
            } catch (Exception e) {
                tvOutput.setText("Error: " + e.getMessage());
            }
        });
    }
}
