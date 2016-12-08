package com.doctoror.fuckoffmusicplayer.auto;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistHolder;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaBrowserServiceCompat.BrowserRoot;
import android.support.v4.media.MediaBrowserServiceCompat.Result;
import android.support.v4.media.MediaDescriptionCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 08.12.16.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
final class MediaBrowserImpl {

    private static final String MEDIA_ID_ROOT = "ROOT";

    private static final String MEDIA_ID_CURENT_QUEUE = "CURRENT_QUEUE";
    private static final String MEDIA_ID_RANDOM = "RANDOM";
    private static final String MEDIA_ID_RECENT = "RECENT";

    @NonNull
    private final Context mContext;

    MediaBrowserImpl(@NonNull final Context context) {
        mContext = context;
    }

    BrowserRoot getRoot() {
        return new BrowserRoot(MEDIA_ID_ROOT, null);
    }

    void onLoadChildren(@NonNull final String parentId,
            @NonNull final Result<List<MediaItem>> result) {
        final List<MediaItem> mediaItems = new ArrayList<>();
        switch (parentId) {
            case MEDIA_ID_ROOT: {
                final List<Media> playlist = PlaylistHolder.getInstance(mContext).getPlaylist();
                if (playlist != null && !playlist.isEmpty()) {
                    mediaItems.add(createBrowsableMediaItemCurrentQueue());
                }
                //mediaItems.add(createBrowsableMediaItemRandom());
                //mediaItems.add(createBrowsableMediaItemRecent());
                break;
            }

            case MEDIA_ID_CURENT_QUEUE: {
                final List<Media> playlist = PlaylistHolder.getInstance(mContext).getPlaylist();
                if (playlist != null && !playlist.isEmpty()) {
                    for (int i = 0; i < playlist.size(); i++) {
                        mediaItems.add(createMediaItemMedia(playlist.get(i)));
                    }
                }
                break;
            }
        }

        result.sendResult(mediaItems);
    }

    @NonNull
    private MediaItem createBrowsableMediaItemCurrentQueue() {
        final MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(MEDIA_ID_CURENT_QUEUE)
                .setTitle(mContext.getText(R.string.Now_Playing))
                .setIconUri(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                        mContext.getPackageName() + "/drawable/ic_playlist_play_white_24dp"))
                .build();
        return new MediaItem(description, MediaItem.FLAG_BROWSABLE);
    }

    @NonNull
    private MediaItem createBrowsableMediaItemRandom() {
        final MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(MEDIA_ID_RANDOM)
                .setTitle(mContext.getText(R.string.Random_50))
                .setIconUri(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                        mContext.getPackageName() + "/drawable/ic_playlist_play_white_24dp"))
                .build();
        return new MediaItem(description, MediaItem.FLAG_BROWSABLE);
    }

    @NonNull
    private MediaItem createBrowsableMediaItemRecent() {
        final MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(MEDIA_ID_RECENT)
                .setTitle(mContext.getText(R.string.Recently_scanned))
                .setIconUri(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                        mContext.getPackageName() + "/drawable/ic_playlist_play_white_24dp"))
                .build();
        return new MediaItem(description, MediaItem.FLAG_BROWSABLE);
    }

    @NonNull
    private MediaItem createMediaItemMedia(@NonNull final Media media) {
        final MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(Long.toString(media.getId()))
                .setTitle(media.getTitle())
                .setSubtitle(media.getArtist())
                .build();
        return new MediaItem(description, MediaItem.FLAG_BROWSABLE);
    }
}
