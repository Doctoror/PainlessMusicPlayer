package com.doctoror.fuckoffmusicplayer.db.media;

import com.doctoror.fuckoffmusicplayer.playlist.Media;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

public interface MediaProvider {

    @NonNull
    List<Media> load(@Nullable final String selection,
            @Nullable final String[] selectionArgs,
            @Nullable final String orderBy,
            @Nullable final Integer limit);

    @NonNull
    List<Media> load(long id);
}
