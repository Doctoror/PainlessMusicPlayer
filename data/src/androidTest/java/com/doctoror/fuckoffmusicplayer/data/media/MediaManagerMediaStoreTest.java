/*
 * Copyright (C) 2017 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.data.media;

import com.doctoror.fuckoffmusicplayer.domain.media.MediaManager;

import org.junit.Test;

import android.content.ContentResolver;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;

import java.io.File;

import static org.junit.Assert.assertFalse;

/**
 * {@link MediaManagerMediaStore} test
 */
public final class MediaManagerMediaStoreTest {


    @NonNull
    private MediaManager getMediaManager() {
        return new MediaManagerMediaStore(getContentResolver());
    }

    @NonNull
    private ContentResolver getContentResolver() {
        return InstrumentationRegistry.getContext().getContentResolver();
    }

    @Test
    public void testDeleteMedia() throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Tests will not work with runtime permissions
            return;
        }

        final File file = MediaManagerTestFileConfigurator.createTestFileMedia();
        final long id = MediaManagerTestFileConfigurator.insertToMediaStoreAsMedia(file);
        try {
            getMediaManager().deleteMedia(id);
            assertFalse(MediaManagerTestFileConfigurator.existsInMediaStore(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id));
        } finally {
            MediaManagerTestFileConfigurator.cleanup(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    file, id);
        }
    }

    @Test
    public void testDeletePlaylist() throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Tests will not work with runtime permissions
            return;
        }

        final File file = MediaManagerTestFileConfigurator.createTestFilePlaylist();
        final long id = MediaManagerTestFileConfigurator.insertToMediaStoreAsPlaylist(file);
        try {
            getMediaManager().deletePlaylist(id);
            assertFalse(MediaManagerTestFileConfigurator.existsInMediaStore(
                    MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, id));
        } finally {
            MediaManagerTestFileConfigurator
                    .cleanup(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, file, id);
        }
    }

}
