/*
 * Copyright (C) 2017 Yaroslav Mytkalyk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.doctoror.fuckoffmusicplayer.presentation.library.playlists;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.domain.playlist.RecentActivityManager;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderPlaylists;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderRandom;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderRecentlyScanned;
import com.doctoror.fuckoffmusicplayer.presentation.Henson;
import com.doctoror.fuckoffmusicplayer.presentation.library.LibraryListFragment;
import com.doctoror.fuckoffmusicplayer.presentation.library.recentalbums.RecentAlbumsActivity;
import com.doctoror.fuckoffmusicplayer.presentation.queue.QueueActivity;
import com.doctoror.fuckoffmusicplayer.presentation.util.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * "Playlsits" fragment
 */
public final class PlaylistsFragment extends LibraryListFragment {

    private PlaylistsRecyclerAdapter mAdapter;

    private Unbinder mUnbinder;
    private boolean mLoading;

    private Toast mNoTracksToast;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @Inject
    QueueProviderPlaylists mPlaylistsProvider;

    @Inject
    RecentActivityManager mRecentActivityManager;

    @Inject
    QueueProviderRecentlyScanned mRecentlyScannedPlaylistFactory;

    @Inject
    QueueProviderRandom mRandomPlaylistFactory;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSupportInjection.inject(this);
        setCanShowEmptyView(false);

        final Activity activity = getActivity();
        if (activity == null) {
            throw new IllegalStateException("Activity is null");
        }

        mAdapter = new PlaylistsRecyclerAdapter(activity,
                generateLivePlaylists(getResources()));
        mAdapter.setOnPlaylistClickListener(new OnPlaylistClickListener());

        setRecyclerAdapter(mAdapter);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            mUnbinder = ButterKnife.bind(this, view);
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mUnbinder != null) {
            mUnbinder.unbind();
            mUnbinder = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        clearLoadingFlag();
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

    @SuppressLint("ShowToast")
    private void showNoTracksToast() {
        if (mNoTracksToast == null) {
            mNoTracksToast = Toast.makeText(getActivity(), R.string.The_queue_is_empty,
                    Toast.LENGTH_LONG);
        }
        if (mNoTracksToast.getView().getWindowToken() == null) {
            mNoTracksToast.show();
        }
    }

    private void clearLoadingFlag() {
        mLoading = false;
    }

    @Nullable
    private View itemViewForPosition(final int position) {
        return ViewUtils.getItemView(mRecyclerView, position);
    }

    private void loadLivePlaylistAndPlay(@NonNull final LivePlaylist livePlaylist,
                                         final int position) {
        if (mLoading) {
            return;
        } else {
            mLoading = true;
        }

        final Activity activity = getActivity();
        if (activity == null) {
            clearLoadingFlag();
            return;
        }

        switch (livePlaylist.getType()) {
            case LivePlaylist.TYPE_RECENTLY_PLAYED_ALBUMS:
                clearLoadingFlag();
                goToRecentAlbumsActivity(activity, position);
                break;

            case LivePlaylist.TYPE_RECENTLY_SCANNED:
                loadLivePlaylistAndPlay(position,
                        livePlaylist.getTitle().toString(),
                        mRecentlyScannedPlaylistFactory.recentlyScannedQueue());
                break;

            case LivePlaylist.TYPE_RANDOM_PLAYLIST:
                loadLivePlaylistAndPlay(position,
                        livePlaylist.getTitle().toString(),
                        mRandomPlaylistFactory.randomQueue());
                break;

        }
    }

    private void loadLivePlaylistAndPlay(final int position,
                                         @NonNull final String name,
                                         @NonNull final Observable<List<Media>> queueSource) {
        disposeOnStop(queueSource
                .take(1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(q -> onLivePlaylistLoaded(position, name, q),
                        this::onLivePlaylistLoadFailed,
                        this::onLivePlaylistLoadComplete));
    }

    private void onLivePlaylistLoaded(final int position,
                                      @NonNull final String name,
                                      @NonNull final List<Media> medias) {
        if (isAdded()) {
            onQueueLoaded(itemViewForPosition(position), name, medias);
        }
    }

    private void onLivePlaylistLoadFailed(@NonNull final Throwable t) {
        if (isAdded()) {
            clearLoadingFlag();
            Toast.makeText(getActivity(),
                    getString(R.string.Failed_to_load_data_s, t.getMessage()),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void onLivePlaylistLoadComplete() {
        if (isAdded()) {
            clearLoadingFlag();
        }
    }

    private void goToRecentAlbumsActivity(@NonNull final Activity context,
                                          final int position) {
        final long[] recentAlbums = mRecentActivityManager.getRecentlyPlayedAlbums();
        if (recentAlbums.length == 0) {
            Toast.makeText(context, R.string.You_played_no_albums_yet, Toast.LENGTH_LONG).show();
        } else {
            final Intent intent = Henson.with(context).gotoRecentAlbumsActivity()
                    .build();

            final View view = itemViewForPosition(position);
            if (view != null) {
                final ActivityOptionsCompat options = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(context, view,
                                RecentAlbumsActivity.TRANSITION_NAME_ROOT);

                startActivity(intent, options.toBundle());
            } else {
                startActivity(intent);
            }
        }
    }

    @NonNull
    private static List<LivePlaylist> generateLivePlaylists(@NonNull final Resources res) {
        final List<LivePlaylist> livePlaylists = new ArrayList<>(3);
        livePlaylists.add(new LivePlaylist(LivePlaylist.TYPE_RECENTLY_PLAYED_ALBUMS,
                res.getText(R.string.Recently_played_albums)));

        livePlaylists.add(new LivePlaylist(LivePlaylist.TYPE_RECENTLY_SCANNED,
                res.getText(R.string.Recently_added)));

        livePlaylists.add(new LivePlaylist(LivePlaylist.TYPE_RANDOM_PLAYLIST,
                res.getText(R.string.Random_playlist)));
        return livePlaylists;
    }

    private void onQueueLoaded(@Nullable final View itemView,
                               @Nullable final String name,
                               @Nullable final List<Media> queue) {
        if (queue != null && !queue.isEmpty()) {
            final Activity activity = getActivity();
            if (activity != null) {
                final Intent intent = Henson.with(activity).gotoQueueActivity()
                        .hasCoverTransition(false)
                        .hasItemViewTransition(true)
                        .isNowPlayingQueue(false)
                        .queue(queue)
                        .title(name)
                        .build();

                Bundle options = null;
                if (itemView != null) {
                    options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                            itemView, QueueActivity.TRANSITION_NAME_ROOT).toBundle();

                }
                startActivity(intent, options);
            }
        } else {
            showNoTracksToast();
            clearLoadingFlag();
        }
    }

    private final class OnPlaylistClickListener
            implements PlaylistsRecyclerAdapter.OnPlaylistClickListener {

        @Override
        public void onLivePlaylistClick(@NonNull final LivePlaylist playlist, final int position) {
            loadLivePlaylistAndPlay(playlist, position);
        }

        @Override
        public void onPlaylistClick(final long id,
                                    @Nullable final String name,
                                    final int position) {
            disposeOnStop(mPlaylistsProvider.loadQueue(id)
                    .take(1)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(q -> onQueueLoaded(itemViewForPosition(position), name, q),
                            PlaylistsFragment.this::onLivePlaylistLoadFailed));
        }

        @Override
        public void onPlaylistDeleteClick(final long id, @Nullable final String name) {
            final Activity activity = getActivity();
            final FragmentManager fragmentManager = getFragmentManager();
            if (activity != null && fragmentManager != null) {
                DeletePlaylistDialogFragment.show(activity,
                        fragmentManager,
                        id,
                        name);
            }
        }
    }
}
