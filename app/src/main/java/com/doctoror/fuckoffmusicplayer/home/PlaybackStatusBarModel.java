package com.doctoror.fuckoffmusicplayer.home;

import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Playback status bar model
 */
public final class PlaybackStatusBarModel {

    private final ObservableField<CharSequence> mTitle = new ObservableField<>();
    private final ObservableField<CharSequence> mArtist = new ObservableField<>();

    private final ObservableField<String> mImageUri = new ObservableField<>();
    private final ObservableInt mBtnPlayRes = new ObservableInt();

    @NonNull
    public ObservableField<String> getImageUri() {
        return mImageUri;
    }

    public void setImageUri(@Nullable final String imageUri) {
        mImageUri.set(imageUri);
    }

    @NonNull
    public ObservableInt getBtnPlayRes() {
        return mBtnPlayRes;
    }

    public void setBtnPlayRes(final int btnPlayRes) {
        mBtnPlayRes.set(btnPlayRes);
    }

    @NonNull
    public ObservableField<CharSequence> getTitle() {
        return mTitle;
    }

    public void setTitle(@Nullable final CharSequence title) {
        mTitle.set(title);
    }

    @NonNull
    public ObservableField<CharSequence> getArtist() {
        return mArtist;
    }

    public void setArtist(@Nullable final CharSequence artist) {
        mArtist.set(artist);
    }
}
