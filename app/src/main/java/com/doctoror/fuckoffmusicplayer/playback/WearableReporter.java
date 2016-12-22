package com.doctoror.fuckoffmusicplayer.playback;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.MessageNano;

import com.bumptech.glide.RequestManager;
import com.doctoror.commons.playback.PlaybackState;
import com.doctoror.commons.util.Log;
import com.doctoror.commons.wear.DataPaths;
import com.doctoror.commons.wear.nano.WearPlaybackData;
import com.doctoror.fuckoffmusicplayer.playlist.Media;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Yaroslav Mytkalyk on 15.11.16.
 */

final class WearableReporter {

    private static final String TAG = "WearableMediaReporter";

    private WearableReporter() {

    }

    static void reportPlaylist(@NonNull final GoogleApiClient googleApiClient,
            @Nullable final List<Media> playlist) {
        if (googleApiClient.isConnected() && playlist != null && !playlist.isEmpty()) {
            final int size = playlist.size();
            final WearPlaybackData.Media[] wMedias = new WearPlaybackData.Media[playlist.size()];
            for (int i = 0; i < size; i++) {
                wMedias[i] = toWearableData(playlist.get(i), 0, 0);
            }

            final PutDataRequest request;
            try {
                request = PutDataRequest.create(DataPaths.Paths.PLAYLIST);
                final WearPlaybackData.Playlist wPlaylist = new WearPlaybackData.Playlist();
                wPlaylist.media = wMedias;
                request.setData(messageNanoToBytes(wPlaylist));
            } catch (IOException e) {
                Log.w(TAG, e);
                return;
            }
            request.setUrgent();

            Wearable.DataApi.putDataItem(googleApiClient, request).await();
        }
    }

    @WorkerThread
    static void reportMedia(@NonNull final GoogleApiClient googleApiClient,
            @NonNull final RequestManager glide,
            @NonNull final Media media,
            final int positionInPlaylist,
            final long position) {
        if (googleApiClient.isConnected()) {
            final PutDataRequest request;
            try {
                request = newPutMediaRequest(media, positionInPlaylist, position);
            } catch (IOException e) {
                Log.w(TAG, e);
                return;
            }
            request.setUrgent();

            final String art = media.getAlbumArt();
            byte[] albumArt = null;
            if (!TextUtils.isEmpty(art)) {
                try {
                    final Bitmap artBitmap = glide.load(art).asBitmap().into(320, 320).get();
                    if (artBitmap != null) {
                        final ByteArrayOutputStream baos = new ByteArrayOutputStream(409600);
                        if (!artBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)) {
                            throw new IOException("Failed compressing bitmap");
                        }

                        albumArt = baos.toByteArray();
                    }
                } catch (InterruptedException | ExecutionException | IOException e) {
                    Log.w(TAG, e);
                }
            }

            if (albumArt == null) {
                // Empty array means no cover
                albumArt = new byte[0];
            }

            request.putAsset(DataPaths.Assets.ALBUM_ART, Asset.createFromBytes(albumArt));
            Wearable.DataApi.putDataItem(googleApiClient, request).await();
        }
    }

    @WorkerThread
    static void reportState(@NonNull final GoogleApiClient googleApiClient,
            @PlaybackState.State final int state,
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
            final int playlistPosition,
            final long position) throws IOException {
        final PutDataRequest request = PutDataRequest.create(DataPaths.Paths.MEDIA);
        request.setData(messageNanoToBytes(toWearableData(media, playlistPosition, position)));
        return request;
    }

    @NonNull
    private static PutDataRequest newPutStateRequest(
            @PlaybackState.State final int state,
            final long duration,
            final long position) throws IOException {
        final PutDataRequest request = PutDataRequest.create(DataPaths.Paths.PLAYBACK_STATE);
        request.setData(messageNanoToBytes(toPlaybackState(state, duration, position)));
        return request;
    }

    @NonNull
    private static byte[] messageNanoToBytes(@NonNull final MessageNano nano) throws
            IOException {
        final byte[] output = new byte[nano.getCachedSize()];
        nano.writeTo(CodedOutputByteBufferNano.newInstance(output));
        return output;
    }

    @NonNull
    private static WearPlaybackData.Media toWearableData(@NonNull final Media media,
            final int playlistPosition,
            final long position) {
        final WearPlaybackData.Media m = new WearPlaybackData.Media();
        m.id = media.getId();
        m.album = media.getAlbum();
        m.artist = media.getArtist();
        m.title = media.getTitle();
        m.duration = media.getDuration();
        m.progress = position;
        m.playlistPosition = playlistPosition;
        return m;
    }

    @NonNull
    private static WearPlaybackData.PlaybackState toPlaybackState(
            @PlaybackState.State final int state,
            final long duration,
            final long position) {
        final WearPlaybackData.PlaybackState m = new WearPlaybackData.PlaybackState();
        m.state = state;
        m.duration = duration;
        m.progress = position;
        return m;
    }

}
