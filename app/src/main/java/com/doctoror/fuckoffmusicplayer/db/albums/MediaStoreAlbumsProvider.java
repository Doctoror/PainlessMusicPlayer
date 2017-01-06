package com.doctoror.fuckoffmusicplayer.db.albums;

import com.doctoror.fuckoffmusicplayer.playlist.RecentPlaylistsManager;
import com.doctoror.fuckoffmusicplayer.util.SelectionUtils;
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
 * MediaStore {@link AlbumsProvider}
 */
public final class MediaStoreAlbumsProvider implements AlbumsProvider {

    @NonNull
    private final ContentResolver mContentResolver;

    @NonNull
    private final RecentPlaylistsManager mRecentPlaylistsManager;

    public MediaStoreAlbumsProvider(@NonNull final ContentResolver contentResolver,
            @NonNull final RecentPlaylistsManager recentPlaylistsManager) {
        mContentResolver = contentResolver;
        mRecentPlaylistsManager = recentPlaylistsManager;
    }

    @Override
    public Observable<Cursor> load(@Nullable final String searchFilter) {
        return RxCursorLoader.create(mContentResolver, newParams(searchFilter)).asObservable();
    }

    @Override
    public Single<Cursor> loadRecentlyPlayedAlbumsOnce() {
        final long[] recentlyPlayedAlbums = mRecentPlaylistsManager.getRecentAlbums();
        final RxCursorLoader.Query.Builder query = newParamsBuilder();
        query
                .setSelection(SelectionUtils.inSelectionLong(MediaStore.Audio.Albums._ID,
                        recentlyPlayedAlbums))

                .setSortOrder(SelectionUtils.orderByLongField(MediaStore.Audio.Albums._ID,
                        recentlyPlayedAlbums));

        return RxCursorLoader.single(mContentResolver, query.create());
    }

    /**
     * Constructs params for albums search.
     *
     * @param searchFilter the user input filter string. May be null if no filtering needed
     * @return params
     */
    @NonNull
    private static RxCursorLoader.Query newParams(@Nullable final String searchFilter) {
        return newParamsBuilder().setSelection(TextUtils.isEmpty(searchFilter) ? null :
                MediaStore.Audio.Albums.ALBUM + " LIKE " + SqlUtils.escapeAndWrapForLikeArgument(
                        searchFilter)).create();
    }

    /**
     * Constructs params Builder for albums search.
     *
     * @return params Builder
     */
    @NonNull
    private static RxCursorLoader.Query.Builder newParamsBuilder() {
        return new RxCursorLoader.Query.Builder()
                .setContentUri(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI)
                .setProjection(new String[]{
                        MediaStore.Audio.Albums._ID,
                        MediaStore.Audio.Albums.ALBUM,
                        MediaStore.Audio.Albums.ALBUM_ART
                })
                .setSortOrder(MediaStore.Audio.Albums.ALBUM);
    }
}
