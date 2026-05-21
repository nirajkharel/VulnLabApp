package com.vulnlab.app.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

public class ReactNativeBridgeActivity extends AppCompatActivity {

    private static final String TAG = "VulnRNBridge";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_react_native_bridge);

        TextView tvOutput = findViewById(R.id.tv_output);
        Button btnDump = findViewById(R.id.btn_dump_modules);

        btnDump.setOnClickListener(v -> {
            tvOutput.setText(
                "Simulated NativeModule registry dump:\n\n"
                + "Module: SystemModule\n"
                + "  Methods: exec(String) → String\n"
                + "           runShell(String) → String\n\n"
                + "Module: AuthModule\n"
                + "  Methods: getStoredToken() → String\n"
                + "           storeCredentials(String, String)\n\n"
                + "Module: FileModule\n"
                + "  Methods: readFile(String) → String\n"
                + "           writeFile(String, String)\n\n"
                + "Module: NetworkModule\n"
                + "  Methods: fetch(String) → String\n\n"
                + "=== Frida hook to dump NativeModules ===\n"
                + "const CatalystInstance = Java.use('com.facebook.react.bridge.CatalystInstanceImpl');\n"
                + "CatalystInstance.callFunction.implementation = function(mod, method, args) {\n"
                + "  console.log('[RN-bridge]', mod, method, JSON.stringify(args));\n"
                + "  return this.callFunction(mod, method, args);\n"
                + "};"
            );
        });

        // Simulate @ReactMethod — these would be registered NativeModule methods
        Button btnExec = findViewById(R.id.btn_exec_module);
        btnExec.setOnClickListener(v -> {
            // VULN: @ReactMethod exec — callable from any JS bundle
            String result = nativeExec("id");
            Log.d(TAG, "[RN-bridge] SystemModule.exec result: " + result);
            tvOutput.setText("SystemModule.exec('id') = " + result);
        });

        Button btnToken = findViewById(R.id.btn_get_token);
        btnToken.setOnClickListener(v -> {
            // VULN: @ReactMethod getStoredToken — callable from JS
            String token = nativeGetStoredToken();
            Log.d(TAG, "[RN-bridge] AuthModule.getStoredToken = " + token);
            tvOutput.setText("AuthModule.getStoredToken() = " + token);
        });
    }

    // Simulated @ReactMethod implementations

    private String nativeExec(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
            BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
            return sb.toString().trim();
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    private String nativeGetStoredToken() {
        // VULN: returns plaintext token from SharedPreferences to JS bridge
        return getSharedPreferences("auth_prefs", MODE_PRIVATE)
            .getString("session_token", "no-token");
    }
}
