package com.doctoror.fuckoffmusicplayer.media.manager;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;

import java.io.File;
import java.io.IOException;

/**
 * Created by Yaroslav Mytkalyk on 12.01.17.
 */
final class MediaManagerTestFileConfigurator {

    private MediaManagerTestFileConfigurator() {

    }

    @NonNull
    static ContentResolver getContentResolver() {
        return InstrumentationRegistry.getContext().getContentResolver();
    }

    static void cleanup(@NonNull final Uri contentUri,
            @NonNull final File file,
            final long id) {
        //noinspection ResultOfMethodCallIgnored
        file.delete();
        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().delete();

        getContentResolver().delete(contentUri, BaseColumns._ID + '=' + id, null);
    }

    @NonNull
    static File createTestFileMedia() throws Exception {
        return createTestFile("media.m4a");
    }

    @NonNull
    static File createTestFilePlaylist() throws Exception {
        return createTestFile("playlist.m3u");
    }

    @NonNull
    private static File createTestFile(@NonNull final String name) throws Exception {
        final File ext = Environment.getExternalStorageDirectory();
        final File packageDir = new File(ext,
                "." + InstrumentationRegistry.getContext().getPackageName());
        if (!packageDir.exists()) {
            if (!packageDir.mkdirs()) {
                throw new IOException("Failed to create test package dir");
            }
        }

        final File testFile = new File(packageDir, name);
        if (!testFile.exists()) {
            if (!testFile.createNewFile()) {
                throw new IOException("Failed to create test file");
            }
        }

        return testFile;
    }

    static long insertToMediaStoreAsMedia(@NonNull final File file) throws Exception {
        final Long existingId = getExistingId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Audio.Media.DATA,
                file);
        if (existingId != null) {
            return existingId;
        }

        final ContentValues values = new ContentValues(4);
        values.put(MediaStore.Audio.Media.DATA, file.getAbsolutePath());
        values.put(MediaStore.Audio.Media.TITLE, file.getName());
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, file.getName());
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/m4a");

        return insertOrThrow(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
    }

    static long insertToMediaStoreAsPlaylist(@NonNull final File file) throws Exception {
        final Long existingId = getExistingId(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                MediaStore.Audio.Playlists.DATA,
                file);
        if (existingId != null) {
            return existingId;
        }

        final ContentValues values = new ContentValues(2);
        values.put(MediaStore.Audio.Playlists.DATA, file.getAbsolutePath());
        values.put(MediaStore.Audio.Playlists.NAME, file.getName());

        return insertOrThrow(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
    }

    @Nullable
    private static Long getExistingId(
            @NonNull final Uri contentUri,
            @NonNull final String dataColumn,
            @NonNull final File file) throws IOException {
        final Cursor c = getContentResolver().query(contentUri,
                new String[]{BaseColumns._ID},
                dataColumn + "=? OR " + dataColumn + "=?",
                new String[]{
                        file.getAbsolutePath(),
                        file.getCanonicalPath()
                },
                null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    return c.getLong(0);
                }
            } finally {
                c.close();
            }
        }
        return null;
    }

    private static long insertOrThrow(
            @NonNull final Uri contentUri,
            @NonNull final ContentValues values) {
        final Uri uri = getContentResolver().insert(contentUri, values);
        final long result = uri != null ? Long.valueOf(uri.getLastPathSegment()) : -1;
        if (result == -1) {
            throw new RuntimeException("Could not insert test file to MediaStore");
        }
        if (!existsInMediaStore(contentUri, result)) {
            throw new RuntimeException("Inserted test file does not exist MediaStore");
        }
        return result;
    }

    static boolean existsInMediaStore(@NonNull final Uri contentUri,
            final long id) {
        final Cursor c = getContentResolver().query(contentUri,
                new String[]{BaseColumns._ID},
                BaseColumns._ID + '=' + id,
                null,
                null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    return c.getLong(0) == id;
                }
            } finally {
                c.close();
            }
        }
        return false;
    }
}
