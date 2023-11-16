/*
 * Copyright (C) 2018 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.presentation.recentactivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doctoror.fuckoffmusicplayer.R
import com.doctoror.fuckoffmusicplayer.databinding.FragmentRecentActivityBinding
import com.doctoror.fuckoffmusicplayer.presentation.base.BaseFragment
import com.doctoror.fuckoffmusicplayer.presentation.util.AlbumArtIntoTargetApplier
import com.doctoror.fuckoffmusicplayer.presentation.util.ViewUtils
import com.doctoror.fuckoffmusicplayer.presentation.widget.SpacesItemDecoration
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class RecentActivityFragment : BaseFragment() {

    @Inject
    lateinit var albumArtIntoTargetApplier: AlbumArtIntoTargetApplier

    @Inject
    lateinit var presenter: RecentActivityPresenter

    @Inject
    lateinit var viewModel: RecentActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        setHasOptionsMenu(true)

        if (savedInstanceState != null) {
            presenter.restoreInstanceState(savedInstanceState)
        }

        lifecycle.addObserver(presenter)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(presenter)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentRecentActivityBinding>(
            inflater,
            R.layout.fragment_recent_activity, container, false
        )
        setupRecyclerView(binding.recyclerView)
        binding.model = viewModel
        binding.root.findViewById<View>(R.id.btnRequest).setOnClickListener {
            presenter.requestPermission()
        }

        val adapter = RecentActivityRecyclerAdapter(requireContext(), albumArtIntoTargetApplier)

        adapter.setOnAlbumClickListener { position, id, album ->
            presenter.onAlbumClick(id, album) {
                ViewUtils.getItemView(binding.recyclerView, position)
            }
        }

        viewModel.recyclerAdapter.set(adapter)

        return binding.root
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        val columns = resources.getInteger(R.integer.recent_activity_grid_columns)
        val lm =
            GridLayoutManager(activity, columns)
        lm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val adapter = recyclerView.adapter ?: return 1
                return when (adapter.getItemViewType(position)) {
                    RecentActivityRecyclerAdapter.VIEW_TYPE_HEADER -> columns
                    else -> 1
                }
            }
        }

        recyclerView.layoutManager = lm
        recyclerView.addItemDecoration(
            SpacesItemDecoration(
                resources.getDimensionPixelSize(R.dimen.recent_activity_grid_spacing)
            )
        )
    }
}
