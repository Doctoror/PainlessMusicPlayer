package com.doctoror.fuckoffmusicplayer;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import com.doctoror.commons.wear.DataPaths;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.nio.ByteBuffer;
import java.util.Set;

/**
 * Created by Yaroslav Mytkalyk on 16.11.16.
 */

final class RemoteControl {

    private final Object mCapabilityLock = new Object();

    private String mPlaybackControlNodeId;

    void playPause(@NonNull final GoogleApiClient googleApiClient) {
        synchronized (mCapabilityLock) {
            if (googleApiClient.isConnected() && mPlaybackControlNodeId != null) {
                Wearable.MessageApi.sendMessage(googleApiClient, mPlaybackControlNodeId,
                        DataPaths.Messages.PLAY_PAUSE, null);
            }
        }
    }

    void prev(@NonNull final GoogleApiClient googleApiClient) {
        synchronized (mCapabilityLock) {
            if (googleApiClient.isConnected() && mPlaybackControlNodeId != null) {
                Wearable.MessageApi.sendMessage(googleApiClient, mPlaybackControlNodeId,
                        DataPaths.Messages.PREV, null);
            }
        }
    }

    void next(@NonNull final GoogleApiClient googleApiClient) {
        synchronized (mCapabilityLock) {
            if (googleApiClient.isConnected() && mPlaybackControlNodeId != null) {
                Wearable.MessageApi.sendMessage(googleApiClient, mPlaybackControlNodeId,
                        DataPaths.Messages.NEXT, null);
            }
        }
    }

    void seek(@NonNull final GoogleApiClient googleApiClient,
            final float seekPercent) {
        synchronized (mCapabilityLock) {
            if (googleApiClient.isConnected() && mPlaybackControlNodeId != null) {
                Wearable.MessageApi.sendMessage(googleApiClient, mPlaybackControlNodeId,
                        DataPaths.Messages.SEEK,
                        ByteBuffer.allocate(4).putFloat(seekPercent).array());
            }
        }
    }

    void updateRemoteControlCapability(@Nullable final CapabilityInfo capabilityInfo) {
        if (capabilityInfo != null) {
            synchronized (mCapabilityLock) {
                mPlaybackControlNodeId = pickBestNodeId(capabilityInfo.getNodes());
            }
        }
    }

    @Nullable
    private static String pickBestNodeId(@Nullable final Set<Node> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }
        String bestNodeId = null;
        for (final Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }

}
