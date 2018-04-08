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
package com.doctoror.fuckoffmusicplayer.presentation.library

import android.annotation.SuppressLint
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.*
import com.doctoror.fuckoffmusicplayer.R
import com.doctoror.fuckoffmusicplayer.databinding.FragmentLibraryListBinding
import com.doctoror.fuckoffmusicplayer.presentation.base.BaseFragment
import com.doctoror.fuckoffmusicplayer.presentation.util.SearchViewUtils
import com.doctoror.fuckoffmusicplayer.presentation.util.SoftInputManager
import com.doctoror.fuckoffmusicplayer.presentation.util.ViewUtils
import com.doctoror.fuckoffmusicplayer.presentation.widget.SwipeDirectionTouchListener
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView
import io.reactivex.Observable
import io.reactivex.processors.BehaviorProcessor
import kotlinx.android.parcel.Parcelize
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Fragment used for library list
 */
abstract class LibraryListFragment : BaseFragment() {

    private val searchProcessor = BehaviorProcessor.create<String>()
    private val model = LibraryListModel()

    private var searchIconified = true

    private lateinit var binding: FragmentLibraryListBinding

    @Inject
    lateinit var presenter: LibraryListPresenter

    @Inject
    lateinit var libraryPermissionsProvider: LibraryPermissionsProvider

    val searchQuerySource: Observable<String>
        get() = searchProcessor.toObservable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        configure()

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState)
        }
    }

    private fun configure() {
        val config = obtainConfig()

        model.setRecyclerAdapter(config.adapter)
        presenter.canShowEmptyView = config.canShowEmptyView
        presenter.setDataSource(config.dataSource)
        model.setEmptyMessage(config.emptyMessage)
    }

    private fun restoreInstanceState(savedInstanceState: Bundle) {
        presenter.restoreInstanceState(savedInstanceState)

        val state = savedInstanceState.getParcelable<InstanceState>(KEY_INSTANCE_STATE)
        if (state != null) {
            searchIconified = state.searchIconified
            searchProcessor.onNext(state.searchQuery ?: "")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.onSaveInstanceState(outState)

        val state = InstanceState(searchIconified, searchProcessor.value)
        outState.putParcelable(KEY_INSTANCE_STATE, state)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_library_list, menu)
        if (libraryPermissionsProvider.permissionsGranted()) {
            val searchView = menu.findItem(R.id.actionFilter).actionView as SearchView
            SearchViewUtils.setSearchIcon(searchView, R.drawable.ic_filter_list_white_24dp)
            searchView.queryHint = getText(R.string.Filter)

            searchView.setQuery(searchProcessor.value, false)
            searchView.setOnCloseListener {
                searchIconified = true
                false
            }
            searchView.setOnSearchClickListener { v -> searchIconified = false }
            searchView.isIconified = searchIconified

            RxSearchView
                    .queryTextChanges(searchView)
                    .debounce(SEARCH_QUERY_DROP_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .subscribe { t -> searchProcessor.onNext(t.toString()) }

        } else {
            menu.findItem(R.id.actionFilter).isVisible = false
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<FragmentLibraryListBinding>(inflater,
                R.layout.fragment_library_list, container, false)

        setupRecyclerView(binding.recyclerView)
        initSwipeDirection(binding)

        binding.model = model
        binding.root.findViewById<View>(R.id.btnRequest)
                .setOnClickListener { v -> presenter.requestPermission() }

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initSwipeDirection(binding: FragmentLibraryListBinding) {
        binding.recyclerView.setOnTouchListener(object : SwipeDirectionTouchListener() {

            override fun onSwipedDown() {
                SoftInputManager.hideSoftInput(activity)
            }
        })
    }

    protected fun getItemView(position: Int): View? {
        return ViewUtils.getItemView(binding.recyclerView, position)
    }

    protected open fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(activity)
    }

    override fun onStop() {
        super.onStop()
        SoftInputManager.hideSoftInput(activity)
    }

    protected abstract fun obtainConfig(): Config

    data class Config(
            val canShowEmptyView: Boolean,
            val adapter: RecyclerView.Adapter<*>,
            val emptyMessage: CharSequence,
            val dataSource: LibraryDataSource)

    @Parcelize
    data class InstanceState(
            val searchIconified: Boolean,
            val searchQuery: String?) : Parcelable

    private companion object {

        private const val SEARCH_QUERY_DROP_TIMEOUT_MS = 400L
        private const val KEY_INSTANCE_STATE = "LibraryListFragment.INSTANCE_STATE"
    }
}
