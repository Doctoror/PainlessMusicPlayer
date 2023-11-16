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
package com.doctoror.fuckoffmusicplayer.data.playlist;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collections;

/**
 * {@link RecentActivityManagerImpl} test
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public final class RecentActivityManagerTest {

    @Test
    public void testStoreAlbum() {
        final RecentActivityManagerImpl rpm = RecentActivityManagerImpl.getInstance(
                RuntimeEnvironment.systemContext);
        rpm.clear();
        rpm.onAlbumPlayed(666);

        final long[] albums = rpm.getRecentlyPlayedAlbums();
        assertEquals(1, albums.length);
        assertEquals(666, albums[0]);
    }

    @Test
    public void testStoreAlbums() {
        final RecentActivityManagerImpl rpm = RecentActivityManagerImpl.getInstance(
                RuntimeEnvironment.systemContext);
        rpm.clear();
        rpm.storeAlbumsSync(Collections.singletonList(666L));

        final long[] albums = rpm.getRecentlyPlayedAlbums();
        assertEquals(1, albums.length);
        assertEquals(666, albums[0]);
    }

    @Test
    public void testOrderingByOne() {
        final RecentActivityManagerImpl rpm = RecentActivityManagerImpl.getInstance(
                RuntimeEnvironment.systemContext);
        rpm.clear();

        rpm.onAlbumPlayed(666);
        rpm.onAlbumPlayed(777);
        rpm.onAlbumPlayed(888);

        final long[] albums = rpm.getRecentlyPlayedAlbums();
        assertEquals(3, albums.length);
        assertEquals(888, albums[0]);
        assertEquals(777, albums[1]);
        assertEquals(666, albums[2]);
    }

    @Test
    public void testOrderingByBatch() {
        final RecentActivityManagerImpl rpm = RecentActivityManagerImpl.getInstance(
                RuntimeEnvironment.systemContext);
        rpm.clear();
        rpm.storeAlbumsSync(Arrays.asList(666L, 777L, 888L));

        final long[] albums = rpm.getRecentlyPlayedAlbums();
        assertEquals(3, albums.length);
        assertEquals(888, albums[0]);
        assertEquals(777, albums[1]);
        assertEquals(666, albums[2]);
    }

    @Test
    public void testAppendingDuplicate() {
        final RecentActivityManagerImpl rpm = RecentActivityManagerImpl.getInstance(
                RuntimeEnvironment.systemContext);
        rpm.clear();
        // Add initial values
        rpm.storeAlbumsSync(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L));
        // This one should append to end
        rpm.onAlbumPlayed(4L);

        final long[] albums = rpm.getRecentlyPlayedAlbums();
        assertEquals(8, albums.length);
        assertEquals(4, albums[0]);
        assertEquals(8, albums[1]);
        assertEquals(7, albums[2]);
        assertEquals(6, albums[3]);
        assertEquals(5, albums[4]);
        assertEquals(3, albums[5]);
        assertEquals(2, albums[6]);
        assertEquals(1, albums[7]);
    }

    @Test
    public void testAppendingTheSameValue() {
        final RecentActivityManagerImpl rpm = RecentActivityManagerImpl.getInstance(
                RuntimeEnvironment.systemContext);
        rpm.clear();
        // Add initial values
        rpm.storeAlbumsSync(Arrays.asList(1L, 1L, 1L, 1L));

        long[] albums = rpm.getRecentlyPlayedAlbums();
        assertEquals(1, albums.length);
        assertEquals(1, albums[0]);

        rpm.onAlbumPlayed(1L);

        albums = rpm.getRecentlyPlayedAlbums();
        assertEquals(1, albums.length);
        assertEquals(1, albums[0]);
    }

    @Test
    public void testAppendingTheSameValueOnEnd() {
        final RecentActivityManagerImpl rpm = RecentActivityManagerImpl.getInstance(
                RuntimeEnvironment.systemContext);
        rpm.clear();
        // Add initial values
        rpm.storeAlbumsSync(Arrays.asList(1L, 2L, 2L, 2L));

        long[] albums = rpm.getRecentlyPlayedAlbums();
        assertEquals(2, albums.length);
        assertEquals(2, albums[0]);
        assertEquals(1, albums[1]);

        rpm.onAlbumPlayed(2L);

        albums = rpm.getRecentlyPlayedAlbums();
        assertEquals(2, albums.length);
        assertEquals(2, albums[0]);
        assertEquals(1, albums[1]);
    }

}
