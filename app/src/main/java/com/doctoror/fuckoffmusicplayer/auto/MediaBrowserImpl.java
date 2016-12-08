package com.doctoror.fuckoffmusicplayer.auto;

import com.doctoror.fuckoffmusicplayer.R;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.media.MediaDescription;
import android.media.browse.MediaBrowser.MediaItem;
import android.net.Uri;
import android.os.Build;
import android.service.media.MediaBrowserService.BrowserRoot;
import android.service.media.MediaBrowserService.Result;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 08.12.16.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
final class MediaBrowserImpl {

    public static final String MEDIA_ID_ROOT = "__ROOT__";
    public static final String MEDIA_ID_MUSIC_GENRES = "GENRES";
    public static final String MEDIA_ID_MUSIC_ARTISTS = "ARTISTS";
    public static final String MEDIA_ID_MUSIC_ALBUMS = "ALBUMS";

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
            case MEDIA_ID_ROOT:
                mediaItems.add(createBrowsableMediaItemGenres());
                mediaItems.add(createBrowsableMediaItemArtists());
                break;

            case MEDIA_ID_MUSIC_GENRES:
                // TODO
                break;
        }

        result.sendResult(mediaItems);
    }

    @NonNull
    private MediaItem createBrowsableMediaItemGenres() {
        MediaDescription description = new MediaDescription.Builder()
                .setMediaId(MEDIA_ID_MUSIC_GENRES)
                .setTitle(mContext.getText(R.string.Genres))
                .setIconUri(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                        mContext.getPackageName() + "/drawable/ic_by_genre"))
                .build();
        return new MediaItem(description, MediaItem.FLAG_BROWSABLE);
    }

    @NonNull
    private MediaItem createBrowsableMediaItemArtists() {
        MediaDescription description = new MediaDescription.Builder()
                .setMediaId(MEDIA_ID_MUSIC_ARTISTS)
                .setTitle(mContext.getText(R.string.Artists))
                .setIconUri(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                        mContext.getPackageName() + "/drawable/ic_by_genre"))
                .build();
        return new MediaItem(description, MediaItem.FLAG_BROWSABLE);
    }

}
