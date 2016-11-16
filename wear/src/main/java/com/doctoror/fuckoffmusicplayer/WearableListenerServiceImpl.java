package com.doctoror.fuckoffmusicplayer;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;

import com.doctoror.commons.wear.DataPaths;
import com.doctoror.commons.wear.nano.ProtoPlaybackData;
import com.doctoror.fuckoffmusicplayer.media.MediaHolder;

import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Yaroslav Mytkalyk on 15.11.16.
 */

public final class WearableListenerServiceImpl extends WearableListenerService {

    private static final String TAG = "WearableListenerService";

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private MediaHolder mMediaHolder;

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaHolder = MediaHolder.getInstance(this);
    }

    @Override
    public void onDataChanged(final DataEventBuffer dataEventBuffer) {
        super.onDataChanged(dataEventBuffer);
        for (final DataEvent event : dataEventBuffer) {
            final DataItem item = event.getDataItem();
            if (item != null) {
                final String path = item.getUri().getPath();
                switch (path) {
                    case DataPaths.PATH_MEDIA:
                        if (event.getType() == DataEvent.TYPE_CHANGED) {
                            onMediaItemChanged(item);
                        } else if (event.getType() == DataEvent.TYPE_DELETED) {
                            onMediaItemChanged(null);
                        }
                        break;

                    case DataPaths.PATH_PLAYBACK_STATE:
                        if (event.getType() == DataEvent.TYPE_CHANGED) {
                            onPlaybackStateItemChanged(item);
                        } else if (event.getType() == DataEvent.TYPE_DELETED) {
                            onPlaybackStateItemChanged(null);
                        }
                        break;
                }
            }
        }
    }

    private void onMediaItemChanged(@Nullable final DataItem mediaItem) {
        if (mediaItem == null) {
            mMediaHolder.setMedia(null);
            return;
        }
        final byte[] data = mediaItem.getData();
        if (data == null) {
            mMediaHolder.setMedia(null);
            return;
        }

        mExecutor.submit(() -> {
            try {
                final ProtoPlaybackData.Media media = ProtoPlaybackData.Media.parseFrom(data);
                mMediaHolder.setMedia(media);
            } catch (InvalidProtocolBufferNanoException e) {
                Log.w(TAG, e);
            }
        });
    }

    private void onPlaybackStateItemChanged(@Nullable final DataItem stateItem) {
        if (stateItem == null) {
            mMediaHolder.setPlaybackState(null);
            return;
        }

        final byte[] data = stateItem.getData();
        if (data == null) {
            mMediaHolder.setPlaybackState(null);
            return;
        }
        mExecutor.submit(() -> {
            try {
                final ProtoPlaybackData.PlaybackState s = ProtoPlaybackData.PlaybackState
                        .parseFrom(data);
                mMediaHolder.setPlaybackState(s);
            } catch (InvalidProtocolBufferNanoException e) {
                Log.w(TAG, e);
            }
        });
    }
}
