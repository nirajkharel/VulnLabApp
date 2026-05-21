package com.vulnlab.app.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CordovaBridgeActivity extends AppCompatActivity {

    private static final String TAG = "VulnCordovaBridge";

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cordova_bridge);

        WebView webView = findViewById(R.id.cordova_webview);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowUniversalAccessFromFileURLs(true);

        // VULN: bridge exposed to JavaScript — exec() and readFile() callable from JS
        webView.addJavascriptInterface(new CordovaBridge(), "cordovaBridge");
        webView.setWebViewClient(new WebViewClient());

        // Load the bundled HTML page that uses the bridge
        webView.loadUrl("file:///android_asset/cordova_app.html");
    }

    /** Simulates Cordova plugin dispatch bridge */
    private static class CordovaBridge {

        @JavascriptInterface
        public String exec(String plugin, String action, String arg) {
            Log.d(TAG, "[cordova-bridge] exec plugin=" + plugin
                + " action=" + action + " arg=" + arg);

            switch (plugin + "." + action) {

                case "System.exec":
                    // VULN: shell command execution from JavaScript
                    return runShell(arg);

                case "FilePlugin.readFile":
                    // VULN: arbitrary file read from JavaScript
                    return readFile(arg);

                case "Device.getInfo":
                    return "{\"platform\":\"Android\",\"uuid\":\"vulnlab-uuid\"}";

                default:
                    return "{\"error\":\"unknown plugin\"}";
            }
        }

        private String runShell(String cmd) {
            try {
                Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
                BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line).append("\n");
                return sb.toString();
            } catch (Exception e) {
                return "error: " + e.getMessage();
            }
        }

        private String readFile(String path) {
            try {
                byte[] bytes = new java.io.FileInputStream(path).readAllBytes();
                return new String(bytes);
            } catch (Exception e) {
                return "error: " + e.getMessage();
            }
        }
    }
}
