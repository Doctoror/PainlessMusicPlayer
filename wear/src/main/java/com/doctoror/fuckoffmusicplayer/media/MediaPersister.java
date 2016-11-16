package com.doctoror.fuckoffmusicplayer.media;

import com.doctoror.commons.util.ByteStreams;
import com.doctoror.commons.util.ProtoPersister;
import com.doctoror.commons.wear.nano.ProtoPlaybackData;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by Yaroslav Mytkalyk on 15.11.16.
 */

final class MediaPersister {

    private static final String TAG = "MediaPersister";

    private static final String FILE_NAME_MEDIA = "media";
    private static final String FILE_NAME_PLAYBACK_STATE = "playback_state";

    private MediaPersister() {
        throw new UnsupportedOperationException();
    }

    static void persistPlaybackStateAsync(@NonNull final Context context,
            @NonNull final ProtoPlaybackData.PlaybackState playbackState) {
        // Retrieve snapshot now, so that it's immutable when writing
        Observable.create(s -> persistPlaybackState(context, playbackState))
                .subscribeOn(Schedulers.io()).subscribe();
    }

    static void persistPlaybackState(@NonNull final Context context,
            @NonNull final ProtoPlaybackData.PlaybackState ps) {
        ProtoPersister.writeToFile(context, FILE_NAME_PLAYBACK_STATE, ps);
    }

    static void persistMediaAsync(@NonNull final Context context,
            @NonNull final ProtoPlaybackData.Media media) {
        // Retrieve snapshot now, so that it's immutable when writing
        Observable.create(s -> persistMedia(context, media))
                .subscribeOn(Schedulers.io()).subscribe();
    }

    static void deleteMedia(@NonNull final Context context) {
        context.deleteFile(FILE_NAME_MEDIA);
    }

    static void deletePlaybackState(@NonNull final Context context) {
        context.deleteFile(FILE_NAME_PLAYBACK_STATE);
    }

    static void persistMedia(@NonNull final Context context,
            @NonNull final ProtoPlaybackData.Media ps) {
        ProtoPersister.writeToFile(context, FILE_NAME_MEDIA, ps);
    }

    @Nullable
    static ProtoPlaybackData.PlaybackState readPlaybackState(@NonNull final Context context) {
        return ProtoPersister.readFromFile(context, new ProtoPlaybackData.PlaybackState(),
                FILE_NAME_PLAYBACK_STATE);
    }

    @Nullable
    static ProtoPlaybackData.Media readMedia(@NonNull final Context context) {
        return ProtoPersister.readFromFile(context, new ProtoPlaybackData.Media(),
                FILE_NAME_MEDIA);
    }
}
