package com.doctoror.fuckoffmusicplayer.data.playback;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.SparseIntArray;

import com.doctoror.fuckoffmusicplayer.data.util.CollectionUtils;
import com.doctoror.fuckoffmusicplayer.data.util.Log;
import com.doctoror.fuckoffmusicplayer.data.util.RandomHolder;
import com.doctoror.fuckoffmusicplayer.domain.effects.AudioEffects;
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumThumbHolder;
import com.doctoror.fuckoffmusicplayer.domain.media.CurrentMediaProvider;
import com.doctoror.fuckoffmusicplayer.domain.media.session.MediaSessionHolder;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackParams;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackService;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceView;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.domain.playback.RepeatMode;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayer;
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayerFactory;
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayerListener;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderRecentlyScanned;
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporter;
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporterFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_ERROR;
import static com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_IDLE;
import static com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_LOADING;
import static com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_PAUSED;
import static com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_PLAYING;

public final class PlaybackServiceImpl implements PlaybackService {

    private static final String TAG = "PlaybackServiceImpl";

    private final Context mContext;

    private final AlbumThumbHolder mAlbumThumbHolder;

    private final AudioEffects mAudioEffects;

    private final CurrentMediaProvider mCurrentMediaProvider;

    private final MediaPlayerFactory mMediaPlayerFactory;

    private final MediaSessionHolder mMediaSessionHolder;

    private final PlaybackData mPlaybackData;

    private final PlaybackInitializer mPlaybackInitializer;

    private final PlaybackParams mPlaybackParams;

    private final PlaybackReporterFactory mPlaybackReporterFactory;

    private final PlaybackServiceView mPlaybackServicePresenter;

    private final QueueProviderRecentlyScanned queueProviderRecentlyScanned;

    private final Runnable mStopAction;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final AudioBecomingNoisyReceiver mBecomingNoisyReceiver
            = new AudioBecomingNoisyReceiver();

    private PlaybackState mState = STATE_IDLE;

    private MediaPlayer mMediaPlayer;

    private AudioManager mAudioManager;

    private PlaybackReporter mPlaybackReporter;

    private boolean mAudioFocusRequested;
    private boolean mFocusGranted;
    private boolean mPlayOnFocusGain;

    private Media mCurrentTrack;

    private Disposable mDisposableTimer;
    private Disposable mDisposablePauseTimeout;

    private PowerManager.WakeLock mWakeLock;
    private boolean mDestroying;

    private CharSequence mErrorMessage;

    private PlaybackController mPlaybackController;
    private Disposable mDisposableQueue;

    public PlaybackServiceImpl(
            @NonNull final Context context,
            @NonNull final AlbumThumbHolder albumThumbHolder,
            @NonNull final AudioEffects audioEffects,
            @NonNull final CurrentMediaProvider currentMediaProvider,
            @NonNull final MediaPlayerFactory mediaPlayerFactory,
            @NonNull final MediaSessionHolder mediaSessionHolder,
            @NonNull final PlaybackData playbackData,
            @NonNull final PlaybackInitializer playbackInitializer,
            @NonNull final PlaybackParams playbackParams,
            @NonNull final PlaybackReporterFactory playbackReporterFactory,
            @NonNull final PlaybackServiceView playbackServicePresenter,
            @NonNull final QueueProviderRecentlyScanned queueProviderRecentlyScanned,
            @NonNull final Runnable stopAction) {
        mContext = context;
        mAlbumThumbHolder = albumThumbHolder;
        mAudioEffects = audioEffects;
        mCurrentMediaProvider = currentMediaProvider;
        mMediaPlayerFactory = mediaPlayerFactory;
        mMediaSessionHolder = mediaSessionHolder;
        mPlaybackData = playbackData;
        mPlaybackInitializer = playbackInitializer;
        mPlaybackParams = playbackParams;
        mPlaybackReporterFactory = playbackReporterFactory;
        mPlaybackServicePresenter = playbackServicePresenter;
        this.queueProviderRecentlyScanned = queueProviderRecentlyScanned;
        mStopAction = stopAction;
        init();
    }

    private void init() {
        acquireWakeLock();

        mMediaPlayer = mMediaPlayerFactory.newMediaPlayer();

        mDestroying = false;
        mErrorMessage = null;

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        mMediaSessionHolder.openSession();

        final MediaSessionCompat mediaSession = mMediaSessionHolder.getMediaSession();
        if (mediaSession == null) {
            throw new IllegalStateException("MediaSession is null");
        }

        mPlaybackReporter = mPlaybackReporterFactory.newUniversalReporter(mediaSession);

        mContext.registerReceiver(mBecomingNoisyReceiver, mBecomingNoisyReceiver.mIntentFilter);

        mMediaPlayer.setListener(mMediaPlayerListener);
        mMediaPlayer.init(mContext);

        mDisposableQueue = mPlaybackData.queueObservable().subscribe(new QueueConsumer());
    }

    @SuppressLint("WakelockTimeout") // User may want this to play forever.
    private void acquireWakeLock() {
        final PowerManager powerManager = (PowerManager) mContext
                .getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            mWakeLock.acquire();
        }
    }

    @NonNull
    private PlaybackController getPlaybackController() {
        boolean created = false;
        if (mPlaybackController == null) {
            mPlaybackController = mPlaybackParams.isShuffleEnabled()
                    ? new PlaybackControllerShuffle()
                    : new PlaybackControllerNormal();
            created = true;
        } else if (mPlaybackParams.isShuffleEnabled()) {
            if (!PlaybackControllerShuffle.class.equals(mPlaybackController.getClass())) {
                mPlaybackController = new PlaybackControllerShuffle();
                created = true;
            }
        } else {
            if (!PlaybackControllerNormal.class.equals(mPlaybackController.getClass())) {
                mPlaybackController = new PlaybackControllerNormal();
                created = true;
            }
        }
        if (created) {
            mPlaybackController.setQueue(mPlaybackData.getQueue());
            mPlaybackController.setPositionInQueue(mPlaybackData.getQueuePosition());
        }
        return mPlaybackController;
    }

    @Override
    public void playPause() {
        switch (mState) {
            case STATE_PLAYING:
                pause();
                break;

            case STATE_PAUSED:
                play();
                break;

            case STATE_IDLE:
            case STATE_ERROR:
                playCurrentOrNewQueue();
                break;

            case STATE_LOADING:
            default:
                // Do nothing
                break;
        }
    }

    @Override
    public void play() {
        if (mDisposablePauseTimeout != null) {
            mDisposablePauseTimeout.dispose();
            mDisposablePauseTimeout = null;
        }
        mPlayOnFocusGain = true;
        playCurrent(true);
    }

    @Override
    public void playAnything() {
        playCurrentOrNewQueue();
    }

    @Override
    public void pause() {
        mPlayOnFocusGain = false;
        pauseInner();
        mDisposablePauseTimeout = Observable.timer(8, TimeUnit.SECONDS)
                .subscribe(o -> stop());
        showNotification();
    }

    @Override
    public void stop() {
        mPlayOnFocusGain = false;
        mStopAction.run();
    }

    @Override
    public void stopWithError(@Nullable final CharSequence errorMessage) {
        mPlayOnFocusGain = false;
        mErrorMessage = errorMessage;
        mStopAction.run();
    }

    @Override
    public void playPrev() {
        playPrevInner();
    }

    @Override
    public void playNext() {
        playNextInner(true);
    }

    @Override
    public void seek(final long position) {
        mMediaPlayer.seekTo(position);
    }

    private void pauseInner() {
        mMediaPlayer.pause();
        setState(STATE_PAUSED);
    }

    private void playCurrentOrNewQueue() {
        final List<Media> playlist = mPlaybackData.getQueue();
        if (playlist != null && !playlist.isEmpty()) {
            play(playlist, mPlaybackData.getQueuePosition(), true, false);
        } else {
            queueProviderRecentlyScanned.recentlyScannedQueue()
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            q -> mPlaybackInitializer.setQueueAndPlay(q, 0),
                            t -> Log.w(TAG, "Failed to load recently scanned", t));
        }
    }

    private void playCurrent(final boolean mayContinueWhereStopped) {
        play(mPlaybackData.getQueue(), mPlaybackData.getQueuePosition(),
                mayContinueWhereStopped, false);
    }

    private void playPrevInner() {
        getPlaybackController().playPrev();
    }

    private void playNextInner(final boolean isUserAction) {
        getPlaybackController().playNext(isUserAction);
    }

    private void restart() {
        mCurrentTrack = null;
        if (mState == STATE_PLAYING) {
            mMediaPlayer.stop();
        }
        playCurrent(false);
    }

    private void play(@Nullable final List<Media> queue,
                      final int position,
                      final boolean mayContinueWhereStopped,
                      final boolean fromPlaybackController) {
        if (queue == null) {
            throw new IllegalArgumentException("Play queue is null");
        }

        ensureFocusRequested();
        if (!mFocusGranted) {
            return;
        }

        Media media = CollectionUtils.getItemSafe(queue, position);
        if (media == null) {
            media = queue.get(0);
        }
        final Media finalMedia = media;
        if (mState == STATE_PAUSED && mCurrentTrack != null
                && media.getId() == mCurrentTrack.getId()) {
            mMediaPlayer.play();
            mExecutor.submit(mRunnableReportCurrentMedia);
        } else {
            mExecutor.submit(() -> {
                long seekPosition = 0;
                // If restoring from stopped state, set seek position to what it was
                if (mayContinueWhereStopped && mState == STATE_IDLE
                        && finalMedia.equals(mCurrentMediaProvider.getCurrentMedia())) {
                    seekPosition = mPlaybackData.getMediaPosition();
                    if (seekPosition >= finalMedia.getDuration() - 100) {
                        seekPosition = 0;
                    }
                }
                mPlaybackData.setPlayQueuePosition(position);
                mPlaybackData.setMediaPosition(seekPosition);
                mCurrentTrack = finalMedia;

                if (!fromPlaybackController) {
                    getPlaybackController().setPositionInQueue(position);
                }

                reportCurrentMedia();
                reportCurrentPlaybackPosition();

                mMediaPlayer.stop();
                mMediaPlayer.load(finalMedia.getData());
                if (seekPosition != 0) {
                    mMediaPlayer.seekTo(seekPosition);
                }
                mMediaPlayer.play();
            });
        }
    }

    @Nullable
    private MediaSessionCompat getMediaSession() {
        return mMediaSessionHolder != null ? mMediaSessionHolder.getMediaSession() : null;
    }

    private void showNotification() {
        final Media media = mCurrentMediaProvider.getCurrentMedia();
        if (media != null) {
            final MediaSessionCompat mediaSession = getMediaSession();
            if (mediaSession != null) {
                mExecutor.submit(() -> mPlaybackServicePresenter.startForeground(media, mState));
            }
        }
    }

    @Override
    public void destroy() {
        mMediaPlayer.stop();

        mDisposableQueue.dispose();
        mPlaybackReporter.onDestroy();

        mPlaybackData.setMediaPosition(mMediaPlayer.getCurrentPosition());
        mPlaybackData.persistAsync();

        mDestroying = true;
        mPlayOnFocusGain = false;
        mContext.unregisterReceiver(mBecomingNoisyReceiver);
        if (mErrorMessage != null) {
            setState(STATE_ERROR);
        } else {
            setState(STATE_IDLE);
        }
        if (mDisposableTimer != null) {
            mDisposableTimer.dispose();
            mDisposableTimer = null;
        }
        mAudioEffects.relese();
        mMediaPlayer.release();
        mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        mAudioFocusRequested = false;
        mMediaSessionHolder.closeSession();
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    private void ensureFocusRequested() {
        if (!mAudioFocusRequested) {
            mAudioFocusRequested = true;
            final int result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            mPlayOnFocusGain = true;
            mFocusGranted = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }
    }

    private void setState(@NonNull final PlaybackState state) {
        if (mState != state) {
            mState = state;
            notifyState();
            mPlaybackData.setPlaybackState(state);
        }
    }

    @Override
    public void notifyState() {
        mExecutor.submit(() -> reportPlaybackState(mState, mErrorMessage));
    }

    private void updateMediaPosition() {
        if (mState == STATE_PLAYING) {
            mPlaybackData.setMediaPosition(mMediaPlayer.getCurrentPosition());
            mExecutor.submit(mRunnableReportCurrentPosition);
        }
    }

    @WorkerThread
    private void reportCurrentMedia() {
        final int pos = mPlaybackData.getQueuePosition();
        final Media media = CollectionUtils.getItemSafe(mPlaybackData.getQueue(), pos);
        if (media != null) {
            mPlaybackReporter.reportTrackChanged(media, pos);
        }
    }

    @WorkerThread
    private void reportPlaybackState(
            @NonNull final PlaybackState state,
            @Nullable final CharSequence errorMessage) {
        mPlaybackReporter.reportPlaybackStateChanged(state, errorMessage);
    }

    @WorkerThread
    private void reportCurrentPlaybackPosition() {
        final Media media = mCurrentMediaProvider.getCurrentMedia();
        if (media == null) {
            mPlaybackReporter.reportPositionChanged(0, 0);
        } else {
            final Uri mediaUri = media.getData();
            if (mediaUri != null && mediaUri.equals(mMediaPlayer.getLoadedMediaUri())) {
                mPlaybackReporter.reportPositionChanged(
                        media.getId(), mMediaPlayer.getCurrentPosition());
            }
        }
    }

    @WorkerThread
    private void reportCurrentQueue() {
        final List<Media> queue = mPlaybackData.getQueue();
        if (queue != null) {
            mPlaybackReporter.reportQueueChanged(queue);
        }
    }

    private final Runnable mRunnableReportCurrentMedia = this::reportCurrentMedia;
    private final Runnable mRunnableReportCurrentPosition = this::reportCurrentPlaybackPosition;
    private final Runnable mRunnableReportCurrentQueue = this::reportCurrentQueue;

    private final MediaPlayerListener mMediaPlayerListener = new MediaPlayerListener() {

        @Override
        public void onAudioSessionId(final int audioSessionId) {
            mErrorMessage = null;
            if (audioSessionId == MediaPlayer.SESSION_ID_NOT_SET) {
                mAudioEffects.relese();
            } else {
                mAudioEffects.create(audioSessionId);
            }
        }

        @Override
        public void onLoading() {
            mErrorMessage = null;
            setState(STATE_LOADING);
        }

        @Override
        public void onPlaybackStarted() {
            mErrorMessage = null;
            setState(STATE_PLAYING);
            showNotification();
            mDisposableTimer = Observable.interval(1L, TimeUnit.SECONDS)
                    .subscribe(o -> updateMediaPosition());
        }

        @Override
        public void onPlaybackFinished() {
            mErrorMessage = null;
            mCurrentTrack = null;
            if (!mDestroying) {
                playNextInner(false);
            }
        }

        @Override
        public void onPlaybackPaused() {
            mErrorMessage = null;
            setState(STATE_PAUSED);
            if (mDisposableTimer != null) {
                mDisposableTimer.dispose();
                mDisposableTimer = null;
            }
        }

        @Override
        public void onPlayerError(final Exception error) {
            mCurrentTrack = null;
            mErrorMessage = mPlaybackServicePresenter.showPlaybackFailedError(error);
            setState(STATE_ERROR);
            mStopAction.run();
        }
    };

    private final AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener
            = focusChange -> {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                mFocusGranted = true;
                if (mPlayOnFocusGain) {
                    playCurrent(true);
                }
                break;

            default:
                mFocusGranted = false;
                mPlayOnFocusGain = mState == STATE_PLAYING;
                pauseInner();
                break;
        }
    };

    private final class QueueConsumer implements Consumer<List<Media>> {

        @Override
        public void accept(@NonNull final List<Media> q) throws Exception {
            if (q.isEmpty()) {
                mCurrentTrack = null;
                mAlbumThumbHolder.setAlbumThumb(null);
                mStopAction.run();
            } else {
                final PlaybackController playbackController = getPlaybackController();
                playbackController.setQueue(q);

                final Media current = mCurrentTrack;
                // Playing some track
                if (current != null) {
                    final int indexOf = q.indexOf(current);
                    if (indexOf != -1) {
                        // This track position changed in queue
                        playbackController.setPositionInQueue(indexOf);
                        mExecutor.submit(mRunnableReportCurrentQueue);
                    } else {
                        // This track is not in new queue
                        restart();
                    }
                }
            }
        }
    }

    private final class AudioBecomingNoisyReceiver extends BroadcastReceiver {

        final IntentFilter mIntentFilter = new IntentFilter(
                AudioManager.ACTION_AUDIO_BECOMING_NOISY);

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
                mStopAction.run();
            }
        }
    }

    private interface PlaybackController {

        void playNext(boolean isUserAction);

        void playPrev();

        void setQueue(@Nullable List<Media> queue);

        void setPositionInQueue(int position);
    }

    private class PlaybackControllerNormal implements PlaybackController {

        private final Object LOCK = new Object();

        private List<Media> mQueue;
        private int mPosition;

        @Override
        public void playPrev() {
            synchronized (LOCK) {
                if (mQueue != null && !mQueue.isEmpty()) {
                    @RepeatMode final int repeatMode = mPlaybackParams.getRepeatMode();
                    switch (repeatMode) {
                        case RepeatMode.NONE:
                            onPlay(mQueue, prevPos(mQueue, mPosition));
                            break;

                        case RepeatMode.QUEUE:
                            onPlay(mQueue, prevPos(mQueue, mPosition));
                            break;

                        case RepeatMode.TRACK:
                            onPlay(mQueue, mPosition);
                            break;
                    }
                }
            }
        }

        @Override
        public void playNext(final boolean isUserAction) {
            synchronized (LOCK) {
                if (mQueue != null && !mQueue.isEmpty()) {
                    final int repeatMode = mPlaybackParams.getRepeatMode();
                    switch (repeatMode) {
                        case RepeatMode.NONE:
                            if (!isUserAction && mPosition == mQueue.size() - 1) {
                                mStopAction.run();
                            } else {
                                onPlay(mQueue, nextPos(mQueue, mPosition));
                            }
                            break;

                        case RepeatMode.QUEUE:
                            onPlay(mQueue, nextPos(mQueue, mPosition));
                            break;

                        case RepeatMode.TRACK:
                            if (isUserAction) {
                                onPlay(mQueue, nextPos(mQueue, mPosition));
                            } else {
                                onPlay(mQueue, mPosition);
                            }
                            break;
                    }
                }
            }
        }

        private void onPlay(@Nullable final List<Media> list, final int position) {
            mPosition = position;
            play(list, position);
        }

        protected void play(@Nullable final List<Media> list, final int position) {
            PlaybackServiceImpl.this.play(list, position, false, true);
        }

        @Override
        public void setQueue(final List<Media> queue) {
            synchronized (LOCK) {
                mQueue = queue;
            }
        }

        @Override
        public void setPositionInQueue(final int position) {
            synchronized (LOCK) {
                mPosition = position;
            }
        }

        private int prevPos(@Nullable final List<Media> list, final int position) {
            if (list == null) {
                throw new IllegalArgumentException("Playlist is null");
            }
            if (position - 1 < 0) {
                return list.size() - 1;
            }
            return position - 1;
        }

        private int nextPos(@Nullable final List<Media> list,
                            final int position) {
            if (list == null) {
                throw new IllegalArgumentException("Playlist is null");
            }
            if (position + 1 >= list.size()) {
                return 0;
            }
            return position + 1;
        }
    }

    private final class PlaybackControllerShuffle extends PlaybackControllerNormal {

        private final Object mLock = new Object();

        @NonNull
        private final SparseIntArray mShuffledPositions = new SparseIntArray();

        @Override
        public void setQueue(@Nullable final List<Media> queue) {
            synchronized (mLock) {
                rebuildShuffledPositions(queue == null ? 0 : queue.size());
            }
            super.setQueue(queue);
        }

        @Override
        protected void play(@Nullable final List<Media> list, final int position) {
            final int shuffledPosition;
            synchronized (mLock) {
                shuffledPosition = mShuffledPositions.get(position);
            }
            super.play(list, shuffledPosition);
        }

        private void rebuildShuffledPositions(final int size) {
            mShuffledPositions.clear();
            if (size != 0) {
                final List<Integer> positions = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    positions.add(i);
                }

                Collections.shuffle(positions, RandomHolder.getInstance().getRandom());

                for (int i = 0; i < size; i++) {
                    mShuffledPositions.put(i, positions.get(i));
                }
            }
        }
    }
}
