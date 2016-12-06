package com.doctoror.fuckoffmusicplayer.wear;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import com.doctoror.commons.util.Log;
import com.doctoror.commons.util.ProtoUtils;
import com.doctoror.commons.wear.DataPaths;
import com.doctoror.commons.wear.nano.WearSearchData;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.util.StringUtils;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.IOException;
import java.util.Set;

/**
 * Created by Yaroslav Mytkalyk on 21.11.16.
 */

public final class WearableSearchProviderService extends IntentService {

    private static final String TAG = "WearSearchProviderService";

    private static final String ACTION_SEARCH = "ACTION_SEARCH";
    private static final String EXTRA_QUERY = "EXTRA_QUERY";

    public static void search(@NonNull final Context context,
            @NonNull final String query) {
        final Intent intent = new Intent(context, WearableSearchProviderService.class);
        intent.setAction(ACTION_SEARCH);
        intent.putExtra(EXTRA_QUERY, query);
        context.startService(intent);
    }

    private PowerManager.WakeLock mWakeLock;

    public WearableSearchProviderService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_SEARCH:
                        onActionSearch(intent);
                        break;
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    private void onActionSearch(@NonNull final Intent intent) {
        final String query = intent.getStringExtra(EXTRA_QUERY);
        if (query == null) {
            throw new IllegalArgumentException("EXTRA_QUERY is null");
        }

        final WearSearchData.Results results = new WearSearchData.Results();
        final ContentResolver resolver = getContentResolver();
        results.albums = queryAlbums(resolver, query);
        results.artists = queryArtists(resolver, query);
        results.tracks = queryTracks(resolver, query);

        final GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API).build();
        final ConnectionResult connectionResult = googleApiClient.blockingConnect();
        if (!connectionResult.isSuccess()) {
            Log.w(TAG, "GoogleApiClient not connected: " + GoogleApiAvailability.getInstance()
                    .getErrorString(connectionResult.getErrorCode()));
            return;
        }

        try {
            final String capability = getString(R.string.wear_capability_search_receiver);
            final CapabilityApi.GetCapabilityResult capabilityResult = Wearable.CapabilityApi
                    .getCapability(googleApiClient, capability, CapabilityApi.FILTER_REACHABLE)
                    .await();
            final CapabilityInfo capabilityInfo = capabilityResult.getCapability();
            if (capabilityInfo == null) {
                Log.w(TAG, "No search receiver devices connected");
                return;
            }

            final String searchReceiverNodeId = pickBestNodeId(capabilityInfo.getNodes());
            if (TextUtils.isEmpty(searchReceiverNodeId)) {
                Log.w(TAG, "No search receiver nodes found");
                return;
            }

            final byte[] resultsArray;
            try {
                resultsArray = ProtoUtils.toByteArray(results);
            } catch (IOException e) {
                Log.w(TAG, e);
                return;
            }

            Wearable.MessageApi.sendMessage(googleApiClient, searchReceiverNodeId,
                    DataPaths.Messages.SEARCH_RESULT, resultsArray).await();
        } finally {
            googleApiClient.disconnect();
        }

    }

    @Nullable
    private static String pickBestNodeId(@Nullable final Set<Node> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }
        String bestNodeId = null;
        for (final Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }

    @NonNull
    private static WearSearchData.Album[] queryAlbums(@NonNull final ContentResolver resolver,
            @NonNull final String query) {
        final Cursor c = resolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM},
                query.isEmpty() ? null : MediaStore.Audio.Albums.ALBUM + " LIKE '%"
                        + StringUtils.sqlEscape(query) + "%'",
                null,
                MediaStore.Audio.Albums.ALBUM + " LIMIT 8");
        if (c == null) {
            return new WearSearchData.Album[0];
        }
        try {
            final WearSearchData.Album[] results = new WearSearchData.Album[c.getCount()];
            int i = 0;
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext(), i++) {
                final WearSearchData.Album item = new WearSearchData.Album();
                item.id = c.getLong(0);
                item.title = c.getString(1);
                results[i] = item;
            }
            return results;
        } finally {
            c.close();
        }
    }

    @NonNull
    private static WearSearchData.Artist[] queryArtists(@NonNull final ContentResolver resolver,
            @NonNull final String query) {
        final Cursor c = resolver.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Artists._ID, MediaStore.Audio.Artists.ARTIST},
                query.isEmpty() ? null : MediaStore.Audio.Artists.ARTIST + " LIKE '%"
                        + StringUtils.sqlEscape(query) + "%'",
                null,
                MediaStore.Audio.Artists.ARTIST + " LIMIT 8");
        if (c == null) {
            return new WearSearchData.Artist[0];
        }
        try {
            final WearSearchData.Artist[] results = new WearSearchData.Artist[c.getCount()];
            int i = 0;
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext(), i++) {
                final WearSearchData.Artist item = new WearSearchData.Artist();
                item.id = c.getLong(0);
                item.title = c.getString(1);
                results[i] = item;
            }
            return results;
        } finally {
            c.close();
        }
    }

    @NonNull
    private static WearSearchData.Track[] queryTracks(@NonNull final ContentResolver resolver,
            @NonNull final String query) {
        final Cursor c = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE},
                query.isEmpty() ? null : MediaStore.Audio.Media.TITLE + " LIKE '%"
                        + StringUtils.sqlEscape(query) + "%'",
                null,
                MediaStore.Audio.Media.TITLE + " LIMIT 15");
        if (c == null) {
            return new WearSearchData.Track[0];
        }
        try {
            final WearSearchData.Track[] results = new WearSearchData.Track[c.getCount()];
            int i = 0;
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext(), i++) {
                final WearSearchData.Track item = new WearSearchData.Track();
                item.id = c.getLong(0);
                item.title = c.getString(1);
                results[i] = item;
            }
            return results;
        } finally {
            c.close();
        }
    }

}
