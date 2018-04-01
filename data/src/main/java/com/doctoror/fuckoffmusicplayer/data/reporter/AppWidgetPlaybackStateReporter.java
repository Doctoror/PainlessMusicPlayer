package com.doctoror.fuckoffmusicplayer.data.reporter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporter;

import java.util.List;

public final class AppWidgetPlaybackStateReporter implements PlaybackReporter {

    public static final String ACTION_STATE_CHANGED
            = "com.doctoror.fuckoffmusicplayer.playback.ACTION_STATE_CHANGED";

    public static final String EXTRA_STATE = "EXTRA_STATE";

    private static final String SUFFIX_PERMISSION_RECEIVE_PLAYBACK_STATE
            = ".permission.RECEIVE_PLAYBACK_STATE";

    private final Context context;
    private final String permissionReceivePlaybackState;

    AppWidgetPlaybackStateReporter(@NonNull final Context context) {
        this.context = context;
        permissionReceivePlaybackState = context.getPackageName()
                .concat(SUFFIX_PERMISSION_RECEIVE_PLAYBACK_STATE);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void reportTrackChanged(@NonNull Media media, int positionInQueue) {

    }

    @Override
    public void reportPlaybackStateChanged(
            @NonNull final PlaybackState state,
            @Nullable final CharSequence errorMessage) {
        final Intent intent = new Intent(ACTION_STATE_CHANGED);
        intent.putExtra(EXTRA_STATE, state.ordinal());
        context.sendBroadcast(intent, permissionReceivePlaybackState);
    }

    @Override
    public void reportPositionChanged(long mediaId, long position) {

    }

    @Override
    public void reportQueueChanged(@Nullable List<Media> queue) {

    }
}
