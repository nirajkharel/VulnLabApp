package com.vulnlab.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.activities.*;

/**
 * Hub activity — lists every vulnerability demo. Tap to launch.
 */
public class MainActivity extends AppCompatActivity {

    private static final String[][] DEMOS = {
        // { label, activity class name }
        { "Login (PII Logging)",                  "LoginActivity" },
        { "WebView (file://, JSI, Cookie, SSRF)",  "WebViewActivity" },
        { "Crypto Misuse (ECB / hardcoded IV)",    "CryptoActivity" },
        { "Keystore (no auth binding)",            "KeystoreActivity" },
        { "Command Injection (Runtime.exec)",      "CommandInjectionActivity" },
        { "Intent Redirector (fwd nested intent)", "IntentRedirectorActivity" },
        { "File Write via Intent",                 "FileWriteActivity" },
        { "Dynamic Code Loading (DexClassLoader)", "DynamicCodeActivity" },
        { "Reflection / Class.forName",            "ReflectionActivity" },
        { "OAuth Callback (intent redirect)",      "OAuthCallbackActivity" },
        { "Stream URI Read (openInputStream)",     "StreamUriActivity" },
        { "FileProvider (overbroad root path)",    "FileProviderActivity" },
        { "Provider Grant Escalation",             "ProviderGrantActivity" },
        { "Notifications (mutable PI / spoof)",    "NotificationActivity" },
        { "Task Hijacking (StrandHogg 2.0)",       "TaskHijackActivity" },
        { "Network (HostnameVerifier / NSC)",      "NetworkActivity" },
        { "Fragment Injection (PreferenceActivity)","VulnPreferenceActivity" },
        { "Nav Component Arg Injection",           "NavHostActivity" },
        { "Root / Frida Detection (bypassable)",   "DetectionActivity" },
        { "Deep Link (autoVerify=false)",          "DeepLinkActivity" },
        { "Cross-App ClassLoader (Valsamaras)",    "CrossAppClassLoaderActivity" },
        { "Play Integrity (local decode)",         "PlayIntegrityActivity" },
        { "Cordova Bridge RCE",                    "CordovaBridgeActivity" },
        { "React Native Bridge Inspection",        "ReactNativeBridgeActivity" },
        { "Implicit Broadcast Leak",               "ImplicitBroadcastActivity" },
        { "Sticky Broadcast Eavesdropping",        "StickyBroadcastActivity" },
        { "Janus / v1-only Signing Info",          "JanusInfoActivity" },
        { "Weak Custom Permission",                "WeakPermissionActivity" },
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] labels = new String[DEMOS.length];
        for (int i = 0; i < DEMOS.length; i++) labels[i] = DEMOS[i][0];

        ListView list = findViewById(R.id.demo_list);
        list.setAdapter(new ArrayAdapter<>(this,
            android.R.layout.simple_list_item_1, labels));

        list.setOnItemClickListener((parent, view, position, id) -> {
            try {
                Class<?> cls = Class.forName(
                    "com.vulnlab.app.activities." + DEMOS[position][1]);
                startActivity(new Intent(this, cls));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }
}
