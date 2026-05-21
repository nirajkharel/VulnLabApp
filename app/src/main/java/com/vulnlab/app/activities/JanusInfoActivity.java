package com.vulnlab.app.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.vulnlab.app.R;

public class JanusInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_janus_info);

        TextView tv = findViewById(R.id.tv_janus_info);
        tv.setText(
            "Janus / v1-only Signing (CVE-2017-13156)\n\n"
            + "This app is built with:\n"
            + "  minSdkVersion = 16\n"
            + "  v1SigningEnabled = true\n"
            + "  v2SigningEnabled = false\n\n"
            + "Exploit:\n"
            + "  1. Craft malicious.dex\n"
            + "  2. cat malicious.dex victim.apk > trojan.apk\n"
            + "  3. Sign trojan.apk with v1 only\n"
            + "  4. adb install trojan.apk\n"
            + "  5. ART on Android ≤ 5.1 runs malicious.dex\n\n"
            + "Fix: enable v2/v3 signing and raise minSdkVersion ≥ 21."
        );
    }
}
