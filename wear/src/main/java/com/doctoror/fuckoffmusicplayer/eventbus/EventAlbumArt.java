package com.doctoror.fuckoffmusicplayer.eventbus;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

/**
 * Created by Yaroslav Mytkalyk on 28.11.16.
 */

public final class EventAlbumArt {

    @Nullable
    public final Bitmap albumArt;

    public EventAlbumArt(@Nullable final Bitmap albumArt) {
        this.albumArt = albumArt;
    }
}
