package com.doctoror.fuckoffmusicplayer.library.genrealbums;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.doctoror.fuckoffmusicplayer.domain.albums.AlbumsProvider;
import com.doctoror.fuckoffmusicplayer.library.albums.conditional.ConditionalAlbumListFragment;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.Observable;

/**
 * Used for showing albums for genre
 */
public final class GenreAlbumsFragment extends ConditionalAlbumListFragment {

    private static final String EXTRA_GENRE_ID = "EXTRA_GENRE_ID";

    @NonNull
    public static GenreAlbumsFragment instantiate(final long genreId) {
        final GenreAlbumsFragment fragment = new GenreAlbumsFragment();
        final Bundle extras = new Bundle();
        extras.putLong(EXTRA_GENRE_ID, genreId);
        fragment.setArguments(extras);
        return fragment;
    }

    private long genreId;

    @Inject
    AlbumsProvider mAlbumsProvider;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        genreId = getArguments().getLong(EXTRA_GENRE_ID);
        AndroidInjection.inject(this);
    }

    @Override
    protected Observable<Cursor> load() {
        return mAlbumsProvider.loadForGenre(genreId);
    }
}
