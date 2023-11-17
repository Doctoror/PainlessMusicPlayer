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

import static org.junit.Assert.assertFalse;

import android.content.ContentResolver;
import android.os.Build;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.doctoror.fuckoffmusicplayer.domain.media.AlbumMediaIdsProvider;
import com.doctoror.fuckoffmusicplayer.domain.media.MediaManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;

@Config(manifest = Config.NONE, sdk = Build.VERSION_CODES.TIRAMISU)
@RunWith(RobolectricTestRunner.class)
public final class MediaManagerMediaStoreTest {

    private final ContentResolver contentResolver =
            RuntimeEnvironment.application.getContentResolver();

    private final MediaManagerTestFileConfigurator configurator =
            new MediaManagerTestFileConfigurator(contentResolver);

    @NonNull
    private MediaManager getMediaManager() {
        return new MediaManagerMediaStore(
                contentResolver,
                Mockito.mock(AlbumMediaIdsProvider.class));
    }

    @Test
    public void testDeleteMedia() throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Tests will not work with runtime permissions
            return;
        }

        final File file = configurator.createTestFileMedia();
        final long id = configurator.insertToMediaStoreAsMedia(file);
        try {
            getMediaManager().deleteMedia(id);
            assertFalse(configurator.existsInMediaStore(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id));
        } finally {
            configurator.cleanup(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, file, id);
        }
    }

    @Test
    public void testDeletePlaylist() throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Tests will not work with runtime permissions
            return;
        }

        final File file = configurator.createTestFilePlaylist();
        final long id = configurator.insertToMediaStoreAsPlaylist(file);
        try {
            getMediaManager().deletePlaylist(id);
            assertFalse(configurator.existsInMediaStore(
                    MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, id));
        } finally {
            configurator.cleanup(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, file, id);
        }
    }

}
