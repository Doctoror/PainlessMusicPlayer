package com.doctoror.fuckoffmusicplayer;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import com.doctoror.commons.util.StringUtils;
import com.doctoror.commons.wear.nano.ProtoPlaybackData;
import com.doctoror.fuckoffmusicplayer.databinding.ActivityWearBinding;
import com.doctoror.fuckoffmusicplayer.media.MediaHolder;
import com.doctoror.fuckoffmusicplayer.util.GooglePlayServicesUtil;

import android.app.Activity;
import android.content.IntentSender;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.Toast;

public final class WearActivity extends Activity {

    private static final String TAG = "WearActivity";

    private static final int REQUEST_CODE_GOOGLE_API = 1;

    private static final int ANIMATOR_CHILD_PRGORESS = 0;
    private static final int ANIMATOR_CHILD_CONTENT = 1;

    private final WearActivityModel mModel = new WearActivityModel();

    private MediaHolder mMediaHolder;

    private GoogleApiClient mGoogleApiClient;
    private View mBtnFix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModel.setBtnPlayRes(R.drawable.ic_play_arrow_white_24dp);

        final ActivityWearBinding binding = DataBindingUtil
                .setContentView(this, R.layout.activity_wear);
        binding.setModel(mModel);
        mBtnFix = binding.btnFix;
        mMediaHolder = MediaHolder.getInstance(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mOnConnectionFailedListener)
                .build();

        setViewConnecting();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindMedia(mMediaHolder.getMedia());
        bindPlaybackState(mMediaHolder.getPlaybackState());
        mMediaHolder.addObserver(mPlaybackInfoObserver);
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMediaHolder.deleteObserver(mPlaybackInfoObserver);
        mGoogleApiClient.disconnect();
    }

    private void setViewConnecting() {
        mModel.setFixButtonVisible(false);
        mModel.setProgressVisible(true);
        mModel.setMessage(getText(R.string.Connecting));
        mModel.setAnimatorChild(ANIMATOR_CHILD_PRGORESS);
    }

    private void setViewConnected() {
        mModel.setFixButtonVisible(false);
        mModel.setProgressVisible(false);
        mModel.setAnimatorChild(ANIMATOR_CHILD_CONTENT);
    }

    private void bindMedia(@Nullable final ProtoPlaybackData.Media media) {
        if (media != null) {
            mModel.setArtistAndAlbum(StringUtils.formatArtistAndAlbum(getResources(),
                    media.artist, media.album));
            mModel.setTitle(media.title);
            mModel.setNavigationButtonsVisible(true);
            bindProgress(media.duration, media.progress);
        } else {
            mModel.setArtistAndAlbum(null);
            mModel.setTitle(getText(R.string.Start_playing));
            mModel.setNavigationButtonsVisible(false);
            bindProgress(0, 0);
        }
        mModel.setArt(getDrawable(R.drawable.album_art_placeholder));
        mModel.notifyChange();
    }

    private void bindPlaybackState(@Nullable final ProtoPlaybackData.PlaybackState playbackState) {
        if (playbackState != null) {
            bindProgress(playbackState.duration, playbackState.progress);
            mModel.setBtnPlayRes(playbackState.state == PlaybackStateCompat.STATE_PLAYING
                    ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp);
        } else {
            mModel.setBtnPlayRes(R.drawable.ic_play_arrow_white_24dp);
        }
    }

    private void bindProgress(final long duration, final long elapsedTime) {
        mModel.setDuration(duration);
        mModel.setElapsedTime(elapsedTime);
        if (duration > 0 && elapsedTime <= duration) {
            // Max is 200 so progress is a fraction of 200
            mModel.setProgress((int) (((double) elapsedTime / (double) duration) * 200f));
        }
    }

    private final MediaHolder.PlaybackInfoObserver mPlaybackInfoObserver
            = new MediaHolder.PlaybackInfoObserver() {

        @Override
        public void onMediaChanged(@Nullable final ProtoPlaybackData.Media media) {
            bindMedia(media);
        }

        @Override
        public void onPlaybackStateChanged(
                @Nullable final ProtoPlaybackData.PlaybackState playbackState) {
            bindPlaybackState(playbackState);
        }
    };

    private final GoogleApiClient.ConnectionCallbacks mConnectionCallbacks
            = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(@Nullable final Bundle bundle) {
            setViewConnected();
        }

        @Override
        public void onConnectionSuspended(final int i) {
            setViewConnecting();
        }
    };

    private final GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedListener
            = connectionResult -> {
        mModel.setProgressVisible(false);
        mModel.setMessage(GooglePlayServicesUtil
                .toHumanReadableMessage(getResources(), connectionResult.getErrorCode()));
        mModel.setFixButtonVisible(connectionResult.hasResolution());
        if (connectionResult.hasResolution()) {
            mBtnFix.setOnClickListener(v -> {
                try {
                    connectionResult.startResolutionForResult(this, REQUEST_CODE_GOOGLE_API);
                } catch (IntentSender.SendIntentException e) {
                    Toast.makeText(this, R.string.Could_not_fix_this_issue, Toast.LENGTH_LONG)
                            .show();
                    mModel.setFixButtonVisible(false);
                }
            });
        }
        mModel.setAnimatorChild(ANIMATOR_CHILD_PRGORESS);
    };
}
