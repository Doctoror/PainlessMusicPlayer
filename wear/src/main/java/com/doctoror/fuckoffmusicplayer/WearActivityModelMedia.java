package com.doctoror.fuckoffmusicplayer;

import android.databinding.ObservableField;
import android.graphics.drawable.Drawable;

/**
 * Created by Yaroslav Mytkalyk on 16.11.16.
 */

public final class WearActivityModelMedia {

    private final ObservableField<CharSequence> mTitle = new ObservableField<>();
    private final ObservableField<CharSequence> mArtistAndAlbum = new ObservableField<>();
    private final ObservableField<Drawable> mArt = new ObservableField<>();

    public ObservableField<Drawable> getArt() {
        return mArt;
    }

    public void setArt(final Drawable art) {
        mArt.set(art);
    }

    public ObservableField<CharSequence> getTitle() {
        return mTitle;
    }

    public void setTitle(final CharSequence title) {
        mTitle.set(title);
    }

    public ObservableField<CharSequence> getArtistAndAlbum() {
        return mArtistAndAlbum;
    }

    public void setArtistAndAlbum(final CharSequence artistAndAlbum) {
        mArtistAndAlbum.set(artistAndAlbum);
    }
}
