package com.vulnlab.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

// VULN: provider-grant-escalation (Direction B). This exported activity hands the
// caller a PERSISTABLE read grant on our OWN private SecretProvider. Any app that
// launches it for a result takes the grant and reads the secret forever — even
// though SecretProvider is exported=false.
public class ShareSecretActivity extends AppCompatActivity {

    private static final String TAG = "VulnShareSecret";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri secret = Uri.parse("content://com.vulnlab.app.secret/token");
        Intent result = new Intent();
        result.setData(secret);
        result.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                      | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);   // VULN: persistable
        Log.d(TAG, "[provider-grant-B] granting persistable read on " + secret);
        setResult(RESULT_OK, result);
        finish();
    }
}
