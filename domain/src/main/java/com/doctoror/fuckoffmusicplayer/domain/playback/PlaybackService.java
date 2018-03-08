package com.doctoror.fuckoffmusicplayer.domain.playback;

import com.doctoror.fuckoffmusicplayer.domain.queue.Media;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

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
    void playMediaFromQueue(@NonNull List<Media> queue, long mediaId);
    void notifyState();

    void destroy();
}
