package com.vulnlab.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

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

    private static final int PERMS_REQUEST_CODE = 0xC0DE;

    private static final String[] RUNTIME_PERMS = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestRuntimePermissions();

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

    private void requestRuntimePermissions() {
        List<String> missing = new ArrayList<>();
        for (String p : RUNTIME_PERMS) {
            if (ContextCompat.checkSelfPermission(this, p)
                    != PackageManager.PERMISSION_GRANTED) {
                missing.add(p);
            }
        }
        if (!missing.isEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missing.toArray(new String[0]),
                PERMS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // VulnLabApp does not branch on the result. A real app would handle
        // the denied case here (e.g. disable a feature, show rationale).
    }
}
