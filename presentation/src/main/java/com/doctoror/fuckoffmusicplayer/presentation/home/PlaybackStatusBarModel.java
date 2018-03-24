package com.doctoror.fuckoffmusicplayer.presentation.home;

import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Playback status bar model
 */
public final class PlaybackStatusBarModel {

    private final ObservableField<CharSequence> title = new ObservableField<>();
    private final ObservableField<CharSequence> artist = new ObservableField<>();

    private final ObservableField<String> imageUri = new ObservableField<>();
    private final ObservableInt btnPlayRes = new ObservableInt();

    @NonNull
    public ObservableField<String> getImageUri() {
        return imageUri;
    }

    void setImageUri(@Nullable final String imageUri) {
        this.imageUri.set(imageUri);
    }

    @NonNull
    public ObservableInt getBtnPlayRes() {
        return btnPlayRes;
    }

    void setBtnPlayRes(final int btnPlayRes) {
        this.btnPlayRes.set(btnPlayRes);
    }

    @NonNull
    public ObservableField<CharSequence> getTitle() {
        return title;
    }

    public void setTitle(@Nullable final CharSequence title) {
        this.title.set(title);
    }

    @NonNull
    public ObservableField<CharSequence> getArtist() {
        return artist;
    }

    public void setArtist(@Nullable final CharSequence artist) {
        this.artist.set(artist);
    }
}
