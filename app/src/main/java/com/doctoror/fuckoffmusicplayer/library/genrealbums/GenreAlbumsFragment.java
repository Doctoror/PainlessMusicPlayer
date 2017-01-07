package com.doctoror.fuckoffmusicplayer.library.genrealbums;

import com.doctoror.fuckoffmusicplayer.Henson;
import com.doctoror.fuckoffmusicplayer.db.albums.AlbumsProvider;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.library.albums.conditional.ConditionalAlbumListFragment;
import com.f2prateek.dart.InjectExtra;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import rx.Observable;

/**
 * Used for showing albums for genre
 */
public final class GenreAlbumsFragment extends ConditionalAlbumListFragment {

    @NonNull
    public static GenreAlbumsFragment instantiate(@NonNull final Context context,
            @NonNull final Long genreId) {
        final GenreAlbumsFragment fragment = new GenreAlbumsFragment();
        final Bundle extras = Henson.with(context).gotoGenreAlbumsFragment()
                .genreId(genreId)
                .build()
                .getExtras();
        fragment.setArguments(extras);
        return fragment;
    }

    @InjectExtra
    Long genreId;

    @Inject
    AlbumsProvider mAlbumsProvider;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerHolder.getInstance(getActivity()).mainComponent().inject(this);
    }

    @Override
    protected Observable<Cursor> load() {
        return mAlbumsProvider.loadForGenre(genreId);
    }
}
