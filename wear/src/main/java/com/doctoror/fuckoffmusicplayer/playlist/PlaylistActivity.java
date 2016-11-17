package com.doctoror.fuckoffmusicplayer.playlist;

import com.doctoror.commons.wear.nano.ProtoPlaybackData;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.media.MediaHolder;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.wearable.view.WearableListView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 17.11.16.
 */

public final class PlaylistActivity extends Activity {

    private MediaHolder mMediaHolder;
    private PlaylistHolder mPlaylistHolder;

    private ImageView mBackground;
    private PlaylistListAdapter mAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMediaHolder = MediaHolder.getInstance(this);
        mPlaylistHolder = PlaylistHolder.getInstance(this);
        setContentView(R.layout.activity_playlist);
        initView();
    }

    private void initView() {
        mBackground = (ImageView) findViewById(R.id.background);
        mBackground.setColorFilter(ContextCompat.getColor(this, R.color.translucentBackground),
                PorterDuff.Mode.SRC_ATOP);

        mAdapter = new PlaylistListAdapter(this);

        final WearableListView listView = (WearableListView) findViewById(android.R.id.list);
        listView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBackground.setImageDrawable(albumArtOrStub(mMediaHolder.getAlbumArt()));
        mAdapter.setItems(makePlaylist(mPlaylistHolder.getPlaylist(), mMediaHolder.getMedia()));
        mMediaHolder.addObserver(mPlaybackInfoObserver);
        mPlaylistHolder.addObserver(mPlaylistObserver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPlaylistHolder.deleteObserver(mPlaylistObserver);
        mMediaHolder.deleteObserver(mPlaybackInfoObserver);
    }

    @NonNull
    private static List<ProtoPlaybackData.Media> makePlaylist(
            @Nullable final ProtoPlaybackData.Playlist playlist,
            @Nullable final ProtoPlaybackData.Media media) {
        List<ProtoPlaybackData.Media> p = null;
        if (playlist != null) {
            final ProtoPlaybackData.Media[] medias = playlist.media;
            if (medias != null && medias.length != 0) {
                p = Arrays.asList(medias);
            }
        }

        if (p == null) {
            p = new ArrayList<>(1);
            p.add(media);
        }

        return p;
    }

    private Drawable albumArtOrStub(@Nullable final Bitmap art) {
        if (art == null) {
            return getDrawable(R.drawable.album_art_placeholder);
        }
        return new BitmapDrawable(getResources(), art);
    }

    private final PlaylistHolder.PlaylistObserver mPlaylistObserver
            = new PlaylistHolder.PlaylistObserver() {

        @Override
        public void onPlaylistChanged(@Nullable final ProtoPlaybackData.Playlist playlist) {
            runOnUiThread(() -> mAdapter.setItems(makePlaylist(playlist, mMediaHolder.getMedia())));
        }
    };

    private final MediaHolder.PlaybackInfoObserver mPlaybackInfoObserver
            = new MediaHolder.PlaybackInfoObserver() {

        @Override
        public void onMediaChanged(@Nullable final ProtoPlaybackData.Media media) {
            // If playlist is a fake list of single media, update it
            if (mAdapter.getItemCount() == 1) {
                runOnUiThread(() -> mAdapter.setItems(
                        makePlaylist(mPlaylistHolder.getPlaylist(), media)));
            }
        }

        @Override
        public void onPlaybackStateChanged(
                @Nullable final ProtoPlaybackData.PlaybackState playbackState) {

        }

        @Override
        public void onAlbumArtChanged(@Nullable final Bitmap albumArt) {
            //noinspection WrongThread
            runOnUiThread(() -> mBackground.setImageDrawable(albumArtOrStub(albumArt)));
        }
    };
}
