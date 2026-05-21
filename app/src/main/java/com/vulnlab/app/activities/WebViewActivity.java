package com.vulnlab.app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebViewActivity extends AppCompatActivity {

    private WebView webView;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        webView = findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();

        // VULN: JavaScript enabled
        settings.setJavaScriptEnabled(true);

        // VULN: file:// access to the WebView's origin
        settings.setAllowFileAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);

        // VULN: cookie-manager-cross-origin — third-party + file:// cookies
        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(webView, true);

        // VULN: @JavascriptInterface RCE bridge
        webView.addJavascriptInterface(new NativeBridge(), "Android");

        webView.setWebViewClient(new WebViewClient());

        // VULN: jwt-token-fragment-leak — token appended to URL fragment
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMTIzIn0.SIG";
        String baseUrl = "https://app.vulnlabapp.example.com/dashboard";
        String urlWithFragment = baseUrl + "#access_token=" + token;

        // Prefer URL from intent extra — VULN: no scheme validation
        Intent intent = getIntent();
        if (intent.hasExtra("url")) {
            // VULN: javascript: scheme allowed — beyond-webview-redirect
            webView.loadUrl(intent.getStringExtra("url"));
        } else {
            webView.loadUrl(urlWithFragment);
        }

        // SSRF button: fetches arbitrary URL from user input
        Button btnFetch = findViewById(R.id.btn_fetch);
        EditText etFetchUrl = findViewById(R.id.et_fetch_url);

        if (intent.hasExtra("fetch")) {
            etFetchUrl.setText(intent.getStringExtra("fetch"));
        }

        btnFetch.setOnClickListener(v -> {
            String target = etFetchUrl.getText().toString();
            // VULN: http-fetch-ssrf — no URL validation, fetches any URL
            new Thread(() -> {
                try {
                    URL url = new URL(target);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line).append("\n");
                    String response = sb.toString();
                    runOnUiThread(() -> Toast.makeText(this,
                        "SSRF response (first 200):\n" + response.substring(0,
                            Math.min(200, response.length())),
                        Toast.LENGTH_LONG).show());
                } catch (IOException e) {
                    runOnUiThread(() -> Toast.makeText(this,
                        "SSRF error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }).start();
        });
    }

    private static class NativeBridge {

        @JavascriptInterface
        public String exec(String cmd) {
            try {
                // VULN: arbitrary OS command execution from JavaScript
                Process p = Runtime.getRuntime().exec(new String[]{ "sh", "-c", cmd });
                BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line).append("\n");
                return sb.toString();
            } catch (IOException e) {
                return "error: " + e.getMessage();
            }
        }

        @JavascriptInterface
        public String readFile(String path) {
            // VULN: webview-file-scheme-arbitrary-read — direct file read bridge
            try {
                java.io.File f = new java.io.File(path);
                byte[] bytes = new java.io.FileInputStream(f).readAllBytes();
                return new String(bytes);
            } catch (Exception e) {
                return "error: " + e.getMessage();
            }
        }

        @JavascriptInterface
        public void sendToAttacker(String data) {
            // Simulates exfil — in a real attack, fetch() to attacker server
            android.util.Log.d("EXFIL", "Data: " + data);
        }
    }
}
