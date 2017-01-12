package com.doctoror.fuckoffmusicplayer.library.playlists;

import com.doctoror.fuckoffmusicplayer.Henson;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistsProvider;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.library.LibraryListFragment;
import com.doctoror.fuckoffmusicplayer.queue.Media;
import com.doctoror.fuckoffmusicplayer.queue.QueueActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * "Playlsits" fragment
 */
public final class PlaylistsFragment extends LibraryListFragment {

    private PlaylistsRecyclerAdapter mAdapter;

    @Inject
    PlaylistsProvider mPlaylistsProvider;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerHolder.getInstance(getActivity()).mainComponent().inject(this);

        mAdapter = new PlaylistsRecyclerAdapter(getActivity());
        mAdapter.setOnPlaylistClickListener(this::onPlaylistClick);

        setRecyclerAdapter(mAdapter);
    }

    @Override
    protected Observable<Cursor> load(@Nullable final String filter) {
        return mPlaylistsProvider.load(filter);
    }

    @Override
    protected void onDataLoaded(@NonNull final Cursor data) {
        mAdapter.changeCursor(data);
    }

    @Override
    protected void onDataReset() {
        mAdapter.changeCursor(null);
    }

    private void onPlaylistClick(@NonNull final View itemView, final long id,
            @NonNull final String name) {
        mPlaylistsProvider.loadQueue(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((queue) -> {
                    if (isAdded()) {
                        onPlaylistLoaded(itemView, name, queue);
                    }
                });
    }

    private void onPlaylistLoaded(@NonNull final View itemView,
            @NonNull final String name,
            @Nullable final List<Media> queue) {
        if (queue != null && !queue.isEmpty()) {
            final Intent intent = Henson.with(getActivity()).gotoQueueActivity()
                    .hasCoverTransition(false)
                    .hasItemViewTransition(true)
                    .isNowPlayingQueue(false)
                    .queue(queue)
                    .title(name)
                    .build();

            final ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(
                            getActivity(), itemView, QueueActivity.TRANSITION_NAME_ROOT);

            startActivity(intent, options.toBundle());
        } else {
            Toast.makeText(getActivity(), R.string.The_queue_is_empty,
                    Toast.LENGTH_SHORT).show();
        }
    }
}
