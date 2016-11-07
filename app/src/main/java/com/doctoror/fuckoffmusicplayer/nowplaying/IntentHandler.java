package com.doctoror.fuckoffmusicplayer.nowplaying;

import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistUtils;
import com.doctoror.fuckoffmusicplayer.util.Log;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 07.11.16.
 */

final class IntentHandler {

    private static final String TAG = "IntentHandler";

    private IntentHandler() {

    }

    @NonNull
    static List<Media> playlistFromActionView(@NonNull final ContentResolver contentResolver,
            @NonNull final Intent intent) throws IOException {
        final Uri data = intent.getData();
        if (data == null) {
            Log.w(TAG, "Intent data is null");
            throw new IOException("Intent data is null");
        }

        final String scheme = data.getScheme();
        if (scheme == null) {
            Log.w(TAG, "Uri scheme is null");
            throw new IOException("Uri scheme is null");
        }
        switch (scheme) {
            case "file":
                return playFile(contentResolver, data);

            default:
                Log.w(TAG, "Unhandled Uri scheme: " + scheme);
                throw new IOException("Unhandled Uri scheme: " + scheme);
        }
    }

    @NonNull
    private static List<Media> playFile(@NonNull final ContentResolver contentResolver,
            @NonNull final Uri data) throws IOException {
        try {
            return PlaylistUtils.forFile(contentResolver, data);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }
}
