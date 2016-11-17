package com.doctoror.fuckoffmusicplayer.root;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.annotation.Nullable;

/**
 * Created by Yaroslav Mytkalyk on 17.11.16.
 */

public final class RootActivityModel {

    private final ObservableInt mAnimatorChild = new ObservableInt();
    private final ObservableField<CharSequence> mMessage = new ObservableField<>();

    private final ObservableBoolean mProgressVisible = new ObservableBoolean();
    private final ObservableBoolean mFixButtonVisible = new ObservableBoolean();

    public ObservableBoolean isFixButtonVisible() {
        return mFixButtonVisible;
    }

    public void setFixButtonVisible(final boolean visible) {
        mFixButtonVisible.set(visible);
    }

    public ObservableBoolean isProgressVisible() {
        return mProgressVisible;
    }

    public void setProgressVisible(final boolean visible) {
        mProgressVisible.set(visible);
    }

    public ObservableInt getAnimatorChild() {
        return mAnimatorChild;
    }

    public void setAnimatorChild(final int child) {
        mAnimatorChild.set(child);
    }

    public ObservableField<CharSequence> getMessage() {
        return mMessage;
    }

    public void setMessage(@Nullable final CharSequence message) {
        mMessage.set(message);
    }

}
