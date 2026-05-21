package com.vulnlab.app.activities;

import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

public class KeystoreActivity extends AppCompatActivity {

    private static final String TAG       = "VulnKeystore";
    private static final String KEY_ALIAS = "vulnlab_signing_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keystore);

        TextView tvOutput = findViewById(R.id.tv_output);

        findViewById(R.id.btn_generate).setOnClickListener(v -> {
            try {
                generateKeyWithoutAuthBinding();
                tvOutput.setText("Key generated — NO auth binding. Any code can use it.");
            } catch (Exception e) {
                tvOutput.setText("Error: " + e.getMessage());
            }
        });

        findViewById(R.id.btn_sign).setOnClickListener(v -> {
            try {
                byte[] sig = signWithoutAuthPrompt("hello vulnlab".getBytes());
                tvOutput.setText("Signed without auth: " +
                    Base64.encodeToString(sig, Base64.DEFAULT));
            } catch (Exception e) {
                tvOutput.setText("Error: " + e.getMessage());
            }
        });
    }

    private void generateKeyWithoutAuthBinding() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");

        kpg.initialize(new KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            // VULN: setUserAuthenticationRequired(true) intentionally omitted
            // The key is accessible without any user authentication.
            .build());

        KeyPair kp = kpg.generateKeyPair();
        Log.d(TAG, "Key generated. Public: " +
            Base64.encodeToString(kp.getPublic().getEncoded(), Base64.DEFAULT));
    }

    private byte[] signWithoutAuthPrompt(byte[] data) throws Exception {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);

        PrivateKey privateKey = (PrivateKey) ks.getKey(KEY_ALIAS, null);

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);  // VULN: no BiometricPrompt, no auth validation
        sig.update(data);
        return sig.sign();
    }
}
