/*
 * Copyright (C) 2016 Yaroslav Mytkalyk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.doctoror.fuckoffmusicplayer.wear.queue;

import com.doctoror.commons.wear.nano.WearPlaybackData;
import com.doctoror.fuckoffmusicplayer.wear.media.eventbus.EventQueue;

import org.greenrobot.eventbus.EventBus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

/**
 * Holds current playlist
 */
public final class QueueHolder {

    // Is not a leak since it's an application context
    @SuppressLint("StaticFieldLeak")
    private static volatile QueueHolder sInstance;

    @NonNull
    public static QueueHolder getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            synchronized (QueueHolder.class) {
                if (sInstance == null) {
                    sInstance = new QueueHolder(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    @NonNull
    private final Context mContext;

    private WearPlaybackData.Queue mQueue;

    private QueueHolder(@NonNull final Context context) {
        mContext = context;
        mQueue = QueuePersister.read(context);
    }

    @WorkerThread
    public synchronized void setQueue(@Nullable final WearPlaybackData.Queue queue) {
        if (mQueue != queue) {
            mQueue = queue;
            notifyChanged(queue);
            QueuePersister.persist(mContext, queue);
        }
    }

    @Nullable
    public WearPlaybackData.Queue getQueue() {
        return mQueue;
    }

    private void notifyChanged(@Nullable final WearPlaybackData.Queue queue) {
        EventBus.getDefault().post(new EventQueue(queue));
    }
}
