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
package com.doctoror.fuckoffmusicplayer.playlist;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.doctoror.fuckoffmusicplayer.BaseActivity;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.databinding.ActivityPlaylistBinding;
import com.doctoror.fuckoffmusicplayer.filemanager.DeleteFileDialogFragment;
import com.doctoror.fuckoffmusicplayer.filemanager.FileManagerService;
import com.doctoror.fuckoffmusicplayer.util.TransitionListenerAdapter;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.parceler.Parcel;
import org.parceler.Parcels;

import android.Manifest;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.transition.Transition;
import android.view.View;

import java.util.List;

import butterknife.OnClick;

/**
 * Created by Yaroslav Mytkalyk on 20.10.16.
 */

public final class PlaylistActivity extends BaseActivity implements
        DeleteFileDialogFragment.Callback {

    public static final String VIEW_ALBUM_ART = "VIEW_ALBUM_ART";

    private static final String EXTRA_STATE = "EXTRA_STATE";
    private static final String TAG_DIALOG_DELETE = "TAG_DIALOG_DELETE";

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final PlaylistActivityModel mModel = new PlaylistActivityModel();
    private PlaylistRecyclerAdapter mAdapter;

    private PlaylistHolder mPlaylistHolder;

    @InjectExtra
    List<Media> playlist;

    @InjectExtra
    Boolean isNowPlayingPlaylist;

    @Nullable
    @InjectExtra
    String title;

    private ActivityPlaylistBinding mBinding;

    private boolean mFinishWhenDialogDismissed;
    private DeleteSession mDeleteSession;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dart.inject(this);
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }

        mPlaylistHolder = PlaylistHolder.getInstance(this);

        mAdapter = new PlaylistRecyclerAdapter(this, playlist);
        mAdapter.setOnTrackClickListener(mOnTrackClickListener);
        mAdapter.registerAdapterDataObserver(mAdapterDataObserver);
        mModel.setRecyclerAdpter(mAdapter);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_playlist);
        mBinding.setModel(mModel);
        initAlbumArtAndToolbar(mBinding);
        initRecyclerView(mBinding);

        mFinishWhenDialogDismissed = false;
        if (savedInstanceState != null) {
            final State state = Parcels.unwrap(savedInstanceState.getParcelable(EXTRA_STATE));
            mDeleteSession = state.deleteSession;
            mFinishWhenDialogDismissed = state.finishWhenDialogDismissed;

            if (mDeleteSession != null && mDeleteSession.permissionRequested) {
                onDeleteClick(mDeleteSession.media);
            }
        }
    }

    private void initRecyclerView(@NonNull final ActivityPlaylistBinding binding) {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelperImpl(
                (PlaylistRecyclerAdapter) mModel.getRecyclerAdapter().get()));
        itemTouchHelper.attachToRecyclerView(binding.recyclerView);
    }

    private void initAlbumArtAndToolbar(@NonNull final ActivityPlaylistBinding binding) {
        setSupportActionBar(binding.toolbar);
        ViewCompat.setTransitionName(binding.albumArt, PlaylistActivity.VIEW_ALBUM_ART);

        String pic = null;
        final int size = playlist.size();
        for (int i = 0; i < size; i++) {
            final Media media = playlist.get(i);
            pic = media.albumArt;
            if (pic != null) {
                break;
            }
        }

        listenForTransitionEnd();
        if (!TextUtils.isEmpty(pic)) {
            supportPostponeEnterTransition();
            Glide.with(this)
                    .load(pic)
                    .dontTransform()
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.album_art_placeholder)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(final Exception e, final String model,
                                final Target<GlideDrawable> target,
                                final boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(final GlideDrawable resource,
                                final String model,
                                final Target<GlideDrawable> target, final boolean isFromMemoryCache,
                                final boolean isFirstResource) {
                            mHandler.post(() -> supportStartPostponedEnterTransition());
                            return false;
                        }
                    })
                    .into(binding.albumArt);
        }
    }

    private void listenForTransitionEnd() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Transition transition = getWindow().getEnterTransition();
            if (transition != null) {
                transition.addListener(new TransitionListenerAdapter() {
                    @Override
                    public void onTransitionEnd(final Transition transition) {
                        onTransitionEnded();
                    }
                });
            } else {
                onTransitionEnded();
            }
        } else {
            onTransitionEnded();
        }
    }

    private void onTransitionEnded() {
        mBinding.albumArtForeground.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        final State state = new State();
        state.deleteSession = mDeleteSession;
        state.finishWhenDialogDismissed = mFinishWhenDialogDismissed;
        outState.putParcelable(EXTRA_STATE, Parcels.wrap(state));
    }

    @Override
    public void setSupportActionBar(@Nullable final Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE
                    | ActionBar.DISPLAY_SHOW_HOME
                    | ActionBar.DISPLAY_HOME_AS_UP);
        }
    }

    @Override
    public void onDeleteClick(@NonNull final Media media) {
        if (mDeleteSession == null) {
            mDeleteSession = new DeleteSession(media);
        }
        mDeleteSession.permissionRequested = true;
        RxPermissions.getInstance(this).request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(result -> {
                    if (result) {
                        if (!isFinishing()) {
                            FileManagerService.delete(getApplicationContext(), media);
                        }
                    }
                    mDeleteSession = null;
                    finishIfNeeded();
                });
    }

    @Override
    public void onDeleteCancel() {
        mDeleteSession = null;
    }

    @Override
    public void onDeleteDialogDismiss() {
        finishIfNeeded();
    }

    void onDeleteClickFromList(@NonNull final Media media) {
        mDeleteSession = new DeleteSession(media);
        DeleteFileDialogFragment.show(this, media, getFragmentManager(), TAG_DIALOG_DELETE);
    }

    void onPlaylistEmpty() {
        mFinishWhenDialogDismissed = true;
        finishIfNeeded();
    }

    private void finishIfNeeded() {
        if (mFinishWhenDialogDismissed && mDeleteSession == null) {
            mFinishWhenDialogDismissed = false;
            finish();
        }
    }

    @OnClick(R.id.fab)
    public void onFabClick() {
        onPlayClick(playlist.get(0), 0);
    }

    private void onPlayClick(final Media media, final int index) {
        PlaylistUtils.play(this, playlist, media, index);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.unregisterAdapterDataObserver(mAdapterDataObserver);
    }

    @Parcel
    static final class State {

        boolean finishWhenDialogDismissed;
        DeleteSession deleteSession;
    }

    @Parcel
    static final class DeleteSession {

        Media media;
        boolean permissionRequested;

        DeleteSession() {
        }

        DeleteSession(final Media media) {
            this.media = media;
        }
    }

    private final PlaylistRecyclerAdapter.OnTrackClickListener mOnTrackClickListener
            = new PlaylistRecyclerAdapter.OnTrackClickListener() {

        @Override
        public void onTrackClick(@NonNull final Media media, final int position) {
            onPlayClick(media, position);
        }

        @Override
        public void onTrackDeleteClick(@NonNull final Media media) {
            onDeleteClickFromList(media);
        }
    };

    private final class ItemTouchHelperImpl extends ItemTouchHelper.SimpleCallback {

        @NonNull
        private final PlaylistRecyclerAdapter mAdapter;

        ItemTouchHelperImpl(@NonNull final PlaylistRecyclerAdapter adapter) {
            super(0, ItemTouchHelper.LEFT);
            mAdapter = adapter;
        }

        @Override
        public boolean onMove(final RecyclerView recyclerView,
                final RecyclerView.ViewHolder viewHolder,
                final RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public int getMovementFlags(final RecyclerView recyclerView,
                final RecyclerView.ViewHolder viewHolder) {
            final int position = viewHolder.getAdapterPosition();
            return mAdapter.canRemove(position) ?
                    super.getMovementFlags(recyclerView, viewHolder) : 0;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int swipeDir) {
            final int pos = viewHolder.getAdapterPosition();
            if (isNowPlayingPlaylist) {
                final Object item = mAdapter.getItem(pos);
                if (item instanceof Media) {
                    mPlaylistHolder.remove((Media) item);
                }
            }
            mAdapter.setItemRemoved(pos);
        }
    }

    private final RecyclerView.AdapterDataObserver mAdapterDataObserver
            = new RecyclerView.AdapterDataObserver() {

        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(final int positionStart, final int itemCount) {
            checkIfEmpty();
        }

        private void checkIfEmpty() {
            if (mAdapter.getItemCount() == 0 && !isFinishing()) {
                onPlaylistEmpty();
            }
        }
    };
}
