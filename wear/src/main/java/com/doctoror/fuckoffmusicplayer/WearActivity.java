package com.doctoror.fuckoffmusicplayer;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.Wearable;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;

import com.doctoror.commons.wear.DataPaths;
import com.doctoror.commons.wear.nano.ProtoPlaybackData;
import com.doctoror.fuckoffmusicplayer.databinding.ActivityWearBinding;
import com.doctoror.fuckoffmusicplayer.util.GooglePlayServicesUtil;

import android.app.Activity;
import android.content.IntentSender;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import rx.Observable;
import rx.schedulers.Schedulers;

public final class WearActivity extends Activity {

    private static final String TAG = "WearActivity";

    private static final int REQUEST_CODE_GOOGLE_API = 1;

    private static final int ANIMATOR_CHILD_PRGORESS = 0;
    private static final int ANIMATOR_CHILD_CONTENT = 1;

    private final WearActivityModel mModel = new WearActivityModel();

    private GoogleApiClient mGoogleApiClient;
    private View mBtnFix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityWearBinding binding = DataBindingUtil
                .setContentView(this, R.layout.activity_wear);
        binding.setModel(mModel);
        mBtnFix = binding.btnFix;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mOnConnectionFailedListener)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setViewConnecting();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Wearable.DataApi.removeListener(mGoogleApiClient, mDataListener);
        mGoogleApiClient.disconnect();
    }

    private void setViewConnecting() {
        mModel.setFixButtonVisible(false);
        mModel.setProgressVisible(true);
        mModel.setMessage(getText(R.string.Connecting));
        mModel.setAnimatorChild(ANIMATOR_CHILD_PRGORESS);
    }

    private void bindMedia(@Nullable final ProtoPlaybackData.Media media) {
        if (media != null) {
            mModel.setArtist(media.artist);
            mModel.setAlbum(media.album);
            mModel.setTitle(media.title);
            bindProgress(media.duration, media.progress);
        } else {
            mModel.setArtist(getText(R.string.Unknown_artist));
            mModel.setAlbum(getText(R.string.Unknown_album));
            mModel.setTitle(getText(R.string.Untitled));
            bindProgress(0, 0);
        }
        mModel.setArt(getDrawable(R.drawable.album_art_placeholder));
        mModel.setAnimatorChild(ANIMATOR_CHILD_CONTENT);
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

    private void onMediaItemChanged(@NonNull final DataItem mediaItem) {
        final byte[] data = mediaItem.getData();
        if (data == null) {
            bindMedia(null);
        } else {
            Observable.create(s -> {
                try {
                    final ProtoPlaybackData.Media media = ProtoPlaybackData.Media.parseFrom(data);
                    bindMedia(media);
                } catch (InvalidProtocolBufferNanoException e) {
                    Log.w(TAG, e);
                    mModel.setMessage(getText(R.string.Failed_to_parse_data));
                }
            })
                    .subscribeOn(Schedulers.io())
                    .subscribe();
        }
    }

    private void onPlaybackStateItemChanged(@NonNull final DataItem stateItem) {
        final byte[] data = stateItem.getData();
        if (data == null) {
            bindPlaybackState(null);
        } else {
            Observable.create(s -> {
                try {
                    final ProtoPlaybackData.PlaybackState media = ProtoPlaybackData.PlaybackState
                            .parseFrom(data);
                    bindPlaybackState(media);
                } catch (InvalidProtocolBufferNanoException e) {
                    Log.w(TAG, e);
                }
            })
                    .subscribeOn(Schedulers.io())
                    .subscribe();
        }
    }

    private final GoogleApiClient.ConnectionCallbacks mConnectionCallbacks
            = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(@Nullable final Bundle bundle) {
            mModel.setFixButtonVisible(false);
            mModel.setMessage(getText(R.string.Waiting_for_data));
            Wearable.DataApi.addListener(mGoogleApiClient, mDataListener);
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

    private final DataApi.DataListener mDataListener = dataEventBuffer -> {
        for (final DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                final DataItem item = event.getDataItem();
                final String path = item.getUri().getPath();
                switch (path) {
                    case DataPaths.PATH_MEDIA:
                        onMediaItemChanged(item);
                        break;

                    case DataPaths.PATH_PLAYBACK_STATE:
                        onPlaybackStateItemChanged(item);
                        break;
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                bindMedia(null);
            }
        }
    };
}
