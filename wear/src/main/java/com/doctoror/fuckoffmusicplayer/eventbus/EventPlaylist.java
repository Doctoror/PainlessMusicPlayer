package com.doctoror.fuckoffmusicplayer.eventbus;

import com.doctoror.commons.wear.nano.WearPlaybackData;

import android.support.annotation.Nullable;

/**
 * Created by Yaroslav Mytkalyk on 28.11.16.
 */

public final class EventPlaylist {

    @Nullable
    public final WearPlaybackData.Playlist playlist;

    public EventPlaylist(@Nullable final WearPlaybackData.Playlist playlist) {
        this.playlist = playlist;
    }
}
