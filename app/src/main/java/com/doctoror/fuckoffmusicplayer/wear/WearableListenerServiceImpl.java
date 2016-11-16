package com.doctoror.fuckoffmusicplayer.wear;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import com.doctoror.commons.wear.DataPaths;

/**
 * Created by Yaroslav Mytkalyk on 15.11.16.
 */

public final class WearableListenerServiceImpl extends WearableListenerService {

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        switch (messageEvent.getPath()) {
            case DataPaths.PATH_MESSAGE_PLAY_PAUSE:
                break;

            case DataPaths.PATH_MESSAGE_PREV:
                break;

            case DataPaths.PATH_MESSAGE_NEXT:
                break;
        }
    }
}
