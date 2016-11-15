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
import com.doctoror.fuckoffmusicplayer.BaseActivity;
import com.doctoror.fuckoffmusicplayer.Henson;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.databinding.ActivityNowplayingBinding;
import com.doctoror.fuckoffmusicplayer.effects.AudioEffectsActivity;
import com.doctoror.fuckoffmusicplayer.library.LibraryActivity;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackService;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistActivity;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistHolder;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistUtils;
import com.doctoror.fuckoffmusicplayer.util.ObserverAdapter;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.jakewharton.rxbinding.widget.RxSeekBar;
import com.tbruyelle.rxpermissions.RxPermissions;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
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
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Yaroslav Mytkalyk on 21.10.16.
 */
public final class NowPlayingActivity extends BaseActivity {

    public static final String VIEW_ALBUM_ART = "VIEW_ALBUM_ART";

    public static void start(@NonNull final Activity activity,
            @Nullable final View albumArt) {
        final Intent intent = Henson.with(activity)
                .gotoNowPlayingActivity()
                .hasCoverTransition(albumArt != null)
                .build();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (albumArt == null) {
            activity.startActivity(intent);
        } else {
            final ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(activity, albumArt,
                            PlaylistActivity.VIEW_ALBUM_ART);
            activity.startActivity(intent, options.toBundle());
        }
    }

    private final NowPlayingActivityModel mModel = new NowPlayingActivityModel();
    private final Receiver mReceiver = new Receiver();
    private PlaylistHolder mPlaylist;

    private int mState = PlaybackService.STATE_IDLE;
    private ActivityNowplayingBinding mBinding;

    @InjectExtra
    @Nullable
    Boolean hasCoverTransition;

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

        mPlaylist = PlaylistHolder.getInstance(this);

        mBinding = DataBindingUtil.setContentView(this,
                R.layout.activity_nowplaying);
        ViewCompat.setTransitionName(mBinding.albumArt, VIEW_ALBUM_ART);
        ButterKnife.bind(this);

        mModel.setBtnPlayRes(R.drawable.ic_play_arrow_white_36dp);
        mBinding.setModel(mModel);
        setSupportActionBar(mBinding.toolbar);

        RxSeekBar.userChanges(mBinding.seekBar).subscribe(new Action1<Integer>() {

            private boolean mFirst = true;

            @Override
            public void call(final Integer progress) {
                if (mFirst) {
                    mFirst = false;
                } else {
                    PlaybackService.seek(NowPlayingActivity.this, (float) progress / 200f);
                }
            }
        });

        handleIntent(getIntent());
    }

    private void setAlbumArt(@Nullable final String artUri) {
        if (TextUtils.isEmpty(artUri)) {
            Glide.clear(mBinding.albumArt);
            mBinding.albumArt.setImageResource(R.drawable.album_art_placeholder);
            onArtProcessed();
        } else {
            supportPostponeEnterTransition();
            final DrawableRequestBuilder<String> b = Glide.with(this).load(artUri);
            if (hasCoverTransition != null && hasCoverTransition) {
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
        try {
            supportStartPostponedEnterTransition();
        } catch (NullPointerException e) {
            // TODO sometimes get NPE. WTF?
            //java.lang.NullPointerException: Attempt to invoke virtual method 'boolean android.app.ActivityOptions.isReturning()' on a null object reference
            //at android.app.ActivityTransitionState.startEnter(ActivityTransitionState.java:203)
            //at android.app.ActivityTransitionState.startPostponedEnterTransition(ActivityTransitionState.java:197)
            //at android.app.Activity.startPostponedEnterTransition(Activity.java:6213)
            //at android.support.v4.app.ActivityCompatApi21.startPostponedEnterTransition(ActivityCompatApi21.java:58)
            //at android.support.v4.app.ActivityCompat.startPostponedEnterTransition(ActivityCompat.java:298)
            //at android.support.v4.app.FragmentActivity.supportStartPostponedEnterTransition(FragmentActivity.java:271)
        }
        if (mBinding.infoContainer.getAlpha() != 1f) {
            mBinding.infoContainer.animate().setStartDelay(500).alpha(1f).start();
        }
        if (mBinding.toolbar.getAlpha() != 1f) {
            mBinding.toolbar.animate().setStartDelay(500).alpha(1f).start();
        }
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(@NonNull final Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            rx.Observable.<List<Media>>create(s -> {
                try {
                    s.onNext(IntentHandler.playlistFromActionView(getContentResolver(), intent));
                } catch (IOException e) {
                    s.onError(e);
                }
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new ObserverAdapter<List<Media>>() {
                        @Override
                        public void onError(final Throwable e) {
                            if (!isFinishing()) {
                                Toast.makeText(getApplicationContext(),
                                        R.string.Failed_to_start_playback, Toast.LENGTH_LONG)
                                        .show();
                            }
                        }

                        @Override
                        public void onNext(final List<Media> playlist) {
                            if (!isFinishing()) {
                                if (playlist.isEmpty()) {
                                    Toast.makeText(getApplicationContext(),
                                            R.string.Failed_to_start_playback, Toast.LENGTH_LONG)
                                            .show();
                                } else {
                                    PlaylistUtils.play(NowPlayingActivity.this, playlist);
                                }
                            }
                        }
                    });
        }
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
                        .isNowPlayingPlaylist(Boolean.TRUE)
                        .playlist(PlaylistHolder.getInstance(this).getPlaylist())
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
        mPlaylist.addObserver(mPlaylistObserver);
        registerReceiver(mReceiver, mReceiver.mIntentFilter);
        PlaybackService.resendState(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        PlaylistHolder.getInstance(this).deleteObserver(mPlaylistObserver);
        unregisterReceiver(mReceiver);
    }

    void bindTrack(@Nullable Media track, final long position) {
        if (track != null) {
            setAlbumArt(track.getAlbumArt());
            mModel.setArtist(track.getArtist());
            mModel.setAlbum(track.getAlbum());
            mModel.setTitle(track.getTitle());
            mModel.setDuration(track.getDuration());
            bindProgress(position);
            mModel.notifyChange();
        } else {
            setAlbumArt(null);
            mModel.setArtist(getString(R.string.Unknown_artist));
            mModel.setAlbum(getString(R.string.Unknown_album));
            mModel.setTitle(getString(R.string.Untitled));
            mModel.setElapsedTime(0);
            mModel.setProgress(0);
            mModel.setDuration(0);
            mModel.notifyChange();
        }
    }

    void bindProgress(final long progress) {
        mModel.setElapsedTime(progress);
        final long duration = mModel.getDuration();
        if (duration > 0) {
            // Max is 200 so progress is a fraction of 200
            mModel.setProgress((int) (((double) progress / (double) duration) * 200f));
        }
    }

    void bindState(final int state) {
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

    private final PlaylistHolder.PlaylistObserver mPlaylistObserver
            = new PlaylistHolder.PlaylistObserver() {

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
