package com.doctoror.fuckoffmusicplayer.domain.playback;

import android.support.annotation.Nullable;

public interface PlaybackService {

    void playPause();

    void play();

    void playAnything();

    void pause();

    void stop();

    void stopWithError(@Nullable CharSequence errorMessage);

    void playPrev();

    void playNext();

    void seek(long position);

    void notifyState();

    void destroy();
}
