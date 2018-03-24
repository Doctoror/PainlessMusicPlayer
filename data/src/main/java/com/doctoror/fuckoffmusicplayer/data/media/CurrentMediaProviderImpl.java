package com.doctoror.fuckoffmusicplayer.data.media;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.doctoror.fuckoffmusicplayer.data.util.CollectionUtils;
import com.doctoror.fuckoffmusicplayer.domain.media.CurrentMediaProvider;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;

public final class CurrentMediaProviderImpl implements CurrentMediaProvider {

    private final PlaybackData playbackData;

    public CurrentMediaProviderImpl(@NonNull PlaybackData playbackData) {
        this.playbackData = playbackData;
    }

    @Nullable
    @Override
    public Media getCurrentMedia() {
        return CollectionUtils.getItemSafe(playbackData.getQueue(), playbackData.getQueuePosition());
    }
}
