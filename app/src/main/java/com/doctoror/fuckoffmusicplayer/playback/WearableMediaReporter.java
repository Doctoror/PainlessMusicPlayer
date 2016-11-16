package com.doctoror.fuckoffmusicplayer.playback;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.MessageNano;

import com.doctoror.commons.wear.DataPaths;
import com.doctoror.commons.wear.nano.ProtoPlaybackData;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.util.Log;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.io.IOException;

/**
 * Created by Yaroslav Mytkalyk on 15.11.16.
 */

final class WearableMediaReporter {

    private static final String TAG = "WearableMediaReporter";

    private WearableMediaReporter() {

    }

    @WorkerThread
    static void reportMedia(@NonNull final GoogleApiClient googleApiClient,
            @NonNull final Media media,
            final long position) {
        if (googleApiClient.isConnected()) {
            final PutDataRequest request;
            try {
                request = newPutMediaRequest(media, position);
            } catch (IOException e) {
                Log.w(TAG, e);
                return;
            }
            request.setUrgent();

            Wearable.DataApi.putDataItem(googleApiClient, request).await();
        }
    }

    @WorkerThread
    static void reportState(@NonNull final GoogleApiClient googleApiClient,
            @PlaybackService.State final int state,
            final long duration,
            final long position) {
        if (googleApiClient.isConnected()) {
            final PutDataRequest request;
            try {
                request = newPutStateRequest(state, duration, position);
            } catch (IOException e) {
                Log.w(TAG, e);
                return;
            }
            request.setUrgent();
            Wearable.DataApi.putDataItem(googleApiClient, request).await();
        }
    }

    @NonNull
    private static PutDataRequest newPutMediaRequest(@NonNull final Media media,
            final long position) throws IOException {
        final PutDataRequest request = PutDataRequest.create(DataPaths.PATH_MEDIA);
        request.setData(messageNanoToBytes(toWearableData(media, position)));
        return request;
    }

    @NonNull
    private static PutDataRequest newPutStateRequest(
            @PlaybackService.State final int state,
            final long duration,
            final long position) throws IOException {
        final PutDataRequest request = PutDataRequest.create(DataPaths.PATH_PLAYBACK_STATE);
        request.setData(messageNanoToBytes(toPlaybackState(state, duration, position)));
        return request;
    }

    private static byte[] messageNanoToBytes(@NonNull final MessageNano nano) throws
            IOException {
        final byte[] output = new byte[nano.getCachedSize()];
        nano.writeTo(CodedOutputByteBufferNano.newInstance(output));
        return output;
    }

    @NonNull
    private static ProtoPlaybackData.Media toWearableData(@NonNull final Media media,
            final long position) {
        final ProtoPlaybackData.Media m = new ProtoPlaybackData.Media();
        m.album = media.getAlbum();
        m.artist = media.getArtist();
        m.title = media.getTitle();
        m.duration = media.getDuration();
        m.progress = position;
        return m;
    }

    @NonNull
    private static ProtoPlaybackData.PlaybackState toPlaybackState(
            @PlaybackService.State final int state,
            final long duration,
            final long position) {
        final ProtoPlaybackData.PlaybackState m = new ProtoPlaybackData.PlaybackState();
        m.state = MediaSessionReporter.toPlaybackStateCompat(state);
        m.duration = duration;
        m.progress = position;
        return m;
    }

}
