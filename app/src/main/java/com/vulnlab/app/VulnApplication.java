package com.vulnlab.app;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Application entry point.
 * Initialises a singleton SharedPreferences store that multiple
 * activities write credentials and tokens into — intentionally
 * exposed for backup / logcat harvest scenarios.
 */
public class VulnApplication extends Application {

    private static final String TAG = "VulnLabApp";

    public static final String PREFS_AUTH   = "auth_prefs";
    public static final String KEY_TOKEN    = "session_token";
    public static final String KEY_EMAIL    = "user_email";
    public static final String KEY_PASSWORD = "user_password";   // VULN: plaintext password in prefs
    public static final String KEY_API_KEY  = "api_key";

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;

        // VULN: logging-pii-in-release — hardcoded secrets logged at startup
        Log.d(TAG, "VulnLabApp started. API key: sk-prod-8f3k2j9x0q1w5e6r");
        Log.d(TAG, "DB connection: jdbc:mysql://internal.vulnlab.corp:3306/prod?user=admin&password=S3cr3t!");
        Log.d(TAG, "OAuth client secret: oauth_secret_abc123xyz789");
    }

    public static Context getContext() {
        return sContext;
    }
}
