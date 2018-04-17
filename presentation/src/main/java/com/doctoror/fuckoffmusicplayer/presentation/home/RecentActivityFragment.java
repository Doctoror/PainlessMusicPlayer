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
package com.doctoror.fuckoffmusicplayer.presentation.home;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.databinding.FragmentRecentActivityBinding;
import com.doctoror.fuckoffmusicplayer.presentation.base.BaseFragment;
import com.doctoror.fuckoffmusicplayer.presentation.util.ViewUtils;
import com.doctoror.fuckoffmusicplayer.presentation.widget.SpacesItemDecoration;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

public final class RecentActivityFragment extends BaseFragment {

    @Inject
    RecentActivityPresenter presenter;

    @Inject
    RecentActivityViewModel viewModel;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSupportInjection.inject(this);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            presenter.restoreInstanceState(savedInstanceState);
        }

        getLifecycle().addObserver(presenter);
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        presenter.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getLifecycle().removeObserver(presenter);
    }

    @NonNull
    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        final FragmentRecentActivityBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_recent_activity, container, false);
        setupRecyclerView(binding.recyclerView);
        binding.setModel(viewModel);
        binding.getRoot().findViewById(R.id.btnRequest)
                .setOnClickListener(v -> presenter.requestPermission());

        final RecentActivityRecyclerAdapter adapter =
                new RecentActivityRecyclerAdapter(requireContext());

        adapter.setOnAlbumClickListener((position, id, album) ->
                presenter.onAlbumClick(id, album,
                        () -> ViewUtils.getItemView(binding.recyclerView, position))
        );

        viewModel.getRecyclerAdapter().set(adapter);

        return binding.getRoot();
    }

    private void setupRecyclerView(@NonNull final RecyclerView recyclerView) {
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
}
