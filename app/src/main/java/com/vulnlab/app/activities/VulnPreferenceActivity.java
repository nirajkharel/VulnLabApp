package com.vulnlab.app.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

@SuppressWarnings("deprecation")
public class VulnPreferenceActivity extends PreferenceActivity {

    private static final String TAG = "VulnFragmentInject";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "[fragment-injection] launched with: "
            + getIntent().getStringExtra(":android:show_fragment"));
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        Log.d(TAG, "[fragment-injection] isValidFragment called for: " + fragmentName);
        return true;  // VULN: should be: return MyAllowedFragment.class.getName().equals(fragmentName)
    }
}
