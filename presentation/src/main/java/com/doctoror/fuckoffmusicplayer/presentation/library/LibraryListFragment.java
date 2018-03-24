/*
 * Copyright (C) 2016 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.presentation.library;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.data.util.Log;
import com.doctoror.fuckoffmusicplayer.databinding.FragmentLibraryListBinding;
import com.doctoror.fuckoffmusicplayer.util.SearchViewUtils;
import com.doctoror.fuckoffmusicplayer.util.SoftInputManager;
import com.doctoror.fuckoffmusicplayer.util.ViewUtils;
import com.doctoror.fuckoffmusicplayer.presentation.widget.SwipeDirectionTouchListener;
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView;

import org.parceler.Parcel;
import org.parceler.Parcels;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.schedulers.Schedulers;

/**
 * Fragment used for library list
 */
public abstract class LibraryListFragment extends LibraryPermissionsFragment {

    private static final String TAG = "LibraryListFragment";

    private static final String KEY_INSTANCE_STATE = "LibraryListFragment.INSTANCE_STATE";

    private static final int ANIMATOR_CHILD_PROGRESS = 0;
    private static final int ANIMATOR_CHILD_PERMISSION_DENIED = 1;
    private static final int ANIMATOR_CHILD_EMPTY = 2;
    private static final int ANIMATOR_CHILD_ERROR = 3;
    private static final int ANIMATOR_CHILD_CONTENT = 4;

    private final BehaviorProcessor<String> mSearchProcessor = BehaviorProcessor.create();
    private final LibraryListFragmentModel mModel = new LibraryListFragmentModel();

    private Disposable mDisposableOld;
    private Disposable mDisposable;

    private boolean mCanShowEmptyView = true;
    private boolean mSearchIconified = true;

    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }
    }

    private void restoreInstanceState(@NonNull final Bundle savedInstanceState) {
        final InstanceState state = Parcels.unwrap(savedInstanceState
                .getParcelable(KEY_INSTANCE_STATE));
        if (state != null) {
            mSearchIconified = state.searchIconified;
            mSearchProcessor.onNext(state.searchQuery);
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        final InstanceState state = new InstanceState();
        state.searchIconified = mSearchIconified;
        state.searchQuery = mSearchProcessor.getValue();
        outState.putParcelable(KEY_INSTANCE_STATE, Parcels.wrap(state));
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_library_list, menu);
        if (hasPermissions()) {
            final SearchView searchView = (SearchView) menu.findItem(R.id.actionFilter)
                    .getActionView();
            SearchViewUtils.setSearchIcon(searchView, R.drawable.ic_filter_list_white_24dp);
            searchView.setQueryHint(getText(R.string.Filter));

            searchView.setQuery(mSearchProcessor.getValue(), false);
            searchView.setOnCloseListener(() -> {
                mSearchIconified = true;
                return false;
            });
            searchView.setOnSearchClickListener((v) -> mSearchIconified = false);
            searchView.setIconified(mSearchIconified);

            RxSearchView
                    .queryTextChanges(searchView)
                    .debounce(400, TimeUnit.MILLISECONDS)
                    .subscribe(t -> mSearchProcessor.onNext(t.toString()));

        } else {
            menu.findItem(R.id.actionFilter).setVisible(false);
        }
    }

    @Override
    protected void onPermissionGranted() {
        mModel.setDisplayedChild(ANIMATOR_CHILD_PROGRESS);
        disposeOnStop(mSearchProcessor.hide().subscribe(mSearchQueryConsumer));
        getActivity().invalidateOptionsMenu();
    }

    @Override
    protected void onPermissionDenied() {
        mModel.setDisplayedChild(ANIMATOR_CHILD_PERMISSION_DENIED);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        final FragmentLibraryListBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_library_list, container, false);
        setupRecyclerView(binding.recyclerView);
        initSwipeDirection(binding);
        binding.setModel(mModel);
        binding.getRoot().findViewById(R.id.btnRequest)
                .setOnClickListener(v -> requestPermission());
        mRecyclerView = binding.recyclerView;
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSwipeDirection(@NonNull final FragmentLibraryListBinding binding) {
        binding.recyclerView.setOnTouchListener(new SwipeDirectionTouchListener() {

            @Override
            protected void onSwipedDown() {
                SoftInputManager.hideSoftInput(getActivity());
            }
        });
    }

    @Nullable
    protected final View getItemView(final int position) {
        return ViewUtils.getItemView(mRecyclerView, position);
    }

    protected void setupRecyclerView(@NonNull final RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onStop() {
        super.onStop();
        SoftInputManager.hideSoftInput(getActivity());
        onDataReset();
    }

    private void restartLoader(@Nullable final String searchFilter) {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            mDisposableOld = mDisposable;
            mDisposable = disposeOnStop(load(searchFilter)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onNextSearchResult, this::onSearchResultLoadFailed));
        } else {
            Log.w(TAG, "restartLoader is called, READ_EXTERNAL_STORAGE is not granted");
        }
    }

    protected abstract Observable<Cursor> load(@Nullable final String filter);

    protected abstract void onDataLoaded(@NonNull Cursor data);

    protected abstract void onDataReset();

    protected final void setCanShowEmptyView(final boolean canShowEmptyView) {
        mCanShowEmptyView = canShowEmptyView;
    }

    protected final void setRecyclerAdapter(@Nullable final RecyclerView.Adapter<?> adapter) {
        mModel.setRecyclerAdapter(adapter);
    }

    protected final void setEmptyMessage(@Nullable final CharSequence emptyMessage) {
        mModel.setEmptyMessage(emptyMessage);
    }

    private void onNextSearchResult(@NonNull final Cursor cursor) {
        onDataLoaded(cursor);
        if (mDisposableOld != null) {
            mDisposableOld.dispose();
            mDisposableOld = null;
        }
        mModel.setDisplayedChild(cursor.getCount() == 0 && mCanShowEmptyView
                ? ANIMATOR_CHILD_EMPTY : ANIMATOR_CHILD_CONTENT);
    }

    private void onSearchResultLoadFailed(@NonNull final Throwable t) {
        Log.w(TAG, "onSearchResultLoadFailed()", t);
        if (mDisposableOld != null) {
            mDisposableOld.dispose();
            mDisposableOld = null;
        }
        onDataReset();
        if (isAdded()) {
            mModel.setDisplayedChild(ANIMATOR_CHILD_ERROR);
        }
    }

    private final Consumer<String> mSearchQueryConsumer = this::restartLoader;

    @Parcel
    static final class InstanceState {

        String searchQuery;
        boolean searchIconified;
    }

}
