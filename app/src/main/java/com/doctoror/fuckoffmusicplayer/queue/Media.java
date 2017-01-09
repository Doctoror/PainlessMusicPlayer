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
package com.doctoror.fuckoffmusicplayer.queue;

import org.parceler.Parcel;

import android.net.Uri;

/**
 * Created by Yaroslav Mytkalyk on 19.10.16.
 */
@Parcel
public final class Media {

    long id;
    Uri data;
    String title;
    long duration;
    String artist;
    String album;
    long albumId;
    String albumArt;
    int track;

    public void setId(final long id) {
        this.id = id;
    }

    public void setData(final Uri data) {
        this.data = data;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void setDuration(final long duration) {
        this.duration = duration;
    }

    public void setArtist(final String artist) {
        this.artist = artist;
    }

    public void setAlbum(final String album) {
        this.album = album;
    }

    public void setAlbumId(final long albumId) {
        this.albumId = albumId;
    }

    public void setAlbumArt(final String albumArt) {
        this.albumArt = albumArt;
    }

    public void setTrack(final int track) {
        this.track = track;
    }

    public long getId() {
        return id;
    }

    public long getAlbumId() {
        return albumId;
    }

    public Uri getData() {
        return data;
    }

    public String getTitle() {
        return title;
    }

    public long getDuration() {
        return duration;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getAlbumArt() {
        return albumArt;
    }

    public int getTrack() {
        return track;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Media media = (Media) o;

        return id == media.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
