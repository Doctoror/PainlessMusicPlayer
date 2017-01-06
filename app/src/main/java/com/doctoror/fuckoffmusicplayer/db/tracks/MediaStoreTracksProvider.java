package com.doctoror.fuckoffmusicplayer.db.tracks;

import com.doctoror.fuckoffmusicplayer.util.SqlUtils;
import com.doctoror.rxcursorloader.RxCursorLoader;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import rx.Observable;

/**
 * Created by Yaroslav Mytkalyk on 06.01.17.
 */

public final class MediaStoreTracksProvider implements TracksProvider {

    @NonNull
    private final ContentResolver mContentResolver;

    public MediaStoreTracksProvider(@NonNull final ContentResolver contentResolver) {
        mContentResolver = contentResolver;
    }

    @Override
    public Observable<Cursor> load(@Nullable final String searchFilter) {
        return RxCursorLoader.create(mContentResolver, newParams(searchFilter)).asObservable();
    }

    @NonNull
    private static RxCursorLoader.Query newParams(@Nullable final String searchFilter) {
        final String wrapped = TextUtils.isEmpty(searchFilter) ? null
                : SqlUtils.escapeAndWrapForLikeArgument(searchFilter);
        final StringBuilder selection = new StringBuilder(256);
        selection.append(SELECTION_NON_HIDDEN_MUSIC);
        if (!TextUtils.isEmpty(wrapped)) {
            selection.append(" AND ")

                    .append(MediaStore.Audio.Media.TITLE)
                    .append(" LIKE ").append(wrapped).append(" OR ")

                    .append(MediaStore.Audio.Media.ARTIST)
                    .append(" LIKE ").append(wrapped);
        }
        return new RxCursorLoader.Query.Builder()
                .setContentUri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                .setProjection(new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST
                })
                .setSortOrder(SORT_ORDER)
                .setSelection(selection.toString())
                .create();
    }

}
