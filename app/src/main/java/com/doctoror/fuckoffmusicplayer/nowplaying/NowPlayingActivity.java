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
import com.doctoror.commons.playback.PlaybackState;
import com.doctoror.commons.util.Log;
import com.doctoror.commons.util.StringUtils;
import com.doctoror.fuckoffmusicplayer.base.BaseActivity;
import com.doctoror.fuckoffmusicplayer.Henson;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.databinding.ActivityNowplayingBinding;
import com.doctoror.fuckoffmusicplayer.db.queue.QueueProviderFiles;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.effects.AudioEffectsActivity;
import com.doctoror.fuckoffmusicplayer.home.HomeActivity;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackParams;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackServiceControl;
import com.doctoror.fuckoffmusicplayer.playback.data.PlaybackData;
import com.doctoror.fuckoffmusicplayer.queue.Media;
import com.doctoror.fuckoffmusicplayer.queue.QueueActivity;
import com.doctoror.fuckoffmusicplayer.transition.TransitionUtils;
import com.doctoror.fuckoffmusicplayer.util.CollectionUtils;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;

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
        Bundle options = null;
        if (albumArt != null) {
            options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, albumArt,
                    TRANSITION_NAME_ALBUM_ART).toBundle();
        } else if (listItemView != null) {
            options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, listItemView,
                    TRANSITION_NAME_ROOT).toBundle();
        }
        activity.startActivity(intent, options);
    }

    private final NowPlayingActivityModel mModel = new NowPlayingActivityModel();

    private NowPlayingActivityIntentHandler mIntentHandler;

    private PlaybackParams mPlaybackParams;

    @PlaybackState.State
    private int mState = PlaybackState.STATE_IDLE;

    private boolean mTransitionPostponed;
    private boolean mTransitionStarted;

    @BindView(R.id.root)
    View root;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.albumArt)
    ImageView albumArt;

    @Nullable
    @BindView(R.id.infoContainer)
    View infoContainer;

    @BindView(R.id.seekBar)
    SeekBar seekBar;

    @InjectExtra
    boolean hasCoverTransition;

    @InjectExtra
    boolean hasListViewTransition;

    @Inject
    PlaybackData mPlaybackData;

    @Inject
    QueueProviderFiles mFileQueueProvider;

    private volatile boolean mSeekBarTracking;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dart.inject(this);
        DaggerHolder.getInstance(this).mainComponent().inject(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            final Intent intent = Intent.makeMainActivity(
                    new ComponentName(this, HomeActivity.class));
            startActivity(intent);
            return;
        }

        if (TransitionUtils.supportsActivityTransitions()) {
            NowPlayingActivityLollipop.applyTransitions(this);
        }

        mTransitionPostponed = false;
        mTransitionStarted = false;

        mPlaybackParams = PlaybackParams.getInstance(this);

        final ActivityNowplayingBinding binding = DataBindingUtil.setContentView(
                this, R.layout.activity_nowplaying);
        ButterKnife.bind(this);

        ViewCompat.setTransitionName(albumArt, TRANSITION_NAME_ALBUM_ART);
        ViewCompat.setTransitionName(root, TRANSITION_NAME_ROOT);

        mModel.setBtnPlayRes(R.drawable.ic_play_arrow_white_36dp);
        mModel.setShuffleEnabled(mPlaybackParams.isShuffleEnabled());
        mModel.setRepeatMode(mPlaybackParams.getRepeatMode());

        binding.setModel(mModel);
        setSupportActionBar(toolbar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(final SeekBar seekBar, final int i, final boolean b) {

            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {
                mSeekBarTracking = true;
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
                PlaybackServiceControl.seek(NowPlayingActivity.this,
                        (float) seekBar.getProgress() / (float) seekBar.getMax());
                mSeekBarTracking = false;
            }
        });

        mIntentHandler = new NowPlayingActivityIntentHandler(this);
        mIntentHandler.handleIntent(getIntent());
    }

    private void setAlbumArt(@Nullable final String artUri) {
        if (!mTransitionPostponed && (hasCoverTransition || hasListViewTransition)) {
            mTransitionPostponed = true;
            supportPostponeEnterTransition();
        }
        if (TextUtils.isEmpty(artUri)) {
            Glide.clear(albumArt);
            albumArt.setImageResource(R.drawable.album_art_placeholder);
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
                            albumArt.setAlpha(0f);
                            albumArt.setImageResource(R.drawable.album_art_placeholder);
                            albumArt.animate().alpha(1f).start();
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
                    .into(albumArt);
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
            if (infoContainer != null && infoContainer.getVisibility() != View.VISIBLE) {
                if (hasListViewTransition) {
                    infoContainer.setVisibility(View.VISIBLE);
                } else {
                    infoContainer.setTranslationY(infoContainer.getHeight());
                    infoContainer.animate().setStartDelay(500)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationStart(final Animator animation) {
                                    infoContainer.setVisibility(View.VISIBLE);
                                }
                            })
                            .translationY(0f).start();
                }
            }
            if (toolbar.getVisibility() != View.VISIBLE) {
                if (hasListViewTransition) {
                    toolbar.setVisibility(View.VISIBLE);
                } else {
                    toolbar.setTranslationY(-toolbar.getHeight());
                    toolbar.animate().setStartDelay(500)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationStart(final Animator animation) {
                                    toolbar.setVisibility(View.VISIBLE);
                                }
                            })
                            .translationY(0f).start();
                }
            }
        }
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        mIntentHandler.handleIntent(intent);
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

            case R.id.actionQueue:
                final Intent intent = Henson.with(this)
                        .gotoQueueActivity()
                        .hasCoverTransition(true)
                        .hasItemViewTransition(false)
                        .isNowPlayingQueue(true)
                        .queue(mPlaybackData.getQueue())
                        .build();

                final Bundle options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                        albumArt, QueueActivity.TRANSITION_NAME_ALBUM_ART).toBundle();

                startActivity(intent, options);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerOnStartSubscription(mPlaybackData.playbackStateObservable()
                .subscribe(this::bindState));

        registerOnStartSubscription(mPlaybackData.queueObservable().subscribe(mQueueAction));

        registerOnStartSubscription(mPlaybackData.queuePositionObservable()
                .subscribe(mQueuePositionAction));

        registerOnStartSubscription(mPlaybackData.mediaPositionObservable()
                .subscribe(mMediaPositionAction));
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
                case PlaybackState.STATE_IDLE:
                    playBtnRes = R.drawable.ic_play_arrow_white_36dp;
                    break;

                case PlaybackState.STATE_LOADING:
                    playBtnRes = R.drawable.ic_pause_white_36dp;
                    break;

                case PlaybackState.STATE_PLAYING:
                    playBtnRes = R.drawable.ic_pause_white_36dp;
                    break;

                case PlaybackState.STATE_PAUSED:
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
            case PlaybackState.STATE_IDLE:
                PlaybackServiceControl.play(this);
                break;

            case PlaybackState.STATE_PAUSED:
                PlaybackServiceControl.playPause(this);
                break;

            case PlaybackState.STATE_PLAYING:
                PlaybackServiceControl.playPause(this);
                break;

            case PlaybackState.STATE_ERROR:
                PlaybackServiceControl.play(this);
                break;

            case PlaybackState.STATE_LOADING:
                // Do nothing
                break;
        }
    }

    @OnClick(R.id.btnPrev)
    public void onPrevClick() {
        PlaybackServiceControl.prev(this);
    }

    @OnClick(R.id.btnNext)
    public void onNextClick() {
        PlaybackServiceControl.next(this);
    }

    @OnClick(R.id.btnShuffle)
    public void onShuffleClick() {
        final boolean newValue = !mPlaybackParams.isShuffleEnabled();
        mPlaybackParams.setShuffleEnabled(newValue);
        mModel.setShuffleEnabled(newValue);
    }

    @OnClick(R.id.btnRepeat)
    public void onRepeatClick() {
        @PlaybackParams.RepeatMode final int value;
        switch (mPlaybackParams.getRepeatMode()) {
            case PlaybackParams.REPEAT_MODE_NONE:
                value = PlaybackParams.REPEAT_MODE_QUEUE;
                break;

            case PlaybackParams.REPEAT_MODE_QUEUE:
                value = PlaybackParams.REPEAT_MODE_TRACK;
                break;

            case PlaybackParams.REPEAT_MODE_TRACK:
                value = PlaybackParams.REPEAT_MODE_NONE;
                break;

            default:
                throw new IllegalArgumentException(
                        "Unexpected repeat mode: " + mPlaybackParams.getRepeatMode());
        }
        mPlaybackParams.setRepeatMode(value);
        mModel.setRepeatMode(value);
    }

    private final Action1<List<Media>> mQueueAction = p -> {
        if (p == null || p.isEmpty()) {
            if (!isFinishing()) {
                finish();
            }
        }
    };

    private final Action1<Integer> mQueuePositionAction = pos -> {
        final Media media = CollectionUtils.getItemSafe(mPlaybackData.getQueue(), pos);
        runOnUiThread(() -> bindTrack(media, mPlaybackData.getMediaPosition()));
    };

    private final Action1<Long> mMediaPositionAction = this::bindProgress;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static final class NowPlayingActivityLollipop {

        static void applyTransitions(@NonNull final NowPlayingActivity activity) {
            TransitionUtils.clearSharedElementsOnReturn(activity);
            final boolean isLandscape = activity.getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_LANDSCAPE;
            activity.getWindow().setReturnTransition(isLandscape
                    ? new RootViewVerticalGateTransition()
                    : new ArtAndControlsGateTransition());
        }

    }
}
