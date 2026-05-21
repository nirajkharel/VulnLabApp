package com.vulnlab.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.vulnlab.app.R;

public class NavHostActivity extends AppCompatActivity {

    private static final String TAG = "VulnNavHost";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_host);

        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null) {
            String url = data.getQueryParameter("url");
            Log.d(TAG, "[nav-component] deep link url arg: " + url);
            // The NavController routes to WebViewFragment with this arg
            // No validation — javascript: and file:// are accepted
        }
    }
}
