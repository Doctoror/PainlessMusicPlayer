package com.doctoror.fuckoffmusicplayer.wear;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.protobuf.nano.MessageNano;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.doctoror.commons.playback.PlaybackState;
import com.doctoror.commons.util.Log;
import com.doctoror.commons.util.ProtoUtils;
import com.doctoror.commons.wear.DataPaths;
import com.doctoror.commons.wear.nano.WearPlaybackData;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackService;
import com.doctoror.fuckoffmusicplayer.playback.data.PlaybackData;
import com.doctoror.fuckoffmusicplayer.queue.Media;
import com.doctoror.fuckoffmusicplayer.reporter.PlaybackReporter;
import com.doctoror.fuckoffmusicplayer.util.CollectionUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

/**
 * {@link PlaybackReporter} for wear
 */
public final class WearableMediaPlaybackReporter implements PlaybackReporter {

    private static final String TAG = "WearableMediaPlaybackReporter";

    @NonNull
    private final RequestManager mGlide;

    @NonNull
    private final GoogleApiClient mGoogleApiClient;

    @Inject
    PlaybackData mPlaybackData;

    public WearableMediaPlaybackReporter(@NonNull final Context context) {
        DaggerHolder.getInstance(context).wearComponent().inject(this);
        mGlide = Glide.with(context);
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClientCallbacks())
                .build();

        mGoogleApiClient.connect();
    }

    private final class GoogleApiClientCallbacks implements GoogleApiClient.ConnectionCallbacks {

        @Override
        public void onConnected(@Nullable final Bundle bundle) {
            final List<Media> queue = mPlaybackData.getQueue();
            if (queue != null) {
                reportQueueChanged(queue);
                final int pos = mPlaybackData.getQueuePosition();
                final Media media = CollectionUtils.getItemSafe(queue, pos);
                if (media != null) {
                    reportTrackChanged(media, pos);
                    reportPositionChanged(media.getId(), mPlaybackData.getMediaPosition());
                }
                reportPlaybackStateChanged(PlaybackService.getLastKnownState(), null);
            }
        }

        @Override
        public void onConnectionSuspended(final int i) {
            // ignored
        }
    };

    @Override
    public void onDestroy() {
        mGoogleApiClient.disconnect();
    }

    @Override
    public void reportTrackChanged(@NonNull final Media media,
            final int positionInQueue) {
        if (mGoogleApiClient.isConnected()) {
            final PutDataRequest request;
            try {
                request = newPutMediaRequest(media, positionInQueue);
            } catch (IOException e) {
                Log.w(TAG, e);
                return;
            }
            request.setUrgent();

            final String art = media.getAlbumArt();
            byte[] albumArt = null;
            if (!TextUtils.isEmpty(art)) {
                try {
                    final Bitmap artBitmap = mGlide.load(art).asBitmap().into(320, 320).get();
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
            Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();
        }
    }

    @Override
    public void reportPlaybackStateChanged(@PlaybackState.State final int state,
            @Nullable final CharSequence errorMessage) {
        if (mGoogleApiClient.isConnected()) {
            final PutDataRequest request;
            try {
                request = newPutStateRequest(state);
            } catch (IOException e) {
                Log.w(TAG, e);
                return;
            }
            request.setUrgent();
            Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();
        }
    }

    @Override
    public void reportPositionChanged(final long mediaId, final long position) {
        if (mGoogleApiClient.isConnected()) {
            final PutDataRequest request;
            try {
                request = newPutPositionRequest(mediaId, position);
            } catch (IOException e) {
                Log.w(TAG, e);
                return;
            }
            request.setUrgent();
            Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();
        }
    }

    @Override
    public void reportQueueChanged(@Nullable final List<Media> queue) {
        if (mGoogleApiClient.isConnected() && queue != null && !queue.isEmpty()) {
            final int size = queue.size();
            final WearPlaybackData.Media[] wMedias = new WearPlaybackData.Media[queue.size()];
            for (int i = 0; i < size; i++) {
                wMedias[i] = toWearableMedia(queue.get(i), 0);
            }

            final PutDataRequest request;
            try {
                request = PutDataRequest.create(DataPaths.Paths.QUEUE);
                final WearPlaybackData.Queue wQueue = new WearPlaybackData.Queue();
                wQueue.media = wMedias;
                request.setData(ProtoUtils.toByteArray(wQueue));
            } catch (IOException e) {
                Log.w(TAG, e);
                return;
            }
            request.setUrgent();

            Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();
        }
    }

    @NonNull
    private static PutDataRequest newPutMediaRequest(@NonNull final Media media,
            final int positionInQueue) throws IOException {
        final PutDataRequest request = PutDataRequest.create(DataPaths.Paths.MEDIA);
        request.setData(ProtoUtils.toByteArray(toWearableMedia(media, positionInQueue)));
        return request;
    }

    @NonNull
    private static PutDataRequest newPutStateRequest(
            @PlaybackState.State final int state) throws IOException {
        final PutDataRequest request = PutDataRequest.create(DataPaths.Paths.PLAYBACK_STATE);
        request.setData(ProtoUtils.toByteArray(toWearablePlaybackState(state)));
        return request;
    }

    @NonNull
    private static PutDataRequest newPutPositionRequest(final long mediaId, final long position)
            throws IOException {
        final PutDataRequest request = PutDataRequest.create(DataPaths.Paths.PLAYBACK_POSITION);
        request.setData(
                ProtoUtils.toByteArray(toWearablePlaybackPosition(mediaId, position)));
        return request;
    }

    @NonNull
    private static WearPlaybackData.Media toWearableMedia(@NonNull final Media media,
            final int positionInQueue) {
        final WearPlaybackData.Media m = new WearPlaybackData.Media();
        m.id = media.getId();
        m.album = media.getAlbum();
        m.artist = media.getArtist();
        m.title = media.getTitle();
        m.duration = media.getDuration();
        m.positionInQueue = positionInQueue;
        return m;
    }

    @NonNull
    private static MessageNano toWearablePlaybackState(
            @PlaybackState.State final int state) {
        final WearPlaybackData.PlaybackState m = new WearPlaybackData.PlaybackState();
        m.state = state;
        return m;
    }

    @NonNull
    private static MessageNano toWearablePlaybackPosition(final long mediaId,
            final long position) {
        final WearPlaybackData.PlaybackPosition m = new WearPlaybackData.PlaybackPosition();
        m.mediaId = mediaId;
        m.position = position;
        return m;
    }
}
