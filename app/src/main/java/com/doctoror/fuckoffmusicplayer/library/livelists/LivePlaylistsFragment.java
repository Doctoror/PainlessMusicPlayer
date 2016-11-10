package com.doctoror.fuckoffmusicplayer.library.livelists;

import com.doctoror.fuckoffmusicplayer.Henson;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.playlist.Media;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Yaroslav Mytkalyk on 08.11.16.
 */

public final class LivePlaylistsFragment extends Fragment {

    private final List<LivePlaylist> mPlaylists = new ArrayList<>(2);

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private Subscription mLoadPlaylistSubscription;
    private ProgressDialog mProgressDialog;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlaylists.add(new LivePlaylistRecent50(getResources()));
        mPlaylists.add(new LivePlaylistRandom50(getResources()));
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_live_playlists, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final LivePlaylistsRecyclerAdapter adapter = new LivePlaylistsRecyclerAdapter(
                getActivity(), mPlaylists);
        adapter.setOnPlaylistClickListener(this::loadPlaylist);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        unsubscribeFromPlaylistLoad();
        dismissProgressDialog();
    }

    private void loadPlaylist(@NonNull final LivePlaylist livePlaylist) {
        unsubscribeFromPlaylistLoad();
        dismissProgressDialog();

        final Context context = getActivity();
        if (context == null) {
            return;
        }

        mProgressDialog = ProgressDialog
                .show(context, null, context.getText(R.string.Loading_playlist), true, true,
                        dialog -> {
                            unsubscribeFromPlaylistLoad();
                            mProgressDialog = null;
                        });

        mLoadPlaylistSubscription = Observable.<List<Media>>create(
                s -> s.onNext(livePlaylist.create(context)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Media>>() {

                    @Override
                    public void onCompleted() {
                        if (isAdded()) {
                            dismissProgressDialog();
                        }
                    }

                    @Override
                    public void onError(final Throwable e) {
                        if (isAdded()) {
                            dismissProgressDialog();
                            Toast.makeText(getActivity(),
                                    getString(R.string.Failed_to_load_data_s, e.getMessage()),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onNext(final List<Media> medias) {
                        if (isAdded()) {
                            dismissProgressDialog();
                            onPlaylistLoaded(medias);
                        }
                    }
                });
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void unsubscribeFromPlaylistLoad() {
        if (mLoadPlaylistSubscription != null) {
            mLoadPlaylistSubscription.unsubscribe();
            mLoadPlaylistSubscription = null;
        }
    }

    private void onPlaylistLoaded(@NonNull final List<Media> playlist) {
        final Activity activity = getActivity();
        if (playlist.isEmpty()) {
            Toast.makeText(activity, R.string.No_tracks_found, Toast.LENGTH_LONG).show();
        } else {
            final Intent intent = Henson.with(activity)
                    .gotoPlaylistActivity()
                    .hasCoverTransition(false)
                    .isNowPlayingPlaylist(false)
                    .playlist(playlist)
                    .build();
            startActivity(intent);
        }
    }
}
