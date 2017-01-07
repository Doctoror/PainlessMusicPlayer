package com.doctoror.fuckoffmusicplayer.library.livelists;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A "Live Playlist"
 */
final class LivePlaylist {

    static final int TYPE_RECENTLY_PLAYED_ALBUMS = 0;
    static final int TYPE_RECENTLY_SCANNED = 1;
    static final int TYPE_RANDOM_PLAYLIST = 2;

    @IntDef({
            TYPE_RECENTLY_PLAYED_ALBUMS,
            TYPE_RECENTLY_SCANNED,
            TYPE_RANDOM_PLAYLIST
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface Type {

    }

    @Type
    private final int mType;

    @NonNull
    private final CharSequence mTitle;

    LivePlaylist(@Type final int type, @NonNull final CharSequence title) {
        mType = type;
        mTitle = title;
    }

    @Type
    public int getType() {
        return mType;
    }

    @NonNull
    public CharSequence getTitle() {
        return mTitle;
    }
}
