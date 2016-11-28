package com.doctoror.fuckoffmusicplayer.eventbus;

import com.doctoror.commons.wear.nano.WearPlaybackData;

import android.support.annotation.Nullable;

/**
 * Created by Yaroslav Mytkalyk on 28.11.16.
 */

public final class EventMedia {

    @Nullable
    public final WearPlaybackData.Media media;

    public EventMedia(@Nullable final WearPlaybackData.Media media) {
        this.media = media;
    }
}
