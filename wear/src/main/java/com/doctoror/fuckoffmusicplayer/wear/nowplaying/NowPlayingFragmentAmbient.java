package com.doctoror.fuckoffmusicplayer.wear.nowplaying;

import android.app.Fragment;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.doctoror.commons.util.StringUtils;
import com.doctoror.commons.wear.nano.WearPlaybackData;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.databinding.FragmentNowPlayingAmbientBinding;
import com.doctoror.fuckoffmusicplayer.wear.media.MediaHolder;
import com.doctoror.fuckoffmusicplayer.wear.media.eventbus.EventMedia;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * "Now playing" fragment for ambient mode
 */
public final class NowPlayingFragmentAmbient extends Fragment {

    private final NowPlayingFragmentModelMedia mModelMedia
            = new NowPlayingFragmentModelMedia();

    private MediaHolder mMediaHolder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMediaHolder = MediaHolder.getInstance(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final FragmentNowPlayingAmbientBinding binding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_now_playing_ambient, container, false);
        binding.setMedia(mModelMedia);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        bindMedia(mMediaHolder.getMedia());
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEventMedia(@NonNull final EventMedia event) {
        bindMedia(event.media);
    }

    private void bindMedia(@Nullable final WearPlaybackData.Media media) {
        if (media != null) {
            mModelMedia.setArtistAndAlbum(StringUtils.formatArtistAndAlbum(getResources(),
                    media.artist, media.album));
            mModelMedia.setTitle(media.title);
        } else {
            mModelMedia.setArtistAndAlbum(null);
            mModelMedia.setTitle(getText(R.string.Start_playing));
        }
    }
}
