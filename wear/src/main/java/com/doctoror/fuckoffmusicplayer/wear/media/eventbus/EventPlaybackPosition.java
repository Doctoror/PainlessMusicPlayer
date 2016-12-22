package com.doctoror.fuckoffmusicplayer.wear.media.eventbus;

import com.doctoror.commons.wear.nano.WearPlaybackData;

import android.support.annotation.Nullable;

/**
 * Event for playback position change
 */
public final class EventPlaybackPosition {

    @Nullable
    public final WearPlaybackData.PlaybackPosition playbackPosition;

    public EventPlaybackPosition(
            @Nullable final WearPlaybackData.PlaybackPosition playbackPosition) {
        this.playbackPosition = playbackPosition;
    }

}
