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

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.doctoror.fuckoffmusicplayer.BaseActivity;
import com.doctoror.fuckoffmusicplayer.Henson;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.databinding.ActivityPlaylistBinding;
import com.doctoror.fuckoffmusicplayer.filemanager.DeleteFileDialogFragment;
import com.doctoror.fuckoffmusicplayer.filemanager.FileManagerService;
import com.doctoror.fuckoffmusicplayer.nowplaying.NowPlayingActivity;
import com.doctoror.fuckoffmusicplayer.transition.SlideAppBarAndRecyclerViewReturnTransition;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.parceler.Parcel;
import org.parceler.Parcels;

import android.Manifest;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;

import java.util.List;

import butterknife.OnClick;

/**
 * "Playlist" activity
 */
public final class PlaylistActivity extends BaseActivity implements
        DeleteFileDialogFragment.Callback {

    public static final String VIEW_ALBUM_ART = "VIEW_ALBUM_ART";

    private static final String EXTRA_STATE = "EXTRA_STATE";
    private static final String TAG_DIALOG_DELETE = "TAG_DIALOG_DELETE";

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

    @InjectExtra
    boolean hasCoverTransition;

    private ActivityPlaylistBinding mBinding;

    private boolean mFinishWhenDialogDismissed;
    private DeleteSession mDeleteSession;

    private String mCoverUri;
    private int mAppbarOffset;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dart.inject(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!hasCoverTransition) {
                // TODO transition not being called
                getWindow().setReturnTransition(new SlideAppBarAndRecyclerViewReturnTransition());
            }
        }

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
        mBinding.appBar.addOnOffsetChangedListener(
                (appBarLayout, verticalOffset) -> mAppbarOffset = verticalOffset);

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
        binding.albumArt.setColorFilter(ContextCompat.getColor(
                this, R.color.playlistAlbumArtBackground), PorterDuff.Mode.SRC_ATOP);

        String pic = null;
        final int size = playlist.size();
        for (int i = 0; i < size; i++) {
            final Media media = playlist.get(i);
            pic = media.albumArt;
            if (pic != null) {
                break;
            }
        }
        mCoverUri = pic;

        if (TextUtils.isEmpty(pic)) {
            Glide.clear(mBinding.albumArt);
            animateToPlaceholder();
            onImageSet();
        } else {
            final DrawableRequestBuilder<String> b = Glide.with(this).load(pic);
            if (hasCoverTransition) {
                supportPostponeEnterTransition();
                b.dontAnimate();
            }
            b.diskCacheStrategy(DiskCacheStrategy.NONE)
                    .dontTransform()
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(final Exception e, final String model,
                                final Target<GlideDrawable> target,
                                final boolean isFirstResource) {
                            mCoverUri = null;
                            animateToPlaceholder();
                            onImageSet();
                            return true;
                        }

                        @Override
                        public boolean onResourceReady(final GlideDrawable resource,
                                final String model,
                                final Target<GlideDrawable> target, final boolean isFromMemoryCache,
                                final boolean isFirstResource) {
                            onImageSet();
                            return false;
                        }
                    })
                    .into(binding.albumArt);
        }
    }

    private void animateToPlaceholder() {
        mBinding.albumArt.setAlpha(0f);
        mBinding.albumArt.setImageResource(R.drawable.album_art_placeholder);
        mBinding.albumArt.animate().alpha(1f).start();
    }

    private void onImageSet() {
        if (hasCoverTransition) {
            supportStartPostponedEnterTransition();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isNowPlayingPlaylist) {
            PlaylistHolder.getInstance(this).addObserver(mPlaylistObserver);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isNowPlayingPlaylist) {
            PlaylistHolder.getInstance(this).deleteObserver(mPlaylistObserver);
        }
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
                        if (!isFinishingAfterTransition()) {
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
        DeleteFileDialogFragment.show(media, getFragmentManager(), TAG_DIALOG_DELETE);
    }

    void onPlaylistEmpty() {
        mFinishWhenDialogDismissed = true;
        finishIfNeeded();
    }

    private void finishIfNeeded() {
        if (mFinishWhenDialogDismissed && mDeleteSession == null) {
            mFinishWhenDialogDismissed = false;
            ActivityCompat.finishAfterTransition(this);
        }
    }

    @OnClick(R.id.fab)
    public void onFabClick() {
        onPlayClick(playlist.get(0), 0);
    }

    private void onPlayClick(final Media media, final int index) {
        PlaylistUtils.play(this, playlist, media, index);

        final boolean shouldPassCoverView = mAppbarOffset == 0
                && TextUtils.equals(mCoverUri, media.getAlbumArt());
        NowPlayingActivity.start(this, shouldPassCoverView ? mBinding.albumArt : null, null);
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

    private final PlaylistHolder.PlaylistObserver mPlaylistObserver
            = new PlaylistHolder.PlaylistObserver() {

        @Override
        public void onPlaylistChanged(@Nullable final List<Media> playlist) {
            if (isNowPlayingPlaylist) {
                final Intent intent = Henson.with(PlaylistActivity.this)
                        .gotoPlaylistActivity()
                        .hasCoverTransition(false)
                        .isNowPlayingPlaylist(true)
                        .playlist(playlist)
                        .build();
                restart(intent);
            }
        }

        @Override
        public void onPositionChanged(final long position) {

        }

        @Override
        public void onMediaChanged(final Media media) {

        }

        @Override
        public void onMediaRemoved(final Media media) {

        }
    };
}
