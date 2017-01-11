package com.doctoror.fuckoffmusicplayer.library.albums;

import com.doctoror.fuckoffmusicplayer.Henson;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderAlbums;
import com.doctoror.fuckoffmusicplayer.queue.Media;
import com.doctoror.fuckoffmusicplayer.queue.QueueActivity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Yaroslav Mytkalyk on 11.01.17.
 */
public final class AlbumClickHandler {

    private AlbumClickHandler() {
        throw new UnsupportedOperationException();
    }

    public static void onAlbumClick(@NonNull final Fragment host,
            @NonNull final PlaylistProviderAlbums playlistProviderAlbums,
            @NonNull final View view,
            final long albumId,
            @Nullable final String albumName) {
        Observable.<List<Media>>create(s -> s.onNext(playlistProviderAlbums.fromAlbum(albumId)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((queue) -> {
                    if (host.isAdded()) {
                        if (queue != null && !queue.isEmpty()) {
                            final Activity activity = host.getActivity();
                            final Intent intent = Henson.with(activity).gotoQueueActivity()
                                    .hasCoverTransition(true)
                                    .hasItemViewTransition(false)
                                    .isNowPlayingQueue(false)
                                    .queue(queue)
                                    .title(albumName)
                                    .build();

                            final ActivityOptionsCompat options = ActivityOptionsCompat
                                    .makeSceneTransitionAnimation(activity, view,
                                            QueueActivity.TRANSITION_NAME_ALBUM_ART);
                            host.startActivity(intent, options.toBundle());
                        } else {
                            Toast.makeText(host.getActivity(), R.string.The_queue_is_empty,
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }

}
