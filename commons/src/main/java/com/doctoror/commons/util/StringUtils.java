package com.doctoror.commons.util;

import com.doctoror.commons.R;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by Yaroslav Mytkalyk on 15.11.16.
 */

public final class StringUtils {

    private StringUtils() {
        throw new IllegalArgumentException();
    }

    @NonNull
    public static String formatArtistAndAlbum(@NonNull final Resources res,
            @Nullable String artist,
            @Nullable String album) {
        if (TextUtils.isEmpty(artist)) {
            artist = res.getString(R.string.Unknown_artist);
        }
        if (TextUtils.isEmpty(album)) {
            album = res.getString(R.string.Unknown_album);
        }
        return artist + res.getString(R.string.artist_album_separator) + album;
    }
}
