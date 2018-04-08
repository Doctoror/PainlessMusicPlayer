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

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.databinding.FragmentLibraryListBinding;
import com.doctoror.fuckoffmusicplayer.presentation.base.BaseFragment;
import com.doctoror.fuckoffmusicplayer.presentation.util.SearchViewUtils;
import com.doctoror.fuckoffmusicplayer.presentation.util.SoftInputManager;
import com.doctoror.fuckoffmusicplayer.presentation.util.ViewUtils;
import com.doctoror.fuckoffmusicplayer.presentation.widget.SwipeDirectionTouchListener;
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView;

import org.parceler.Parcel;
import org.parceler.Parcels;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.processors.BehaviorProcessor;
import kotlin.jvm.functions.Function1;

/**
 * Fragment used for library list
 */
public abstract class LibraryListFragment extends BaseFragment {

    private static final String KEY_INSTANCE_STATE = "LibraryListFragment.INSTANCE_STATE";

    private final BehaviorProcessor<String> mSearchProcessor = BehaviorProcessor.create();
    private final LibraryListModel mModel = new LibraryListModel();

    private boolean mSearchIconified = true;

    private RecyclerView mRecyclerView;

    @Inject
    LibraryListPresenter presenter;

    @Inject
    LibraryPermissionsProvider libraryPermissionsProvider;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        configure();

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }
    }

    private void configure() {
        final Config config = obtainConfig();
        //noinspection ConstantConditions
        if (config == null) {
            throw new NullPointerException("Config must not be null");
        }

        mModel.setRecyclerAdapter(config.adapter);
        presenter.setCanShowEmptyView(config.canShowEmptyView);
        presenter.setDataSource(config.dataSource);
        mModel.setEmptyMessage(config.emptyMessage);
    }

    private void restoreInstanceState(@NonNull final Bundle savedInstanceState) {
        presenter.restoreInstanceState(savedInstanceState);

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
        presenter.onSaveInstanceState(outState);

        final InstanceState state = new InstanceState();
        state.searchIconified = mSearchIconified;
        state.searchQuery = mSearchProcessor.getValue();
        outState.putParcelable(KEY_INSTANCE_STATE, Parcels.wrap(state));
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_library_list, menu);
        if (libraryPermissionsProvider.permissionsGranted()) {
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

    @Nullable
    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        final FragmentLibraryListBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_library_list, container, false);
        setupRecyclerView(binding.recyclerView);
        initSwipeDirection(binding);
        binding.setModel(mModel);
        binding.getRoot().findViewById(R.id.btnRequest)
                .setOnClickListener(v -> presenter.requestPermission());
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
    }

    @NonNull
    public Observable<String> getSearchQuerySource() {
        return mSearchProcessor.toObservable();
    }

    @NonNull
    protected abstract Config obtainConfig();

    static final class Config {

        boolean canShowEmptyView = true;
        RecyclerView.Adapter<?> adapter;
        CharSequence emptyMessage;
        Function1<? super String, ? extends Observable<Cursor>> dataSource;
    }

    @Parcel
    static final class InstanceState {

        String searchQuery;
        boolean searchIconified;
    }

}
