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
package com.doctoror.fuckoffmusicplayer.media.manager;

import org.junit.Test;

import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import java.io.File;

import static org.junit.Assert.assertFalse;

/**
 * {@link MediaManagerFile} test
 */
public final class MediaManagerFileTest {

    @NonNull
    private MediaManager getMediaManager() {
        return new MediaManagerFile(MediaManagerTestFileConfigurator.getContentResolver());
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
            assertFalse(file.exists());
        } finally {
            //noinspection ResultOfMethodCallIgnored
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
            assertFalse(file.exists());
        } finally {
            MediaManagerTestFileConfigurator
                    .cleanup(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, file, id);
        }
    }
}
