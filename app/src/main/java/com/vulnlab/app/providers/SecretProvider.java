package com.vulnlab.app.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

// VULN: mutable-pending-intent-hijack (Tier 2).
// This provider is exported=false, so no other app can query it directly.
// It is reachable ONLY because the empty-base mutable PendingIntent that
// NotificationActivity posts lets an attacker make THIS app grant a temporary
// read URI permission on it (FLAG_GRANT_READ_URI_PERMISSION). grantUriPermissions
// is on, so the grant bypasses exported=false — that is the whole point.
public class SecretProvider extends ContentProvider {

    private static final String TAG = "VulnSecretProvider";
    private static final File SECRET =
        new File("/data/data/com.vulnlab.app/secret/token.txt");

    @Override
    public boolean onCreate() {
        try {
            SECRET.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(SECRET)) {
                fos.write(("session_token=eyJhbGciOiJIUzI1NiJ9.FAKE\n"
                    + "api_key=sk-prod-8f3k2j9x0q1w5e6r\n").getBytes());
            }
        } catch (Exception e) {
            Log.e(TAG, "seed failed: " + e.getMessage());
        }
        return true;
    }

    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode)
            throws FileNotFoundException {
        // If we get here from another UID, a URI grant let them in.
        Log.d(TAG, "[pending-intent-hijack] read granted on " + uri);
        return ParcelFileDescriptor.open(SECRET, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override
    public String getType(@NonNull Uri uri) { return "text/plain"; }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] p, @Nullable String s,
                        @Nullable String[] a, @Nullable String o) { return null; }

    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues v) { return null; }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] a) { return 0; }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues v,
                      @Nullable String s, @Nullable String[] a) { return 0; }
}
