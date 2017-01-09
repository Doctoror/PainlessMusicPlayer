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
import com.doctoror.fuckoffmusicplayer.db.albums.AlbumsProvider;
import com.doctoror.fuckoffmusicplayer.db.artists.ArtistsProvider;
import com.doctoror.fuckoffmusicplayer.db.tracks.TracksProvider;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.util.Box;
import com.doctoror.fuckoffmusicplayer.util.RxUtils;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;

import rx.schedulers.Schedulers;

/**
 * Provides search for wear
 */
public final class WearableSearchProviderService extends IntentService {

    private static final String TAG = "WearSearchProviderService";

    private static final String ACTION_SEARCH = "ACTION_SEARCH";
    private static final String EXTRA_QUERY = "EXTRA_QUERY";

    private static final Integer LIMIT = 8;
    private static final Integer LIMIT_TRACKS = 15;

    public static void search(@NonNull final Context context,
            @NonNull final String query) {
        final Intent intent = new Intent(context, WearableSearchProviderService.class);
        intent.setAction(ACTION_SEARCH);
        intent.putExtra(EXTRA_QUERY, query);
        context.startService(intent);
    }

    private PowerManager.WakeLock mWakeLock;

    @Inject
    ArtistsProvider mArtistsProvider;

    @Inject
    AlbumsProvider mAlbumsProvider;

    @Inject
    TracksProvider mTracksProvider;

    public WearableSearchProviderService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();

        DaggerHolder.getInstance(this).wearComponent().inject(this);
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
        results.albums = queryAlbums(query);
        results.artists = queryArtists(query);
        results.tracks = queryTracks(query);

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
    private WearSearchData.Album[] queryAlbums(@NonNull final String query) {
        final Box<WearSearchData.Album[]> resultsHolder = new Box<>();
        RxUtils.subscribeBlocking(mAlbumsProvider.load(query, LIMIT)
                .subscribeOn(Schedulers.io())
                .map(this::cursorToWearSearchDataAlbum), resultsHolder);
        return resultsHolder.getValue();
    }

    @NonNull
    private WearSearchData.Album[] cursorToWearSearchDataAlbum(@NonNull final Cursor c) {
        final WearSearchData.Album[] results = new WearSearchData.Album[c.getCount()];
        int i = 0;
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext(), i++) {
            final WearSearchData.Album item = new WearSearchData.Album();
            item.id = c.getLong(AlbumsProvider.COLUMN_ID);
            item.title = c.getString(AlbumsProvider.COLUMN_ALBUM);
            results[i] = item;
        }
        return results;
    }

    @NonNull
    private WearSearchData.Artist[] queryArtists(@NonNull final String query) {
        final Box<WearSearchData.Artist[]> resultsHolder = new Box<>();
        RxUtils.subscribeBlocking(mArtistsProvider.load(query, LIMIT)
                .subscribeOn(Schedulers.io())
                .map(this::cursorToWearSearchDataArtist), resultsHolder);
        return resultsHolder.getValue();
    }

    @NonNull
    private WearSearchData.Artist[] cursorToWearSearchDataArtist(@NonNull final Cursor c) {
        final WearSearchData.Artist[] results = new WearSearchData.Artist[c.getCount()];
        int i = 0;
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext(), i++) {
            final WearSearchData.Artist item = new WearSearchData.Artist();
            item.id = c.getLong(ArtistsProvider.COLUMN_ID);
            item.title = c.getString(ArtistsProvider.COLUMN_ARTIST);
            results[i] = item;
        }
        return results;
    }

    @NonNull
    private WearSearchData.Track[] queryTracks(@NonNull final String query) {
        final Box<WearSearchData.Track[]> resultsHolder = new Box<>();
        RxUtils.subscribeBlocking(mTracksProvider.load(query, LIMIT_TRACKS, false)
                .subscribeOn(Schedulers.io())
                .map(this::cursorToWearSearchDataTrack), resultsHolder);
        return resultsHolder.getValue();
    }

    @NonNull
    private WearSearchData.Track[] cursorToWearSearchDataTrack(@NonNull final Cursor c) {
        final WearSearchData.Track[] results = new WearSearchData.Track[c.getCount()];
        int i = 0;
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext(), i++) {
            final WearSearchData.Track item = new WearSearchData.Track();
            item.id = c.getLong(TracksProvider.COLUMN_ID);
            item.title = c.getString(TracksProvider.COLUMN_TITLE);
            results[i] = item;
        }
        return results;
    }

}
