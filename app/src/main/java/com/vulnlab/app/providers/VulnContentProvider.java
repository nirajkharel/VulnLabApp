package com.vulnlab.app.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;

public class VulnContentProvider extends ContentProvider {

    private static final String TAG      = "VulnProvider";
    private static final String DB_NAME  = "vulnlab.db";
    private static final int    DB_VER   = 1;

    private SQLiteDatabase db;

    private static final File EXPORTS_DIR =
        new File("/data/data/com.vulnlab.app/exports");

    @Override
    public boolean onCreate() {
        db = new VulnDbHelper(getContext()).getWritableDatabase();
        EXPORTS_DIR.mkdirs();
        // Seed some exported files for path-traversal demo
        try {
            new java.io.File(EXPORTS_DIR, "public_report.pdf")
                .createNewFile();
        } catch (Exception ignored) {}
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {

        String table = uri.getLastPathSegment();
        if (table == null) table = "users";

        // VULN: selection concatenated directly — SQL injection possible
        String where = (selection != null && !selection.isEmpty())
            ? " WHERE " + selection   // VULN: raw attacker string in SQL
            : "";

        String query = "SELECT * FROM " + table + where;
        Log.d(TAG, "[sql-injection] executing: " + query);

        try {
            return db.rawQuery(query, null);
        } catch (Exception e) {
            Log.e(TAG, "Query error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode)
            throws FileNotFoundException {

        String relativePath = uri.getLastPathSegment();
        // VULN: no File.getCanonicalPath() normalization
        File target = new File(EXPORTS_DIR, relativePath);
        Log.d(TAG, "[path-traversal] openFile: uri=" + uri + " target=" + target.getAbsolutePath());

        // The fix would be:
        // File canonical = target.getCanonicalFile();
        // if (!canonical.getPath().startsWith(EXPORTS_DIR.getCanonicalPath())) throw new SecurityException();

        return ParcelFileDescriptor.open(target, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override
    public String getType(@NonNull Uri uri) { return null; }

    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) { return null; }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String sel, @Nullable String[] args) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String sel, @Nullable String[] args) {
        return 0;
    }

    // ── Database helper ────────────────────────────────────────────────────

    private static class VulnDbHelper extends SQLiteOpenHelper {

        VulnDbHelper(android.content.Context ctx) {
            super(ctx, DB_NAME, null, DB_VER);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(
                "CREATE TABLE users ("
                + "id INTEGER PRIMARY KEY, "
                + "email TEXT, "
                + "password TEXT, "
                + "role TEXT, "
                + "api_key TEXT)");
            db.execSQL(
                "CREATE TABLE sessions ("
                + "id INTEGER PRIMARY KEY, "
                + "user_id INTEGER, "
                + "token TEXT, "
                + "created_at INTEGER)");
            // Seed data
            db.execSQL("INSERT INTO users VALUES (1,'admin@corp.com','P@ssw0rd!','ADMIN','sk-prod-8f3k2j9x0q1w5e6r')");
            db.execSQL("INSERT INTO users VALUES (2,'user@corp.com','hunter2','USER','sk-test-abc123')");
            db.execSQL("INSERT INTO sessions VALUES (1,1,'eyJhbGciOiJIUzI1NiJ9.FAKE',1716000000)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
    }
}
