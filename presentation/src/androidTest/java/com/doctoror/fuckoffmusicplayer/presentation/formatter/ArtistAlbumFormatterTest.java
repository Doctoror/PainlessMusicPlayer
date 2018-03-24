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
package com.doctoror.fuckoffmusicplayer.presentation.formatter;

import com.doctoror.fuckoffmusicplayer.R;

import org.junit.Test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import static org.junit.Assert.assertEquals;

/**
 * {@link ArtistAlbumFormatter} test
 */
public final class ArtistAlbumFormatterTest {

    private final ArtistAlbumFormatter underTest = new ArtistAlbumFormatter();

    @Test(expected = NullPointerException.class)
    public void testFormatArtistAndAlbumNull() throws Exception {
        //noinspection ConstantConditions
        underTest.formatArtistAndAlbum(null, null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testFormatArtistAndAlbumNullRes() throws Exception {
        //noinspection ConstantConditions
        underTest.formatArtistAndAlbum(null, "a", "a");
    }

    @Test
    public void testFormatArtistAndAlbumNullArtistAndAlbum() throws Exception {
        final Context context = InstrumentationRegistry.getContext();
        final String separator = context.getString(R.string.artist_album_separator);
        final String expected = context.getString(R.string.Unknown_artist)
                + separator + context.getString(R.string.Unknown_album);

        assertEquals(expected, underTest.formatArtistAndAlbum(context.getResources(), null, null));

        assertEquals(expected, underTest.formatArtistAndAlbum(context.getResources(), "", ""));
    }

    @Test
    public void testFormatArtistAndAlbumEmptyAlbum() throws Exception {
        final Context context = InstrumentationRegistry.getContext();
        final String separator = context.getString(R.string.artist_album_separator);
        final String artist = "artist";
        final String expected = artist + separator + context.getString(R.string.Unknown_album);

        assertEquals(expected,
                underTest.formatArtistAndAlbum(context.getResources(), artist, null));
        assertEquals(expected,
                underTest.formatArtistAndAlbum(context.getResources(), artist, ""));
    }

    @Test
    public void testFormatArtistAndAlbumEmptyArtist() throws Exception {
        final Context context = InstrumentationRegistry.getContext();
        final String separator = context.getString(R.string.artist_album_separator);
        final String album = "album";
        final String expected = context.getString(R.string.Unknown_artist) + separator + album;

        assertEquals(expected,
                underTest.formatArtistAndAlbum(context.getResources(), null, album));

        assertEquals(expected,
                underTest.formatArtistAndAlbum(context.getResources(), "", album));
    }

    @Test
    public void testFormatArtistAndAlbum() throws Exception {
        final Context context = InstrumentationRegistry.getContext();
        final String separator = context.getString(R.string.artist_album_separator);
        final String artist = "artist";
        final String album = "album";
        final String expected = artist + separator + album;

        assertEquals(expected,
                underTest.formatArtistAndAlbum(context.getResources(), artist, album));
    }
}
