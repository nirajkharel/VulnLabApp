package com.vulnlab.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CommandInjectionActivity extends AppCompatActivity {

    private static final String TAG = "VulnCmdInjection";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command_injection);

        EditText etCmd = findViewById(R.id.et_cmd);
        TextView tvOutput = findViewById(R.id.tv_output);

        // Pre-fill from intent extra — attacker surface
        Intent intent = getIntent();
        if (intent.hasExtra("cmd")) {
            etCmd.setText(intent.getStringExtra("cmd"));
        }

        // Pattern 1 — exec(String) with user input
        findViewById(R.id.btn_exec_string).setOnClickListener(v -> {
            String cmd = etCmd.getText().toString();
            tvOutput.setText(runExecString(cmd));
        });

        // Pattern 2 — exec via shell wrapper (most dangerous)
        findViewById(R.id.btn_exec_shell).setOnClickListener(v -> {
            String cmd = etCmd.getText().toString();
            tvOutput.setText(runExecShell(cmd));
        });

        // Pattern 3 — ProcessBuilder with joined user input
        findViewById(R.id.btn_process_builder).setOnClickListener(v -> {
            String cmd = etCmd.getText().toString();
            tvOutput.setText(runProcessBuilder(cmd));
        });
    }

    /** VULN Pattern 1: exec(String) — user controls the full command string */
    private String runExecString(String userInput) {
        try {
            // VULN: userInput is split on whitespace; first element is the binary.
            // Not as dangerous as Pattern 2 but first arg can be a malicious binary.
            Process p = Runtime.getRuntime().exec(userInput);
            return readProcess(p);
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    /** VULN Pattern 2: sh -c <user_input> — full shell injection */
    private String runExecShell(String userInput) {
        try {
            // VULN: passing user input to sh -c gives full shell metacharacter expansion
            Process p = Runtime.getRuntime().exec(new String[]{ "sh", "-c", userInput });
            Log.d(TAG, "[shell-injection] executing: sh -c " + userInput);
            return readProcess(p);
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    /** VULN Pattern 3: ProcessBuilder — developer joined user tokens into one string */
    private String runProcessBuilder(String userInput) {
        try {
            // VULN: developer intended to ping an IP, but userInput is unsanitized.
            // Attacker sends "8.8.8.8; id" and ProcessBuilder executes it via sh.
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", "ping -c 1 " + userInput);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            Log.d(TAG, "[ProcessBuilder] ping -c 1 " + userInput);
            return readProcess(p);
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    private String readProcess(Process p) throws Exception {
        BufferedReader br = new BufferedReader(
            new InputStreamReader(p.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append("\n");
        return sb.length() > 0 ? sb.toString() : "(no output)";
    }
}
