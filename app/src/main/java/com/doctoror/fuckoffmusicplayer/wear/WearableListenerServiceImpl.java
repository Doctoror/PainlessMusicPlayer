package com.doctoror.fuckoffmusicplayer.wear;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import com.doctoror.commons.wear.DataPaths;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackService;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Created by Yaroslav Mytkalyk on 15.11.16.
 */

public final class WearableListenerServiceImpl extends WearableListenerService {

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        switch (messageEvent.getPath()) {
            case DataPaths.Messages.PLAY_PAUSE:
                PlaybackService.playPause(this);
                break;

            case DataPaths.Messages.PREV:
                PlaybackService.prev(this);
                break;

            case DataPaths.Messages.NEXT:
                PlaybackService.next(this);
                break;

            case DataPaths.Messages.SEEK: {
                final byte[] data = messageEvent.getData();
                if (data != null && data.length == 4) {
                    final float positionPercent = ByteBuffer.wrap(data).getFloat();
                    PlaybackService.seek(this, positionPercent);
                }
                break;
            }

            case DataPaths.Messages.PLAY_FROM_PLAYLIST: {
                final byte[] data = messageEvent.getData();
                if (data != null && data.length == 8) {
                    final long mediaId = ByteBuffer.wrap(data).getLong();
                    PlaybackService.playMediaFromPlaylist(this, mediaId);
                }
                break;
            }

            case DataPaths.Messages.SEARCH: {
                final byte[] data = messageEvent.getData();
                if (data != null) {
                    final String query = new String(data, Charset.forName("UTF-8"));
                    WearSearchProviderService.search(this, query);
                }
                break;
            }
        }
    }
}
