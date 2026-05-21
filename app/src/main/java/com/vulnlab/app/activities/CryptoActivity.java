package com.vulnlab.app.activities;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoActivity extends AppCompatActivity {

    private static final String TAG = "VulnCrypto";

    // VULN: hardcoded AES key (also used in ECB demo)
    private static final byte[] HARDCODED_KEY =
        "0123456789abcdef".getBytes(StandardCharsets.UTF_8); // 16 bytes = AES-128

    // VULN: hardcoded IV — all zeros
    private static final byte[] HARDCODED_IV = new byte[16];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypto);

        EditText etPlaintext = findViewById(R.id.et_plaintext);
        EditText etPassword  = findViewById(R.id.et_password);
        TextView tvOutput    = findViewById(R.id.tv_output);

        findViewById(R.id.btn_ecb).setOnClickListener(v -> {
            String pt = etPlaintext.getText().toString();
            tvOutput.setText(encryptEcb(pt));
        });
        findViewById(R.id.btn_cbc_fixed_iv).setOnClickListener(v -> {
            String pt = etPlaintext.getText().toString();
            tvOutput.setText(encryptCbcFixedIv(pt));
        });
        findViewById(R.id.btn_password_key).setOnClickListener(v -> {
            String pt  = etPlaintext.getText().toString();
            String pwd = etPassword.getText().toString();
            tvOutput.setText(encryptWithPasswordKey(pt, pwd));
        });
    }

    /** VULN #1: AES/ECB — no mode specified → defaults to ECB */
    private String encryptEcb(String plaintext) {
        try {
            // VULN: "AES" without mode/padding → AES/ECB/PKCS5Padding on most JCE providers
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec key = new SecretKeySpec(HARDCODED_KEY, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.encodeToString(ct, Base64.DEFAULT);
            Log.d(TAG, "[ECB] ciphertext: " + encoded);
            return "[ECB] " + encoded;
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    /** VULN #2: AES/CBC with hardcoded all-zero IV */
    private String encryptCbcFixedIv(String plaintext) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec key = new SecretKeySpec(HARDCODED_KEY, "AES");
            // VULN: static IV — same plaintext always yields same ciphertext
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(HARDCODED_IV);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.encodeToString(ct, Base64.DEFAULT);
            Log.d(TAG, "[CBC-fixed-IV] ciphertext: " + encoded);
            return "[CBC-fixed-IV] " + encoded;
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    /** VULN #3: password used as AES key via SHA-256, no KDF, no salt */
    private String encryptWithPasswordKey(String plaintext, String password) {
        try {
            // VULN: SHA-256(password) as key — no salt, no iterations, no KDF
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = md.digest(
                password.getBytes(StandardCharsets.UTF_8)); // 32 bytes = AES-256

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(HARDCODED_IV);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.encodeToString(ct, Base64.DEFAULT);
            Log.d(TAG, "[pwd-as-key] password=" + password + " ciphertext=" + encoded);
            return "[pwd-as-key] " + encoded;
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
}
