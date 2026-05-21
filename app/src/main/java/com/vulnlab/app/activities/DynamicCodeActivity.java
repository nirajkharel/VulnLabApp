package com.vulnlab.app.activities;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;

import java.io.File;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class DynamicCodeActivity extends AppCompatActivity {

    private static final String TAG = "VulnDynamicCode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_code);

        EditText etDexPath = findViewById(R.id.et_dex_path);
        TextView tvOutput  = findViewById(R.id.tv_output);

        // Default to the externally-writable path
        etDexPath.setText(
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/plugin.dex");

        findViewById(R.id.btn_load).setOnClickListener(v -> {
            String dexPath = etDexPath.getText().toString();
            tvOutput.setText(loadAndRunDex(dexPath));
        });
    }

    private String loadAndRunDex(String dexPath) {
        File dexFile = new File(dexPath);
        if (!dexFile.exists()) {
            return "DEX not found at: " + dexPath + "\n"
                + "Push a payload: adb push payload.dex " + dexPath;
        }

        // VULN: loading from external storage — attacker-controlled path
        File optimizedDir = getDir("dex_opt", MODE_PRIVATE);
        Log.d(TAG, "[dynamic-code-loading] loading DEX: " + dexPath);

        try {
            DexClassLoader loader = new DexClassLoader(
                dexPath,
                optimizedDir.getAbsolutePath(),
                null,                              // librarySearchPath
                getClassLoader());                 // parent

            // VULN: reflectively calls run() on the loaded class
            Class<?> pluginClass = loader.loadClass("com.vulnlab.plugin.Payload");
            Method runMethod = pluginClass.getMethod("run");
            Object instance = pluginClass.newInstance();
            Object result = runMethod.invoke(instance);
            Log.d(TAG, "[dynamic-code-loading] result: " + result);
            return "Loaded and executed. Result: " + result;
        } catch (Exception e) {
            Log.e(TAG, "[dynamic-code-loading] error", e);
            return "Error loading/executing: " + e.getMessage();
        }
    }
}
