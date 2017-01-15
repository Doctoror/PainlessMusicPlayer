/*
 * Copyright (C) 2017 Yaroslav Mytkalyk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.doctoror.fuckoffmusicplayer.home;

import com.doctoror.commons.util.Log;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.databinding.FragmentRecentActivityBinding;
import com.doctoror.fuckoffmusicplayer.db.albums.AlbumsProvider;
import com.doctoror.fuckoffmusicplayer.db.queue.QueueProviderAlbums;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.library.LibraryPermissionsFragment;
import com.doctoror.fuckoffmusicplayer.library.albums.AlbumClickHandler;
import com.doctoror.fuckoffmusicplayer.util.ObserverAdapter;
import com.doctoror.fuckoffmusicplayer.widget.SpacesItemDecoration;

import org.parceler.Parcel;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * "Recent Activity" fragment
 */
public final class RecentActivityFragment extends LibraryPermissionsFragment {

    private static final String TAG = "HomeFragment";

    private static final int MAX_HISTORY_SECTION_LENGTH = 6;

    private static final int ANIMATOR_CHILD_PROGRESS = 0;
    private static final int ANIMATOR_CHILD_PERMISSION_DENIED = 1;
    private static final int ANIMATOR_CHILD_EMPTY = 2;
    private static final int ANIMATOR_CHILD_ERROR = 3;
    private static final int ANIMATOR_CHILD_CONTENT = 4;

    private final RecentActivityFragmentModel mModel = new RecentActivityFragmentModel();

    private RecentActivityRecyclerAdapter mAdapter;

    @Inject
    AlbumsProvider mAlbumsProvider;

    @Inject
    QueueProviderAlbums mQueueProvider;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerHolder.getInstance(getActivity()).mainComponent().inject(this);

        setHasOptionsMenu(true);

        mAdapter = new RecentActivityRecyclerAdapter(getActivity());
        mAdapter.setOnAlbumClickListener(mOnAlbumClickListener);

        mModel.setEmptyMessage(getText(R.string.No_albums_found));
        mModel.setErrorText(getText(R.string.Failed_connecting_to_Media_Store));
        mModel.setRecyclerAdapter(mAdapter);
    }

    @Override
    protected void onPermissionGranted() {
        mModel.setDisplayedChild(ANIMATOR_CHILD_PROGRESS);
        load();
    }

    @Override
    protected void onPermissionDenied() {
        mModel.setDisplayedChild(ANIMATOR_CHILD_PERMISSION_DENIED);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        final FragmentRecentActivityBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_recent_activity, container, false);
        setupRecyclerView(binding.recyclerView);
        binding.setModel(mModel);
        binding.getRoot().findViewById(R.id.btnRequest)
                .setOnClickListener(v -> requestPermission());
        return binding.getRoot();
    }

    protected void setupRecyclerView(@NonNull final RecyclerView recyclerView) {
        final int columns = getResources().getInteger(R.integer.recent_activity_grid_columns);
        final GridLayoutManager lm = new GridLayoutManager(getActivity(), columns);
        lm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(final int position) {
                final RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
                if (adapter == null) {
                    return 1;
                }
                switch (adapter.getItemViewType(position)) {
                    case RecentActivityRecyclerAdapter.VIEW_TYPE_HEADER:
                        return columns;

                    default:
                        return 1;
                }
            }
        });

        recyclerView.setLayoutManager(lm);
        recyclerView.addItemDecoration(new SpacesItemDecoration(
                (int) getResources().getDimension(R.dimen.recent_activity_grid_spacing)));
    }

    private void load() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            final Observable<Cursor> recentlyPlayed =
                    mAlbumsProvider.loadRecentlyPlayedAlbums(MAX_HISTORY_SECTION_LENGTH).take(1);

            final Observable<Cursor> recentlyScanned =
                    mAlbumsProvider.loadRecentlyScannedAlbums(MAX_HISTORY_SECTION_LENGTH).take(1);

            registerOnStartSubscription(Observable.zip(recentlyPlayed,
                    recentlyScanned,
                    new RecyclerAdapterDataFunc(getResources()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(mObserver));
        } else {
            Log.w(TAG, "load() is called, READ_EXTERNAL_STORAGE is not granted");
        }
    }

    private final RecentActivityRecyclerAdapter.OnAlbumClickListener mOnAlbumClickListener
            = new RecentActivityRecyclerAdapter.OnAlbumClickListener() {

        @Override
        public void onAlbumClick(final View albumArtView, final long id, final String album) {
            AlbumClickHandler.onAlbumClick(RecentActivityFragment.this,
                    mQueueProvider,
                    albumArtView,
                    id,
                    album);
        }
    };

    private final Observer<List<Object>> mObserver = new ObserverAdapter<List<Object>>() {

        @Override
        public void onError(final Throwable e) {
            if (isAdded()) {
                mModel.setErrorText(getText(R.string.Failed_connecting_to_Media_Store));
                mModel.setDisplayedChild(ANIMATOR_CHILD_ERROR);
            }
        }

        @Override
        public void onNext(final List<Object> data) {
            mAdapter.setItems(data);
            mModel.setDisplayedChild(data.isEmpty()
                    ? ANIMATOR_CHILD_EMPTY : ANIMATOR_CHILD_CONTENT);
        }
    };

    private static final class RecyclerAdapterDataFunc
            implements Func2<Cursor, Cursor, List<Object>> {

        @NonNull
        private final Resources mRes;

        RecyclerAdapterDataFunc(@NonNull final Resources res) {
            mRes = res;
        }

        @Override
        public List<Object> call(final Cursor rPlayed, final Cursor rAdded) {
            final List<Object> data = new ArrayList<>(MAX_HISTORY_SECTION_LENGTH + 2);

            final List<AlbumItem> rPlayedList = AlbumItemsFactory.itemsFromCursor(rPlayed);
            if (!rPlayedList.isEmpty()) {
                data.add(new RecentActivityHeader(mRes.getText(R.string.Recently_played_albums)));
                data.addAll(rPlayedList);
            }

            data.add(new RecentActivityHeader(mRes.getText(R.string.Recently_added)));
            data.addAll(AlbumItemsFactory.itemsFromCursor(rAdded));

            rPlayed.close();
            rAdded.close();
            return data;
        }
    }

    @Parcel
    static final class InstanceState {

        boolean permissionsRequested;
    }

}
