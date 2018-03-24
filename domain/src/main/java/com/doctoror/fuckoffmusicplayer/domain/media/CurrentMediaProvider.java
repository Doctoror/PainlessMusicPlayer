package com.doctoror.fuckoffmusicplayer.domain.media;

import android.support.annotation.Nullable;

import com.doctoror.fuckoffmusicplayer.domain.queue.Media;

public interface CurrentMediaProvider {

    @Nullable
    Media getCurrentMedia();
}
