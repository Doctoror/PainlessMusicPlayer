/*
 * Copyright (C) 2016 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.library.albums.conditional;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.doctoror.fuckoffmusicplayer.Henson;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.databinding.FragmentConditionalAlbumListBinding;
import com.doctoror.fuckoffmusicplayer.nowplaying.NowPlayingActivity;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistActivity;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistFactory;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistUtils;
import com.doctoror.fuckoffmusicplayer.util.ToolbarUtils;
import com.doctoror.rxcursorloader.RxCursorLoader;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Album lists fragment
 */
public class ConditionalAlbumListFragment extends Fragment {

    @NonNull
    public static ConditionalAlbumListFragment instantiate(@NonNull final Context context,
            @NonNull final RxCursorLoader.Query params) {
        final ConditionalAlbumListFragment fragment = new ConditionalAlbumListFragment();
        final Bundle extras = Henson.with(context).gotoConditionalAlbumListFragment()
                .loaderParams(params)
                .build()
                .getExtras();
        fragment.setArguments(extras);
        return fragment;
    }

    private final ConditionalAlbumListModel mModel = new ConditionalAlbumListModel();
    private FragmentConditionalAlbumListBinding mBinding;
    private View mToolbarTitle;

    private ConditionalAlbumsRecyclerAdapter mAdapter;

    private RxCursorLoader mLoaderObservable;
    private Subscription mSubscription;

    private RequestManager mRequestManager;
    private Cursor mData;

    private int mAnimTime;

    @InjectExtra
    RxCursorLoader.Query loaderParams;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dart.inject(this, getArguments());
        mRequestManager = Glide.with(this);

        mAdapter = new ConditionalAlbumsRecyclerAdapter(getActivity(), mRequestManager);
        mAdapter.setOnAlbumClickListener(this::onListItemClick);
        mModel.setRecyclerAdpter(mAdapter);

        mAnimTime = getResources().getInteger(R.integer.short_anim_time);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        final FragmentConditionalAlbumListBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_conditional_album_list, container, false);
        mBinding = binding;

        final AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(mBinding.toolbar);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.setModel(mModel);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mToolbarTitle = ToolbarUtils.getTitleTextView(mBinding.toolbar);
        restartLoader();
        ((AppCompatActivity) getActivity()).supportStartPostponedEnterTransition();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mSubscription != null) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mBinding.fab.setScaleX(1f);
        mBinding.fab.setScaleY(1f);
        mBinding.albumArtDim.setAlpha(1f);
        if (mToolbarTitle != null) {
            mToolbarTitle.setAlpha(1f);
        }
    }

    @Nullable
    @WorkerThread
    protected List<Media> playlistFromAlbum(final long albumId) {
        return PlaylistFactory.fromAlbum(getActivity().getContentResolver(), albumId);
    }

    @Nullable
    @WorkerThread
    protected List<Media> playlistFromAlbums(@NonNull final long[] albumIds) {
        return PlaylistFactory.fromAlbums(getActivity().getContentResolver(), albumIds, null);
    }

    private void onListItemClick(@NonNull final View itemView,
            final long albumId,
            @Nullable final String playlistName) {
        Observable.<List<Media>>create(s -> s.onNext(playlistFromAlbum(albumId)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((playlist) -> {
                    if (isAdded()) {
                        final Activity activity = getActivity();
                        if (playlist != null && !playlist.isEmpty()) {
                            final Intent intent = Henson.with(activity).gotoPlaylistActivity()
                                    .hasCoverTransition(false)
                                    .hasItemViewTransition(false)
                                    .isNowPlayingPlaylist(false)
                                    .playlist(playlist)
                                    .title(playlistName)
                                    .build();

                            final ActivityOptionsCompat options = ActivityOptionsCompat
                                    .makeSceneTransitionAnimation(activity, itemView,
                                            PlaylistActivity.TRANSITION_NAME_ROOT);

                            startActivity(intent, options.toBundle());
                        } else {
                            Toast.makeText(activity, R.string.The_playlist_is_empty,
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }

    private void onPlayClick(@NonNull final long[] albumIds) {
        Observable.<List<Media>>create(s -> s.onNext(playlistFromAlbums(albumIds)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((playlist) -> {
                    if (isAdded()) {
                        final Activity activity = getActivity();
                        if (playlist != null && !playlist.isEmpty()) {
                            PlaylistUtils.play(activity, playlist);
                            prepareViewsAndExit(() -> NowPlayingActivity.start(getActivity(),
                                    mBinding.image, null), true);
                        } else {
                            Toast.makeText(activity, R.string.The_playlist_is_empty,
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }

    private void prepareViewsAndExit(@NonNull final Runnable exitAction,
            final boolean fadeDim) {
        if (mBinding.fab.getScaleX() == 0f) {
            exitAction.run();
        } else {
            if (mToolbarTitle != null) {
                mToolbarTitle.animate().alpha(0f).setDuration(mAnimTime).start();
            }
            if (fadeDim) {
                mBinding.albumArtDim.animate().alpha(0f).setDuration(mAnimTime).start();
            }
            mBinding.fab.animate().scaleX(0f).scaleY(0f).setDuration(mAnimTime)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(final Animator animation) {
                            exitAction.run();
                        }
                    }).start();
        }
    }

    private void restartLoader() {
        final RxCursorLoader.Query params = loaderParams;
        if (mLoaderObservable == null) {
            mLoaderObservable = RxCursorLoader
                    .create(getActivity().getContentResolver(), params);
            mLoaderObservable.subscribe(mObserver);
        } else {
            mLoaderObservable.reloadWithNewQuery(params);
        }
    }

    private void showStateError() {
        mBinding.fab.setVisibility(View.GONE);
        mBinding.progress.setVisibility(View.GONE);
        mBinding.recyclerView.setVisibility(View.GONE);
        mBinding.errorContainer.setVisibility(View.VISIBLE);
    }

    private void showStateContent() {
        mBinding.fab.setVisibility(View.VISIBLE);
        mBinding.progress.setVisibility(View.GONE);
        mBinding.recyclerView.setVisibility(View.VISIBLE);
        mBinding.errorContainer.setVisibility(View.GONE);
    }

    @OnClick(R.id.fab)
    public void onFabClick() {
        if (mData != null) {
            final long[] ids = new long[mData.getCount()];
            int i = 0;
            for (mData.moveToFirst(); !mData.isAfterLast(); mData.moveToNext(), i++) {
                ids[i] = mData.getLong(ConditionalAlbumListQuery.COLUMN_ID);
            }
            onPlayClick(ids);
        }
    }

    private final Observer<Cursor> mObserver = new Observer<Cursor>() {

        @Override
        public void onCompleted() {
            mData = null;
        }

        @Override
        public void onError(final Throwable e) {
            mData = null;
            if (isAdded()) {
                mModel.setErrorText(getString(R.string.Failed_to_load_data_s, e));
                showStateError();
            }
        }

        @Override
        public void onNext(final Cursor cursor) {
            if (mBinding != null) {
                String pic = null;
                if (cursor != null) {
                    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                        pic = cursor.getString(ConditionalAlbumListQuery.COLUMN_ALBUM_ART);
                        if (pic != null) {
                            break;
                        }
                    }
                }
                if (TextUtils.isEmpty(pic)) {
                    Glide.clear(mBinding.image);
                    //Must be a delay of from here. TODO Why?
                    Observable.timer(300, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread()).subscribe((l) ->
                            animateToPlaceholder());
                } else {
                    mRequestManager.load(pic)
                            .dontTransform()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .listener(new RequestListener<String, GlideDrawable>() {
                                @Override
                                public boolean onException(final Exception e, final String model,
                                        final Target<GlideDrawable> target,
                                        final boolean isFirstResource) {
                                    animateToPlaceholder();
                                    return true;
                                }

                                @Override
                                public boolean onResourceReady(final GlideDrawable resource,
                                        final String model,
                                        final Target<GlideDrawable> target,
                                        final boolean isFromMemoryCache,
                                        final boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .into(mBinding.image);
                }
            }
            mAdapter.swapCursor(cursor);
            mData = cursor;
            showStateContent();
        }

        private void animateToPlaceholder() {
            mBinding.image.setAlpha(0f);
            mBinding.image.setImageResource(R.drawable.album_art_placeholder);
            mBinding.image.animate().alpha(1f).setDuration(500).start();
        }
    };
}
