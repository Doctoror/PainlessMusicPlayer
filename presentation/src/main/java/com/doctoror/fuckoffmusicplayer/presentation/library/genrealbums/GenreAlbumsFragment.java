package com.doctoror.fuckoffmusicplayer.presentation.library.genrealbums;

import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.doctoror.fuckoffmusicplayer.domain.albums.AlbumsProvider;
import com.doctoror.fuckoffmusicplayer.presentation.library.albums.conditional.ConditionalAlbumListFragment;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
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
        final Bundle args = getArguments();
        if (args == null) {
            throw new IllegalStateException("Arguments must not be null");
        }
        genreId = args.getLong(EXTRA_GENRE_ID);
        AndroidSupportInjection.inject(this);
    }

    @Override
    protected Observable<Cursor> load() {
        return mAlbumsProvider.loadForGenre(genreId);
    }
}
