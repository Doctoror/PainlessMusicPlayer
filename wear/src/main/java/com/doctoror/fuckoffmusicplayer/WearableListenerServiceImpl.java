package com.doctoror.fuckoffmusicplayer;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemAsset;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;

import com.doctoror.commons.util.ByteStreams;
import com.doctoror.commons.wear.DataPaths;
import com.doctoror.commons.wear.nano.ProtoPlaybackData;
import com.doctoror.fuckoffmusicplayer.media.MediaHolder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yaroslav Mytkalyk on 15.11.16.
 */

public final class WearableListenerServiceImpl extends WearableListenerService {

    private static final String TAG = "WearableListenerService";

    private GoogleApiClient mGoogleApiClient;

    private MediaHolder mMediaHolder;

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaHolder = MediaHolder.getInstance(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
    }

    @Override
    public void onDataChanged(final DataEventBuffer dataEventBuffer) {
        super.onDataChanged(dataEventBuffer);
        for (final DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                final DataItem item = event.getDataItem();
                if (item != null) {
                    final String path = item.getUri().getPath();
                    switch (path) {
                        case DataPaths.Paths.MEDIA:
                            onMediaItemChanged(item);
                            readAlbumArt(item);
                            break;

                        case DataPaths.Paths.PLAYBACK_STATE:
                            onPlaybackStateItemChanged(item);
                            break;
                    }
                }
            }
        }
    }

    private void onMediaItemChanged(@NonNull final DataItem mediaItem) {
        final byte[] data = mediaItem.getData();
        if (data == null) {
            mMediaHolder.setMedia(null);
            return;
        }

        try {
            final ProtoPlaybackData.Media media = ProtoPlaybackData.Media.parseFrom(data);
            mMediaHolder.setMedia(media);
        } catch (InvalidProtocolBufferNanoException e) {
            Log.w(TAG, e);
        }
    }

    private void readAlbumArt(@NonNull final DataItem mediaItem) {
        final Map<String, DataItemAsset> assets = mediaItem.getAssets();
        if (assets != null) {
            readAlbumArt(assets.get(DataPaths.Assets.ALBUM_ART));
        }
    }

    private void readAlbumArt(@Nullable final DataItemAsset asset) {
        if (asset != null) {
            if (!mGoogleApiClient.isConnected()) {
                final ConnectionResult result = mGoogleApiClient
                        .blockingConnect(5, TimeUnit.SECONDS);
                if (!result.isSuccess()) {
                    return;
                }
            }
            // convert asset into a file descriptor and block until it's ready
            final InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                    mGoogleApiClient, asset).await().getInputStream();
            if (assetInputStream != null) {
                byte[] albumArt = null;
                try {
                    albumArt = ByteStreams.toByteArray(assetInputStream);
                } catch (IOException e) {
                    Log.w(TAG, e);
                } finally {
                    try {
                        assetInputStream.close();
                    } catch (IOException e) {
                        Log.w(TAG, e);
                    }
                }
                mMediaHolder.setAlbumArt(albumArt);
            }
        }
    }

    private void onPlaybackStateItemChanged(@NonNull final DataItem stateItem) {
        final byte[] data = stateItem.getData();
        if (data == null) {
            mMediaHolder.setPlaybackState(null);
            return;
        }
        try {
            final ProtoPlaybackData.PlaybackState s = ProtoPlaybackData.PlaybackState
                    .parseFrom(data);
            mMediaHolder.setPlaybackState(s);
        } catch (InvalidProtocolBufferNanoException e) {
            Log.w(TAG, e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }
}
