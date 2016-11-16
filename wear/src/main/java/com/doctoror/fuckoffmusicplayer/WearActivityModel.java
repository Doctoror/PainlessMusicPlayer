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

public final class WearActivityModel extends BaseObservable {

    private CharSequence mTitle;
    private CharSequence mArtistAndAlbum;
    private Drawable mStateIcon;
    private long mDuration;

    private final ObservableField<Drawable> mArt = new ObservableField<>();

    private final ObservableLong mElapsedTime = new ObservableLong();
    private final ObservableInt mProgress = new ObservableInt();

    private final ObservableInt mBtnPlayRes = new ObservableInt();

    private final ObservableInt mAnimatorChild = new ObservableInt();
    private final ObservableField<CharSequence> mMessage = new ObservableField<>();

    private final ObservableBoolean mProgressVisible = new ObservableBoolean();
    private final ObservableBoolean mFixButtonVisible = new ObservableBoolean();
    private final ObservableBoolean mNavigationButtonsVisible = new ObservableBoolean();

    public ObservableBoolean navigationButtonsVisible() {
        return mNavigationButtonsVisible;
    }

    public void setNavigationButtonsVisible(final boolean visible) {
        mNavigationButtonsVisible.set(visible);
    }

    public ObservableBoolean isFixButtonVisible() {
        return mFixButtonVisible;
    }

    public void setFixButtonVisible(final boolean visible) {
        mFixButtonVisible.set(visible);
    }

    public ObservableField<Drawable> getArt() {
        return mArt;
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

    public void setArt(final Drawable art) {
        mArt.set(art);
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public void setTitle(final CharSequence title) {
        mTitle = title;
    }

    public CharSequence getArtistAndAlbum() {
        return mArtistAndAlbum;
    }

    public void setArtistAndAlbum(final CharSequence artistAndAlbum) {
        mArtistAndAlbum = artistAndAlbum;
    }

    public Drawable getStateIcon() {
        return mStateIcon;
    }

    public void setStateIcon(final Drawable stateIcon) {
        mStateIcon = stateIcon;
    }

    public ObservableInt getProgress() {
        return mProgress;
    }

    public void setProgress(final int progress) {
        mProgress.set(progress);
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(final long duration) {
        mDuration = duration;
    }

    @NonNull
    public ObservableLong getElapsedTime() {
        return mElapsedTime;
    }

    public void setElapsedTime(final long elapsedTime) {
        mElapsedTime.set(elapsedTime);
    }

    @NonNull
    public ObservableInt getBtnPlayRes() {
        return mBtnPlayRes;
    }

    public void setBtnPlayRes(final int btnPlayRes) {
        mBtnPlayRes.set(btnPlayRes);
    }

}
