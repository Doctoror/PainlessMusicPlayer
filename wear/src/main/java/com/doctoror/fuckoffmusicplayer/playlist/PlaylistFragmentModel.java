package com.doctoror.fuckoffmusicplayer.playlist;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

/**
 * Created by Yaroslav Mytkalyk on 17.11.16.
 */

public final class PlaylistFragmentModel {

    private final ObservableBoolean mIsEmpty = new ObservableBoolean();
    private final ObservableField<Drawable> mBackground = new ObservableField<>();
    private final ObservableField<RecyclerView.Adapter> mAdapter = new ObservableField<>();

    @NonNull
    public ObservableBoolean isEmpty() {
        return mIsEmpty;
    }

    void setIsEmpty(final boolean empty) {
        mIsEmpty.set(empty);
    }

    @NonNull
    public ObservableField<Drawable> getBackground() {
        return mBackground;
    }

    void setBackground(@Nullable final Drawable background) {
        mBackground.set(background);
    }

    @NonNull
    public ObservableField<RecyclerView.Adapter> getAdapter() {
        return mAdapter;
    }

    void setAdapter(@Nullable final RecyclerView.Adapter adapter) {
        mAdapter.set(adapter);
    }
}
