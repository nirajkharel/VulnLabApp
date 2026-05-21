package com.vulnlab.app.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class NetworkActivity extends AppCompatActivity {

    private static final String TAG = "VulnNetwork";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);

        EditText etUrl    = findViewById(R.id.et_url);
        TextView tvOutput = findViewById(R.id.tv_output);
        etUrl.setText("https://self-signed.badssl.com/");

        // Button 1: HTTPS with broken HostnameVerifier + TrustManager
        findViewById(R.id.btn_broken_tls).setOnClickListener(v -> {
            String url = etUrl.getText().toString();
            new Thread(() -> {
                String result = fetchWithBrokenTls(url);
                runOnUiThread(() -> tvOutput.setText(result));
            }).start();
        });

        // Button 2: Normal HTTPS (uses NSC — user certs trusted by the config)
        findViewById(R.id.btn_normal_https).setOnClickListener(v -> {
            String url = etUrl.getText().toString();
            new Thread(() -> {
                String result = fetchNormal(url);
                runOnUiThread(() -> tvOutput.setText(result));
            }).start();
        });
    }

    private String fetchWithBrokenTls(String urlStr) {
        try {
            // VULN: TrustManager that accepts any certificate
            TrustManager[] trustAll = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] c, String t) {}
                    public void checkServerTrusted(X509Certificate[] c, String t) {
                        // VULN: no validation — accepts self-signed, expired, wrong-CA certs
                        Log.d(TAG, "[broken-tls] trusting cert for: " + c[0].getSubjectDN());
                    }
                }
            };

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAll, null);

            HttpsURLConnection conn = (HttpsURLConnection) new URL(urlStr).openConnection();
            conn.setSSLSocketFactory(sc.getSocketFactory());

            // VULN: HostnameVerifier always returns true — MITM with any cert
            conn.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    Log.d(TAG, "[hostname-verifier] always true for: " + hostname);
                    return true;
                }
            });

            conn.connect();
            BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
            return "[broken TLS] HTTP " + conn.getResponseCode()
                + "\n" + sb.toString().substring(0, Math.min(sb.length(), 300));
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String fetchNormal(String urlStr) {
        try {
            // Uses the NSC — because user certs are trusted in the config,
            // a Burp Suite CA installed in the user cert store is accepted here too
            HttpsURLConnection conn = (HttpsURLConnection) new URL(urlStr).openConnection();
            conn.connect();
            return "[NSC] HTTP " + conn.getResponseCode()
                + " (user certs trusted — see network_security_config.xml)";
        } catch (Exception e) {
            return "NSC fetch error: " + e.getMessage();
        }
    }
}
