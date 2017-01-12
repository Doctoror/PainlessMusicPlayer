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
package com.doctoror.fuckoffmusicplayer.library.playlists;

import com.doctoror.fuckoffmusicplayer.Henson;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderRandom;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderRecentlyScanned;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistsProvider;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.library.LibraryListFragment;
import com.doctoror.fuckoffmusicplayer.library.recentalbums.RecentAlbumsActivity;
import com.doctoror.fuckoffmusicplayer.playlist.RecentPlaylistsManager;
import com.doctoror.fuckoffmusicplayer.queue.Media;
import com.doctoror.fuckoffmusicplayer.queue.QueueActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * "Playlsits" fragment
 */
public final class PlaylistsFragment extends LibraryListFragment {

    private static final String TAG_DIALOG_DELETE = "PlaylistsFragment.TAG_DIALOG_DELETE";

    private PlaylistsRecyclerAdapter mAdapter;

    private Unbinder mUnbinder;
    private boolean mLoading;

    private Toast mNoTracksToast;
    private Subscription mLoadPlaylistSubscription;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @Inject
    PlaylistsProvider mPlaylistsProvider;

    @Inject
    RecentPlaylistsManager mRecentPlaylistsManager;

    @Inject
    PlaylistProviderRecentlyScanned mRecentlyScannedPlaylistFactory;

    @Inject
    PlaylistProviderRandom mRandomPlaylistFactory;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerHolder.getInstance(getActivity()).mainComponent().inject(this);
        setCanShowEmptyView(false);

        mAdapter = new PlaylistsRecyclerAdapter(getActivity(),
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
        unsubscribeFromPlaylistLoad();
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

    private void unsubscribeFromPlaylistLoad() {
        if (mLoadPlaylistSubscription != null) {
            mLoadPlaylistSubscription.unsubscribe();
            mLoadPlaylistSubscription = null;
        }
    }

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
        final RecyclerView.LayoutManager lm = mRecyclerView.getLayoutManager();
        return lm != null ? lm.findViewByPosition(position) : null;
    }

    private void loadLivePlaylistAndPlay(@NonNull final LivePlaylist livePlaylist,
            final int position) {
        if (mLoading) {
            return;
        } else {
            mLoading = true;
        }
        unsubscribeFromPlaylistLoad();

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
                        () -> mRecentlyScannedPlaylistFactory.recentlyScannedPlaylist());
                break;

            case LivePlaylist.TYPE_RANDOM_PLAYLIST:
                loadLivePlaylistAndPlay(position,
                        livePlaylist.getTitle().toString(),
                        () -> mRandomPlaylistFactory.randomPlaylist());
                break;

        }
    }

    private void loadLivePlaylistAndPlay(final int position,
            @NonNull final String name,
            @NonNull final LoadPlaylistAction action) {
        mLoadPlaylistSubscription = Observable.<List<Media>>create(
                s -> s.onNext(action.load()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Media>>() {

                    @Override
                    public void onCompleted() {
                        if (isAdded()) {
                            clearLoadingFlag();
                        }
                    }

                    @Override
                    public void onError(final Throwable e) {
                        if (isAdded()) {
                            clearLoadingFlag();
                            Toast.makeText(getActivity(),
                                    getString(R.string.Failed_to_load_data_s, e.getMessage()),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onNext(final List<Media> medias) {
                        if (isAdded()) {
                            onPlaylistLoaded(itemViewForPosition(position), name, medias);
                        }
                    }
                });
    }

    private void goToRecentAlbumsActivity(@NonNull final Activity context,
            final int position) {
        final long[] recentAlbums = mRecentPlaylistsManager.getRecentAlbums();
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

    private void onPlaylistLoaded(@Nullable final View itemView,
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

            Bundle options = null;
            if (itemView != null) {
                options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                        itemView, QueueActivity.TRANSITION_NAME_ROOT).toBundle();

            }
            startActivity(intent, options);
        } else {
            showNoTracksToast();
            clearLoadingFlag();
        }
    }

    private final class OnPlaylistClickListener
            implements PlaylistsRecyclerAdapter.OnPlaylistClickListener {

        @Override
        public void onLivePlaylistClick(final LivePlaylist playlist, final int position) {
            loadLivePlaylistAndPlay(playlist, position);
        }

        @Override
        public void onPlaylistClick(final long id, final String name, final int position) {
            mPlaylistsProvider.loadQueue(id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((queue) -> {
                        if (isAdded()) {
                            onPlaylistLoaded(itemViewForPosition(position), name, queue);
                        }
                    });
        }

        @Override
        public void onPlaylistDeleteClick(final long id, final String name) {
            DeletePlaylistDialogFragment.show(getActivity(),
                    getFragmentManager(),
                    TAG_DIALOG_DELETE,
                    id,
                    name);
        }
    }

    private interface LoadPlaylistAction {

        List<Media> load();
    }
}
