package com.doctoror.fuckoffmusicplayer.db.artists;

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
 * MediaStore {@link ArtistsProvider}
 */
public final class MediaStoreArtistsProvider implements ArtistsProvider {

    @NonNull
    private final ContentResolver mContentResolver;

    public MediaStoreArtistsProvider(@NonNull final ContentResolver contentResolver) {
        mContentResolver = contentResolver;
    }

    @Override
    public Observable<Cursor> load(@Nullable final String searchFilter) {
        return load(searchFilter, null);
    }

    @Override
    public Observable<Cursor> load(@Nullable final String searchFilter,
            @Nullable final Integer limit) {
        return RxCursorLoader.create(mContentResolver, newQuery(searchFilter, limit))
                .asObservable();
    }

    @NonNull
    private static RxCursorLoader.Query newQuery(@Nullable final String searchFilter,
            @Nullable final Integer limit) {
        final RxCursorLoader.Query.Builder b = new RxCursorLoader.Query.Builder()
                .setContentUri(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI)
                .setProjection(new String[]{
                        MediaStore.Audio.Artists._ID,
                        MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
                        MediaStore.Audio.Artists.ARTIST
                })
                .setSelection(TextUtils.isEmpty(searchFilter) ? null
                        : MediaStore.Audio.Artists.ARTIST + " LIKE "
                                + SqlUtils.escapeAndWrapForLikeArgument(searchFilter));

        if (limit == null) {
            b.setSortOrder(MediaStore.Audio.Artists.ARTIST);
        } else {
            b.setSortOrder(MediaStore.Audio.Artists.ARTIST + " LIMIT " + limit);
        }

        return b.create();
    }
}
