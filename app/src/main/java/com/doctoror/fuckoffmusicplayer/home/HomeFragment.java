package com.doctoror.fuckoffmusicplayer.home;

import com.doctoror.fuckoffmusicplayer.library.LibraryListFragment;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.Observable;

/**
 * Created by Yaroslav Mytkalyk on 11.01.17.
 */

public final class HomeFragment extends LibraryListFragment {

    @Override
    protected Observable<Cursor> load(@Nullable final String filter) {
        return null;
    }

    @Override
    protected void onDataLoaded(@NonNull final Cursor data) {

    }

    @Override
    protected void onDataReset() {

    }
}
