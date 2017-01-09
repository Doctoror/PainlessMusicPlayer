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
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.filemanager.DeleteFileDialogFragment;
import com.doctoror.fuckoffmusicplayer.filemanager.FileManagerService;
import com.doctoror.fuckoffmusicplayer.nowplaying.NowPlayingActivity;
import com.doctoror.fuckoffmusicplayer.playback.data.PlaybackData;
import com.doctoror.fuckoffmusicplayer.transition.CardVerticalGateTransition;
import com.doctoror.fuckoffmusicplayer.transition.SlideFromBottomHelper;
import com.doctoror.fuckoffmusicplayer.transition.TransitionUtils;
import com.doctoror.fuckoffmusicplayer.transition.VerticalGateTransition;
import com.doctoror.fuckoffmusicplayer.util.CollectionUtils;
import com.doctoror.fuckoffmusicplayer.util.ViewUtils;
import com.doctoror.fuckoffmusicplayer.util.CoordinatorLayoutUtil;
import com.doctoror.fuckoffmusicplayer.transition.TransitionListenerAdapter;
import com.doctoror.fuckoffmusicplayer.widget.DisableableAppBarLayout;
import com.doctoror.fuckoffmusicplayer.widget.ItemTouchHelperViewHolder;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.parceler.Parcel;
import org.parceler.Parcels;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.res.Configuration;
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
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.transition.Transition;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * "Playlist" activity
 */
public final class PlaylistActivity extends BaseActivity implements
        DeleteFileDialogFragment.Callback {

    public static final String TRANSITION_NAME_ALBUM_ART
            = "PlaylistActivity.TRANSITION_NAME_ALBUM_ART";

    public static final String TRANSITION_NAME_ROOT = "PlaylistActivity.TRANSITION_NAME_ROOT";

    private static final String EXTRA_STATE = "EXTRA_STATE";
    private static final String TAG_DIALOG_DELETE = "TAG_DIALOG_DELETE";

    private final PlaylistActivityModel mModel = new PlaylistActivityModel();
    private PlaylistRecyclerAdapter mAdapter;
    private CoordinatorLayoutUtil.AnchorParams mFabAnchorParams;

    private RxPermissions mRxPermissions;

    private int mShortAnimTime;
    private int mMediumAnimTime;

    @InjectExtra
    List<Media> playlist;

    @InjectExtra
    boolean isNowPlayingPlaylist;

    @Nullable
    @InjectExtra
    String title;

    @InjectExtra
    boolean hasCoverTransition;

    @InjectExtra
    boolean hasItemViewTransition;

    @BindView(R.id.root)
    View root;

    @BindView(R.id.appBar)
    DisableableAppBarLayout appBar;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.albumArt)
    ImageView albumArt;

    @BindView(R.id.albumArtDim)
    View albumArtDim;

    @BindView(R.id.fab)
    View fab;

    @Nullable
    @BindView(R.id.cardHostScrollView)
    View cardHostScrollView;

    @Nullable
    @BindView(R.id.cardView)
    CardView cardView;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private boolean mFinishWhenDialogDismissed;
    private DeleteSession mDeleteSession;

    private String mCoverUri;
    private int mAppbarOffset;

    private boolean mCreatedWithInstanceState;

    @Inject
    PlaybackData mPlaybackData;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dart.inject(this);
        DaggerHolder.getInstance(this).mainComponent().inject(this);

        mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mMediumAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }

        mAdapter = new PlaylistRecyclerAdapter(this, playlist);
        mAdapter.setTrackListener(mTrackListener);
        mAdapter.registerAdapterDataObserver(mAdapterDataObserver);
        mModel.setRecyclerAdpter(mAdapter);

        final com.doctoror.fuckoffmusicplayer.databinding.ActivityPlaylistBinding binding
                = DataBindingUtil.setContentView(this, R.layout.activity_playlist);
        binding.setModel(mModel);

        ButterKnife.bind(this);

        appBar.addOnOffsetChangedListener(
                (appBarLayout, verticalOffset) -> mAppbarOffset = verticalOffset);

        initAlbumArtAndToolbar(binding);
        initRecyclerView();

        mFinishWhenDialogDismissed = false;

        if (TransitionUtils.supportsActivityTransitions()) {
            PlaylistActivityLollipop.applyTransitions(this, cardView != null);
        }

        mCreatedWithInstanceState = savedInstanceState != null;
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            final State state = Parcels.unwrap(savedInstanceState.getParcelable(EXTRA_STATE));
            mDeleteSession = state.deleteSession;
            mFinishWhenDialogDismissed = state.finishWhenDialogDismissed;
            mFabAnchorParams = state.fabAnchorParams;
            playlist = state.playlist;
            mAdapter.setItems(playlist);

            fab.setScaleX(1f);
            fab.setScaleY(1f);

            if (mDeleteSession != null && mDeleteSession.permissionRequested) {
                onDeleteClick(mDeleteSession.media);
            }
        }
    }

    @Override
    protected void onPostCreate(@Nullable final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (TransitionUtils.supportsActivityTransitions()) {
            PlaylistActivityLollipop.addEnterTransitionListener(this);
        }
    }

    private void setAppBarCollapsibleIfNeeded() {
        ViewUtils.setAppBarCollapsibleIfScrollableViewIsLargeEnoughToScroll(
                root, appBar, recyclerView, ViewUtils.getOverlayTop(cardHostScrollView != null
                        ? cardHostScrollView : recyclerView));
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public void onLayoutChildren(final RecyclerView.Recycler recycler,
                    final RecyclerView.State state) {
                super.onLayoutChildren(recycler, state);
                setAppBarCollapsibleIfNeeded();
            }
        });
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelperImpl(
                (PlaylistRecyclerAdapter) mModel.getRecyclerAdapter().get()));
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void initAlbumArtAndToolbar(
            @NonNull final com.doctoror.fuckoffmusicplayer.databinding.ActivityPlaylistBinding binding) {
        setSupportActionBar(toolbar);
        ViewCompat.setTransitionName(binding.getRoot(), PlaylistActivity.TRANSITION_NAME_ROOT);
        ViewCompat.setTransitionName(albumArt, PlaylistActivity.TRANSITION_NAME_ALBUM_ART);

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
            Glide.clear(albumArt);
            animateToPlaceholder();
            onImageSet();
        } else {
            final DrawableRequestBuilder<String> b = Glide.with(this).load(pic);
            if (hasCoverTransition || hasItemViewTransition) {
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
                    .into(albumArt);
        }
    }

    private void animateToPlaceholder() {
        albumArt.setAlpha(0f);
        albumArt.setImageResource(R.drawable.album_art_placeholder);
        albumArt.animate().alpha(1f).start();
    }

    private void onImageSet() {
        if (hasCoverTransition || hasItemViewTransition) {
            supportStartPostponedEnterTransition();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCreatedWithInstanceState
                || !TransitionUtils.supportsActivityTransitions()
                || (!hasItemViewTransition && !hasCoverTransition)) {
            onEnterTransitionFinished();
        }
        if (mFabAnchorParams != null) {
            CoordinatorLayoutUtil.applyAnchorParams(fab, mFabAnchorParams);
            fab.post(() -> fab.requestLayout());
            mFabAnchorParams = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        fab.setScaleX(1f);
        fab.setScaleY(1f);
        albumArtDim.setAlpha(1f);
        albumArt.clearColorFilter();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        final State state = new State();
        state.playlist = playlist;
        state.deleteSession = mDeleteSession;
        state.finishWhenDialogDismissed = mFinishWhenDialogDismissed;
        state.fabAnchorParams = mFabAnchorParams;
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

    @NonNull
    private RxPermissions getRxPermissions() {
        if (mRxPermissions == null) {
            mRxPermissions = new RxPermissions(this);
        }
        return mRxPermissions;
    }

    @Override
    public void onDeleteClick(@NonNull final Media media) {
        if (mDeleteSession == null) {
            mDeleteSession = new DeleteSession(media);
        }
        mDeleteSession.permissionRequested = true;
        getRxPermissions().request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(result -> {
                    if (result) {
                        if (!isFinishingAfterTransition()) {
                            mAdapter.removeItem(media);
                            playlist.remove(media);
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
    public void onFabClick(@NonNull final View view) {
        onPlayClick(view, 0);
    }

    private void onPlayClick(@NonNull final View clickedView,
            final int playlistPosition) {

        PlaylistUtils.play(this, mPlaybackData, playlist, playlistPosition);
        final Media media = CollectionUtils.getItemSafe(playlist, playlistPosition);
        final boolean shouldPassCoverView = mAppbarOffset == 0
                && TextUtils.equals(mCoverUri, media != null ? media.getAlbumArt() : null);
        if (shouldPassCoverView) {
            prepareViewsAndExit(() -> NowPlayingActivity.start(this, albumArt, null));
        } else {
            mFabAnchorParams = CoordinatorLayoutUtil.getAnchorParams(fab);
            CoordinatorLayoutUtil.clearAnchorGravityAndApplyMargins(fab);
            NowPlayingActivity.start(this, null, clickedView);
        }
    }

    private void onEnterTransitionFinished() {
        if (fab.getScaleX() != 1f) {
            fab.animate().scaleX(1f).scaleY(1f).setDuration(mShortAnimTime).start();
        }
        if (albumArtDim.getAlpha() != 1f) {
            albumArtDim.animate().alpha(1f).setDuration(mShortAnimTime).start();
        }
        if (cardView != null && cardView.getVisibility() != View.VISIBLE) {
            if (TransitionUtils.supportsActivityTransitions() && hasCoverTransition) {
                cardView.setTranslationY(SlideFromBottomHelper.getStartTranslation(cardView));
                cardView.setVisibility(View.VISIBLE);
                SlideFromBottomHelper.createAnimator(cardView).setDuration(mMediumAnimTime).start();
            } else {
                cardView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void prepareViewsAndExit(@NonNull final Runnable exitAction) {
        if (!TransitionUtils.supportsActivityTransitions() ||
                (fab.getScaleX() == 0f && albumArtDim.getAlpha() == 0f)) {
            exitAction.run();
        } else {
            final boolean isLandscape = getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_LANDSCAPE;
            // Landscape Now Playing has a dim, so dim the ImageView and send it
            if (isLandscape) {
                albumArtDim.setAlpha(0f);
                albumArt.setColorFilter(
                        ContextCompat.getColor(this, R.color.translucentBackground),
                        PorterDuff.Mode.SRC_ATOP);
            } else {
                // Portrait NowPlaying does not have a dim. Fade out the dim before animating.
                albumArtDim.animate().alpha(0f).setDuration(mShortAnimTime).start();
            }
            fab.animate().scaleX(0f).scaleY(0f).setDuration(mShortAnimTime)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(final Animator animation) {
                            exitAction.run();
                        }
                    }).start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.unregisterAdapterDataObserver(mAdapterDataObserver);
    }

    @Parcel
    static final class State {

        List<Media> playlist;
        boolean finishWhenDialogDismissed;
        DeleteSession deleteSession;
        CoordinatorLayoutUtil.AnchorParams fabAnchorParams;
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

    private final PlaylistRecyclerAdapter.TrackListener mTrackListener
            = new PlaylistRecyclerAdapter.TrackListener() {

        @Override
        public void onTrackClick(@NonNull final View itemView,
                final int position) {
            onPlayClick(itemView, position);
        }

        @Override
        public void onTrackDeleteClick(@NonNull final Media item) {
            mDeleteSession = new DeleteSession(item);
            DeleteFileDialogFragment.show(item, getFragmentManager(), TAG_DIALOG_DELETE);
        }

        @Override
        public void onTracksSwapped(final int i, final int j) {
            if (i < playlist.size() && j < playlist.size()) {
                Collections.swap(playlist, i, j);
            }
            if (isNowPlayingPlaylist) {
                mPlaybackData.setPlaylist(playlist);
            }
        }
    };

    private final class ItemTouchHelperImpl extends ItemTouchHelper.SimpleCallback {

        @NonNull
        private final PlaylistRecyclerAdapter mAdapter;

        ItemTouchHelperImpl(@NonNull final PlaylistRecyclerAdapter adapter) {
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT);
            mAdapter = adapter;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public boolean onMove(final RecyclerView recyclerView,
                final RecyclerView.ViewHolder source,
                final RecyclerView.ViewHolder target) {
            //noinspection SimplifiableIfStatement
            if (source.getItemViewType() != target.getItemViewType()) {
                return false;
            }

            return mAdapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
        }

        @Override
        public int getMovementFlags(final RecyclerView recyclerView,
                final RecyclerView.ViewHolder viewHolder) {
            final int swipeFlags = ItemTouchHelper.LEFT;
            int dragFlags = 0;
            if (mAdapter.getItemCount() > 1) {
                dragFlags |= ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            }
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int swipeDir) {
            final int pos = viewHolder.getAdapterPosition();
            playlist.remove(pos);
            if (isNowPlayingPlaylist) {
                mPlaybackData.setPlaylist(playlist);
            }
            mAdapter.removeItem(pos);
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                if (viewHolder instanceof ItemTouchHelperViewHolder) {
                    ((ItemTouchHelperViewHolder) viewHolder).onItemSelected();
                }
            }

            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            if (viewHolder instanceof ItemTouchHelperViewHolder) {
                ((ItemTouchHelperViewHolder) viewHolder).onItemClear();
            }
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static final class PlaylistActivityLollipop {

        static void applyTransitions(@NonNull final BaseActivity activity,
                final boolean hasCardView) {
            TransitionUtils.clearSharedElementsOnReturn(activity);
            final Window window = activity.getWindow();
            window.setReturnTransition(hasCardView
                    ? new CardVerticalGateTransition()
                    : new VerticalGateTransition());
        }

        static void addEnterTransitionListener(@NonNull final PlaylistActivity playlistActivity) {
            final Transition enter = playlistActivity.getWindow().getSharedElementEnterTransition();
            if (enter != null) {
                enter.addListener(new TransitionListenerAdapter() {
                    @Override
                    public void onTransitionEnd(final Transition transition) {
                        playlistActivity.onEnterTransitionFinished();
                    }
                });
            }
        }
    }
}
