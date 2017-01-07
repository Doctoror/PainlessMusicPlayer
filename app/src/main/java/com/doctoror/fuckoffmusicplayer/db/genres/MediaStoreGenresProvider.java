package com.doctoror.fuckoffmusicplayer.db.genres;

import com.doctoror.fuckoffmusicplayer.util.SqlUtils;
import com.doctoror.rxcursorloader.RxCursorLoader;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import rx.Observable;
import rx.Single;

/**
 * MediaStore {@link GenresProvider}
 */
public final class MediaStoreGenresProvider implements GenresProvider {

    @NonNull
    private final ContentResolver mContentResolver;

    public MediaStoreGenresProvider(@NonNull final ContentResolver contentResolver) {
        mContentResolver = contentResolver;
    }

    @Override
    public Observable<Cursor> load(@Nullable final String searchFilter) {
        return RxCursorLoader.create(mContentResolver, newQuery(searchFilter));
    }

    @Override
    public Single<Cursor> loadOnce() {
        return RxCursorLoader.single(mContentResolver, newQuery(null));
    }

    @NonNull
    private static RxCursorLoader.Query newQuery(@Nullable final String searchFilter) {
        return new RxCursorLoader.Query.Builder()
                .setContentUri(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI)
                .setProjection(new String[]{
                        MediaStore.Audio.Genres._ID,
                        MediaStore.Audio.Genres.NAME
                })
                .setSortOrder(MediaStore.Audio.Genres.NAME)
                .setSelection(TextUtils.isEmpty(searchFilter) ? null : MediaStore.Audio.Genres.NAME
                        + " LIKE " + SqlUtils.escapeAndWrapForLikeArgument(searchFilter))
                .create();
    }
}
