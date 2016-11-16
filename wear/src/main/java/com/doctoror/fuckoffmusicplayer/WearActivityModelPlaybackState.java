package com.doctoror.fuckoffmusicplayer;

import android.databinding.BaseObservable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ObservableLong;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Yaroslav Mytkalyk on 15.11.16.
 */

public final class WearActivityModelPlaybackState {

    private final ObservableLong mDuration = new ObservableLong();

    private final ObservableLong mElapsedTime = new ObservableLong();
    private final ObservableInt mProgress = new ObservableInt();

    public ObservableInt getProgress() {
        return mProgress;
    }

    public void setProgress(final int progress) {
        mProgress.set(progress);
    }

    public ObservableLong getDuration() {
        return mDuration;
    }

    public void setDuration(final long duration) {
        mDuration.set(duration);
    }

    @NonNull
    public ObservableLong getElapsedTime() {
        return mElapsedTime;
    }

    public void setElapsedTime(final long elapsedTime) {
        mElapsedTime.set(elapsedTime);
    }


}
