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
package com.doctoror.fuckoffmusicplayer.nowplaying;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.doctoror.commons.util.Log;
import com.doctoror.commons.util.StringUtils;
import com.doctoror.fuckoffmusicplayer.BaseActivity;
import com.doctoror.fuckoffmusicplayer.Henson;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.databinding.ActivityNowplayingBinding;
import com.doctoror.fuckoffmusicplayer.effects.AudioEffectsActivity;
import com.doctoror.fuckoffmusicplayer.library.LibraryActivity;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackService;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.playlist.CurrentPlaylist;
import com.doctoror.fuckoffmusicplayer.transition.TransitionUtils;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tbruyelle.rxpermissions.RxPermissions;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * "Now Playing" activity
 */
public final class NowPlayingActivity extends BaseActivity {

    private static final String TAG = "NowPlayingActivity";

    public static final String TRANSITION_NAME_ALBUM_ART
            = "NowPlayingActivity.TRANSITION_NAME_ALBUM_ART";
    public static final String TRANSITION_NAME_ROOT = "NowPlayingActivity.TRANSITION_NAME_ROOT";

    public static void start(@NonNull final Activity activity,
            @Nullable final View albumArt,
            @Nullable final View listItemView) {
        final Intent intent = Henson.with(activity)
                .gotoNowPlayingActivity()
                .hasCoverTransition(albumArt != null)
                .hasListViewTransition(listItemView != null)
                .build();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (albumArt != null) {
            final ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(activity, albumArt, TRANSITION_NAME_ALBUM_ART);
            activity.startActivity(intent, options.toBundle());
        } else if (listItemView != null) {
            final ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(activity, listItemView, TRANSITION_NAME_ROOT);
            activity.startActivity(intent, options.toBundle());
        } else {
            activity.startActivity(intent);
        }
    }

    private final NowPlayingActivityModel mModel = new NowPlayingActivityModel();
    private final Receiver mReceiver = new Receiver();
    private CurrentPlaylist mPlaylist;

    private int mState = PlaybackService.STATE_IDLE;
    private ActivityNowplayingBinding mBinding;

    private boolean mTransitionPostponed;
    private boolean mTransitionStarted;

    @InjectExtra
    boolean hasCoverTransition;

    @InjectExtra
    boolean hasListViewTransition;

    private volatile boolean mSeekBarTracking;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dart.inject(this);

        if (!RxPermissions.getInstance(this).isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            final Intent intent = Intent.makeMainActivity(
                    new ComponentName(this, LibraryActivity.class));
            startActivity(intent);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TransitionUtils.clearSharedElementsOnReturn(this);
            getWindow().setReturnTransition(new ArtAndControlsGateTransition());
        }

        mTransitionPostponed = false;
        mTransitionStarted = false;
        mPlaylist = CurrentPlaylist.getInstance(this);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_nowplaying);
        ViewCompat.setTransitionName(mBinding.albumArt, TRANSITION_NAME_ALBUM_ART);
        ViewCompat.setTransitionName(mBinding.getRoot(), TRANSITION_NAME_ROOT);
        ButterKnife.bind(this);

        mModel.setBtnPlayRes(R.drawable.ic_play_arrow_white_36dp);
        mBinding.setModel(mModel);
        setSupportActionBar(mBinding.toolbar);

        mBinding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(final SeekBar seekBar, final int i, final boolean b) {

            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {
                mSeekBarTracking = true;
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
                PlaybackService.seek(NowPlayingActivity.this,
                        (float) seekBar.getProgress() / (float) seekBar.getMax());
                mSeekBarTracking = false;
            }
        });

        handleIntent(getIntent());
    }

    private void setAlbumArt(@Nullable final String artUri) {
        if (!mTransitionPostponed && (hasCoverTransition || hasListViewTransition)) {
            mTransitionPostponed = true;
            supportPostponeEnterTransition();
        }
        if (TextUtils.isEmpty(artUri)) {
            Glide.clear(mBinding.albumArt);
            mBinding.albumArt.setImageResource(R.drawable.album_art_placeholder);
            onArtProcessed();
        } else {
            final DrawableRequestBuilder<String> b = Glide.with(this).load(artUri);
            if (hasCoverTransition || hasListViewTransition) {
                b.dontAnimate();
            }
            b.diskCacheStrategy(DiskCacheStrategy.NONE)
                    .dontTransform()
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(final Exception e, final String model,
                                final Target<GlideDrawable> target,
                                final boolean isFirstResource) {
                            mBinding.albumArt.setAlpha(0f);
                            mBinding.albumArt.setImageResource(R.drawable.album_art_placeholder);
                            mBinding.albumArt.animate().alpha(1f).start();
                            onArtProcessed();
                            return true;
                        }

                        @Override
                        public boolean onResourceReady(final GlideDrawable resource,
                                final String model,
                                final Target<GlideDrawable> target, final boolean isFromMemoryCache,
                                final boolean isFirstResource) {
                            onArtProcessed();
                            return false;
                        }
                    })
                    .into(mBinding.albumArt);
        }
    }

    private void onArtProcessed() {
        if (!isFinishingAfterTransition()) {
            if (!mTransitionStarted && (hasCoverTransition || hasListViewTransition)) {
                mTransitionStarted = true;
                try {
                    supportStartPostponedEnterTransition();
                } catch (NullPointerException e) {
                    Log.wtf(TAG, "While starting postponed transition", e);
                    // TODO sometimes get NPE. WTF?
                    //java.lang.NullPointerException: Attempt to invoke virtual method 'boolean android.app.ActivityOptions.isReturning()' on a null object reference
                    //at android.app.ActivityTransitionState.startEnter(ActivityTransitionState.java:203)
                    //at android.app.ActivityTransitionState.startPostponedEnterTransition(ActivityTransitionState.java:197)
                    //at android.app.Activity.startPostponedEnterTransition(Activity.java:6213)
                    //at android.support.v4.app.ActivityCompatApi21.startPostponedEnterTransition(ActivityCompatApi21.java:58)
                    //at android.support.v4.app.ActivityCompat.startPostponedEnterTransition(ActivityCompat.java:298)
                    //at android.support.v4.app.FragmentActivity.supportStartPostponedEnterTransition(FragmentActivity.java:271)
                }
            }
            if (mBinding.infoContainer.getScaleY() == 0f) {
                mBinding.infoContainer.setTranslationY(mBinding.infoContainer.getHeight() / 2);
                mBinding.infoContainer.animate().setStartDelay(500).scaleY(1f).translationY(0f)
                        .start();
            }
            if (mBinding.toolbar.getScaleY() == 0f) {
                mBinding.toolbar.setTranslationY(-(mBinding.toolbar.getHeight() / 2));
                mBinding.toolbar.animate().setStartDelay(500).scaleY(1f).translationY(0f).start();
            }
        }
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(@NonNull final Intent intent) {
        IntentHandler.handleIntent(this, intent);
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
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_nowplaying, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionEffects:
                startActivity(new Intent(this, AudioEffectsActivity.class));
                return true;

            case R.id.actionPlaylist:
                final Intent playlistActivity = Henson.with(this)
                        .gotoPlaylistActivity()
                        .hasCoverTransition(false)
                        .hasItemViewTransition(false)
                        .isNowPlayingPlaylist(true)
                        .playlist(CurrentPlaylist.getInstance(this).getPlaylist())
                        .build();
                playlistActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(playlistActivity);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindTrack(mPlaylist.getMedia(), mPlaylist.getPosition());
        bindState(PlaybackService.getLastKnownState());
        mPlaylist.addObserver(mPlaylistObserver);
        registerReceiver(mReceiver, mReceiver.mIntentFilter);
        PlaybackService.resendState(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        CurrentPlaylist.getInstance(this).deleteObserver(mPlaylistObserver);
        unregisterReceiver(mReceiver);
    }

    void bindTrack(@Nullable Media track, final long position) {
        if (!isFinishingAfterTransition()) {
            if (track != null) {
                setAlbumArt(track.getAlbumArt());
                mModel.setArtistAndAlbum(StringUtils.formatArtistAndAlbum(getResources(),
                        track.getArtist(), track.getAlbum()));
                mModel.setTitle(track.getTitle());
                mModel.setDuration(track.getDuration());
                bindProgress(position);
                mModel.notifyChange();
            } else {
                setAlbumArt(null);
                mModel.setArtistAndAlbum(StringUtils.formatArtistAndAlbum(getResources(),
                        null, null));
                mModel.setTitle(getString(R.string.Untitled));
                mModel.setElapsedTime(0);
                mModel.setProgress(0);
                mModel.setDuration(0);
                mModel.notifyChange();
            }
        }
    }

    void bindProgress(final long progress) {
        if (!isFinishingAfterTransition()) {
            mModel.setElapsedTime(progress);
            final long duration = mModel.getDuration();
            if (!mSeekBarTracking && duration > 0) {
                // Max is 200 so progress is a fraction of 200
                mModel.setProgress((int) (((double) progress / (double) duration) * 200f));
            }
        }
    }

    void bindState(final int state) {
        if (!isFinishingAfterTransition()) {
            mState = state;
            final int playBtnRes;
            switch (state) {
                case PlaybackService.STATE_IDLE:
                    playBtnRes = R.drawable.ic_play_arrow_white_36dp;
                    break;

                case PlaybackService.STATE_LOADING:
                    playBtnRes = R.drawable.ic_pause_white_36dp;
                    break;

                case PlaybackService.STATE_PLAYING:
                    playBtnRes = R.drawable.ic_pause_white_36dp;
                    break;

                case PlaybackService.STATE_PAUSED:
                    playBtnRes = R.drawable.ic_play_arrow_white_36dp;
                    break;

                default:
                    playBtnRes = R.drawable.ic_play_arrow_white_36dp;
                    break;
            }
            mModel.setBtnPlayRes(playBtnRes);
        }
    }

    @OnClick(R.id.btnPlay)
    public void onPlayClick() {
        switch (mState) {
            case PlaybackService.STATE_IDLE:
                PlaybackService.play(this);
                break;

            case PlaybackService.STATE_PAUSED:
                PlaybackService.play(this);
                break;

            case PlaybackService.STATE_PLAYING:
                PlaybackService.pause(this);
                break;
        }
    }

    @OnClick(R.id.btnPrev)
    public void onPrevClick() {
        PlaybackService.prev(this);
    }

    @OnClick(R.id.btnNext)
    public void onNextClick() {
        PlaybackService.next(this);
    }

    private final CurrentPlaylist.PlaylistObserver mPlaylistObserver
            = new CurrentPlaylist.PlaylistObserver() {

        @Override
        public void onPlaylistChanged(@Nullable final List<Media> playlist) {
            // Nothing
        }

        @Override
        public void onPlaylistOrderingChanged(@NonNull final List<Media> playlist) {
            // Nothing
        }

        @Override
        public void onPositionChanged(final long position) {
            bindProgress(position);
        }

        @Override
        public void onMediaChanged(final Media media) {
            runOnUiThread(() -> bindTrack(media, 0));
        }

        @Override
        public void onMediaRemoved(final Media media) {
            final List<Media> playlist = mPlaylist.getPlaylist();
            if (playlist == null || playlist.isEmpty()) {
                if (!isFinishing()) {
                    finish();
                }
            }
        }
    };

    private final class Receiver extends BroadcastReceiver {

        final IntentFilter mIntentFilter = new IntentFilter(PlaybackService.ACTION_STATE_CHANGED);

        @Override
        public void onReceive(final Context context, final Intent intent) {
            switch (intent.getAction()) {
                case PlaybackService.ACTION_STATE_CHANGED:
                    bindState(intent.getIntExtra(PlaybackService.EXTRA_STATE, 0));
                    break;
            }
        }
    }
}
