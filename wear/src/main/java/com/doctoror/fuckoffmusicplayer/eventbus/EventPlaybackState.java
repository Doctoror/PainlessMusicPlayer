package com.doctoror.fuckoffmusicplayer.eventbus;

import com.doctoror.commons.wear.nano.WearPlaybackData;

import android.support.annotation.Nullable;

/**
 * Created by Yaroslav Mytkalyk on 28.11.16.
 */

public final class EventPlaybackState {

    @Nullable
    public final WearPlaybackData.PlaybackState playbackState;

    public EventPlaybackState(@Nullable final WearPlaybackData.PlaybackState playbackState) {
        this.playbackState = playbackState;
    }
}
