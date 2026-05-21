package com.vulnlab.app.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

public class DetectionActivity extends AppCompatActivity {

    private static final String TAG = "VulnDetection";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);

        TextView tvOutput = findViewById(R.id.tv_output);
        StringBuilder sb = new StringBuilder();

        sb.append("=== Root Detection ===\n");
        sb.append("root files:   ").append(checkRootFilesystem()).append("\n");
        sb.append("build tags:   ").append(checkBuildTags()).append("\n");
        sb.append("su binary:    ").append(checkSuBinary()).append("\n");
        sb.append("magisk:       ").append(checkMagisk()).append("\n\n");

        sb.append("=== Frida Detection ===\n");
        sb.append("port 27042:   ").append(checkFridaPort()).append("\n");
        sb.append("/proc/maps:   ").append(checkProcMapsForFrida()).append("\n");
        sb.append("gadget name:  ").append(checkFridaGadgetName()).append("\n\n");

        sb.append("=== Anti-Debug ===\n");
        sb.append("debugger:     ").append(checkDebugger()).append("\n");
        sb.append("TracerPid:    ").append(checkTracerPid()).append("\n\n");

        sb.append("All checks above are hookable with Frida.\n"
            + "Use Frida hooks to return false from each check to bypass.");

        tvOutput.setText(sb.toString());
    }

    // ── Root Detection ─────────────────────────────────────────────────────

    /** VULN (root): filesystem check — hookable */
    private boolean checkRootFilesystem() {
        String[] paths = {
            "/system/app/Superuser.apk", "/system/xbin/su",
            "/system/bin/su", "/sbin/su",
            "/data/local/xbin/su", "/data/local/bin/su",
            "/system/sd/xbin/su", "/system/bin/failsafe/su",
            "/data/local/su"
        };
        for (String path : paths) {
            if (new File(path).exists()) {
                Log.d(TAG, "[root] found: " + path);
                return true;
            }
        }
        return false;
    }

    /** VULN (root): build tags check — test-keys indicates custom ROM */
    private boolean checkBuildTags() {
        String tags = android.os.Build.TAGS;
        return tags != null && tags.contains("test-keys");
    }

    /** VULN (root): run "which su" — hookable */
    private boolean checkSuBinary() {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"which", "su"});
            BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream()));
            String line = br.readLine();
            return line != null && !line.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /** VULN (root): Magisk manager package check */
    private boolean checkMagisk() {
        try {
            getPackageManager().getPackageInfo("com.topjohnwu.magisk", 0);
            return true;
        } catch (Exception e) {}
        try {
            getPackageManager().getPackageInfo("io.github.huskydg.magisk", 0);
            return true;
        } catch (Exception e) {}
        return false;
    }

    // ── Frida Detection ────────────────────────────────────────────────────

    /** VULN (frida): port 27042 check — default Frida server port */
    private boolean checkFridaPort() {
        try {
            java.net.Socket s = new java.net.Socket();
            s.connect(new java.net.InetSocketAddress("127.0.0.1", 27042), 100);
            s.close();
            Log.d(TAG, "[frida] port 27042 open");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** VULN (frida): /proc/self/maps search — gadget leaves "frida" in map names */
    private boolean checkProcMapsForFrida() {
        try {
            BufferedReader br = new BufferedReader(
                new java.io.FileReader("/proc/self/maps"));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("frida") || line.contains("linjector")) {
                    Log.d(TAG, "[frida] found in /proc/self/maps: " + line);
                    return true;
                }
            }
        } catch (Exception e) {}
        return false;
    }

    /** VULN (frida): checks for "gum-js-loop" thread name — Frida JS runtime */
    private boolean checkFridaGadgetName() {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().contains("gum-js-loop") ||
                t.getName().contains("gmain")        ||
                t.getName().contains("gdbus")) {
                Log.d(TAG, "[frida] suspicious thread: " + t.getName());
                return true;
            }
        }
        return false;
    }

    // ── Anti-Debug ─────────────────────────────────────────────────────────

    /** VULN (debug): android.os.Debug.isDebuggerConnected() — hookable */
    private boolean checkDebugger() {
        return android.os.Debug.isDebuggerConnected();
    }

    /** VULN (debug): /proc/self/status TracerPid — hookable */
    private boolean checkTracerPid() {
        try {
            BufferedReader br = new BufferedReader(
                new java.io.FileReader("/proc/self/status"));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("TracerPid:")) {
                    int pid = Integer.parseInt(line.split("\\s+")[1]);
                    if (pid != 0) {
                        Log.d(TAG, "[debug] TracerPid=" + pid);
                        return true;
                    }
                }
            }
        } catch (Exception e) {}
        return false;
    }
}
