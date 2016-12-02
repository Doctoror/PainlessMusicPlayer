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
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistActivity;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistUtils;
import com.doctoror.fuckoffmusicplayer.util.StringUtils;
import com.doctoror.rxcursorloader.RxCursorLoader;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
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
 * Created by Yaroslav Mytkalyk on 25.10.16.
 */

public class ConditionalAlbumListFragment extends Fragment {

    @NonNull
    public static ConditionalAlbumListFragment instantiate(@NonNull final Context context,
            @NonNull final String title,
            @NonNull final RxCursorLoader.Query params) {
        final ConditionalAlbumListFragment fragment = new ConditionalAlbumListFragment();
        final Bundle extras = Henson.with(context).gotoConditionalAlbumListFragment()
                .loaderParams(params)
                .title(title)
                .build()
                .getExtras();
        fragment.setArguments(extras);
        return fragment;
    }

    private final ConditionalAlbumListModel mModel = new ConditionalAlbumListModel();
    private FragmentConditionalAlbumListBinding mBinding;

    private ConditionalAlbumsRecyclerAdapter mAdapter;

    private RxCursorLoader mLoaderObservable;
    private Subscription mSubscription;

    private RequestManager mRequestManager;
    private Cursor mData;

    private String mHeaderArtUri;

    @InjectExtra
    String title;

    @InjectExtra
    RxCursorLoader.Query loaderParams;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dart.inject(this, getArguments());
        mRequestManager = Glide.with(this);

        mAdapter = new ConditionalAlbumsRecyclerAdapter(getActivity(), mRequestManager);
        mAdapter.setOnAlbumClickListener((artView, id, album, art) ->
                onPlayClick(artView, album, new long[]{id}, new String[]{art}));
        mModel.setRecyclerAdpter(mAdapter);
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

        binding.image.setColorFilter(ContextCompat.getColor(
                activity, R.color.translucentBackground), PorterDuff.Mode.SRC_ATOP);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.setModel(mModel);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
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

    @Nullable
    @WorkerThread
    protected List<Media> playlistFromAlbums(@NonNull final long[] albumIds,
            @NonNull final String[] arts) {
        return PlaylistUtils.fromAlbums(getActivity().getContentResolver(), albumIds, arts, null);
    }

    private void onPlayClick(@Nullable final View albumArtView,
            @Nullable final String playlistName,
            @NonNull final long[] albumIds,
            @NonNull final String[] arts) {
        Observable.<List<Media>>create(s -> s.onNext(playlistFromAlbums(albumIds, arts)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((playlist) -> {
                    if (isAdded()) {
                        final Activity activity = getActivity();
                        if (playlist != null && !playlist.isEmpty()) {
                            final Intent intent = Henson.with(activity).gotoPlaylistActivity()
                                    .hasCoverTransition(true)
                                    .hasItemViewTransition(false)
                                    .isNowPlayingPlaylist(false)
                                    .playlist(playlist)
                                    .title(playlistName)
                                    .build();

                            if (albumArtView != null && !isTheSameArtOnNextScreen(arts)) {
                                //noinspection unchecked
                                final ActivityOptionsCompat options = ActivityOptionsCompat
                                        .makeSceneTransitionAnimation(activity, albumArtView,
                                                PlaylistActivity.TRANSITION_NAME_ALBUM_ART);

                                startActivity(intent, options.toBundle());
                            } else {
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(activity, R.string.The_playlist_is_empty,
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }

    private boolean isTheSameArtOnNextScreen(@NonNull final String[] arts) {
        if (arts.length == 0) {
            return TextUtils.isEmpty(mHeaderArtUri);
        }
        final String art = StringUtils.firstNonEmptyString(arts);
        return art == null && mHeaderArtUri == null || TextUtils.equals(art, mHeaderArtUri);
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
            final String[] arts = new String[mData.getCount()];
            int i = 0;
            for (mData.moveToFirst(); !mData.isAfterLast(); mData.moveToNext(), i++) {
                ids[i] = mData.getLong(ConditionalAlbumListQuery.COLUMN_ID);
                arts[i] = mData.getString(ConditionalAlbumListQuery.COLUMN_ALBUM_ART);
            }
            onPlayClick(null, null, ids, arts);
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
                mHeaderArtUri = pic;
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
