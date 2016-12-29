/*
 * Copyright (C) 2016 Yaroslav Mytkalyk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.doctoror.fuckoffmusicplayer.util;

import com.doctoror.fuckoffmusicplayer.playlist.Media;

import org.junit.Test;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * {@link FileUtils} test
 */
public final class FileUtilsTest {

    @Test
    public void testDeleteFile() throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Tests will not work with runtime permissions
            return;
        }

        final ContentResolver resolver = InstrumentationRegistry.getContext().getContentResolver();
        final File file = createTestFile();
        try {
            final long id = insertToMediaStore(resolver, file);
            if (id == -1) {
                throw new RuntimeException("Could not insert test file to MediaStore");
            }

            final Media media = mediaForFile(file, id);
            FileUtils.delete(resolver, media);
        } finally {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().delete();
        }
    }

    @NonNull
    private File createTestFile() throws Exception {
        final File ext = Environment.getExternalStorageDirectory();
        final File packageDir = new File(ext,
                "." + InstrumentationRegistry.getContext().getPackageName());
        if (!packageDir.exists()) {
            if (!packageDir.mkdirs()) {
                throw new IOException("Failed to create test package dir");
            }
        }

        final File testFile = new File(packageDir, "test.m4a");
        if (!testFile.exists()) {
            if (!testFile.createNewFile()) {
                throw new IOException("Failed to create test file");
            }
        }

        return testFile;
    }

    private long insertToMediaStore(@NonNull final ContentResolver resolver,
            @NonNull final File file) throws Exception {
        final Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final Cursor c = resolver.query(contentUri,
                new String[]{MediaStore.Audio.Media._ID},
                MediaStore.Audio.Media.DATA + "=? OR "
                        + MediaStore.Audio.Media.DATA + "=?",
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

        final ContentValues values = new ContentValues(4);
        values.put(MediaStore.Audio.Media.DATA, file.getAbsolutePath());
        values.put(MediaStore.Audio.Media.TITLE, file.getName());
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, file.getName());
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/m4a");

        final Uri uri = resolver.insert(contentUri, values);
        return uri != null ? Long.valueOf(uri.getLastPathSegment()) : -1;
    }

    @NonNull
    private Media mediaForFile(@NonNull final File file,
            final long id) throws Exception {
        final Media media = new Media();

        final Field fieldId = Media.class.getDeclaredField("id");
        fieldId.setAccessible(true);
        fieldId.set(media, id);

        final Field fieldData = Media.class.getDeclaredField("data");
        fieldData.setAccessible(true);
        fieldData.set(media, Uri.parse(file.toURI().toString()));

        return media;
    }
}
