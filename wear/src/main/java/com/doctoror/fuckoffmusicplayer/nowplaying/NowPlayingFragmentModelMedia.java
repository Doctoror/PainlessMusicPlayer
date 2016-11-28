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
package com.doctoror.fuckoffmusicplayer.nowplaying;

import android.databinding.ObservableField;
import android.graphics.drawable.Drawable;

/**
 * Media data binding model for {@link NowPlayingFragment} view
 */
public final class NowPlayingFragmentModelMedia {

    private final ObservableField<CharSequence> mTitle = new ObservableField<>();
    private final ObservableField<CharSequence> mArtistAndAlbum = new ObservableField<>();
    private final ObservableField<Drawable> mArt = new ObservableField<>();

    public ObservableField<Drawable> getArt() {
        return mArt;
    }

    void setArt(final Drawable art) {
        mArt.set(art);
    }

    public ObservableField<CharSequence> getTitle() {
        return mTitle;
    }

    void setTitle(final CharSequence title) {
        mTitle.set(title);
    }

    public ObservableField<CharSequence> getArtistAndAlbum() {
        return mArtistAndAlbum;
    }

    void setArtistAndAlbum(final CharSequence artistAndAlbum) {
        mArtistAndAlbum.set(artistAndAlbum);
    }
}
