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
package com.doctoror.fuckoffmusicplayer.library;

import com.doctoror.commons.util.Log;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.databinding.FragmentLibraryListBinding;
import com.doctoror.fuckoffmusicplayer.util.ObserverAdapter;
import com.doctoror.fuckoffmusicplayer.util.SoftInputManager;
import com.doctoror.fuckoffmusicplayer.widget.SwipeDirectionTouchListener;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Fragment used for library list
 */
public abstract class LibraryListFragment extends Fragment {

    private static final String TAG = "LibraryListFragment";

    private static final int ANIMATOR_CHILD_PROGRESS = 0;
    private static final int ANIMATOR_CHILD_EMPTY = 1;
    private static final int ANIMATOR_CHILD_ERROR = 2;
    private static final int ANIMATOR_CHILD_CONTENT = 3;

    private final SearchSubject mSearchSubject = SearchSubject.getInstance();
    private Subscription mSearchSubscription;

    private final LibraryListFragmentModel mModel = new LibraryListFragmentModel();

    private Subscription mOldSubscription;
    private Subscription mSubscription;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        final FragmentLibraryListBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_library_list, container, false);
        setupRecyclerView(binding.recyclerView);
        binding.setModel(mModel);
        binding.recyclerView.setOnTouchListener(new SwipeDirectionTouchListener() {

            @Override
            protected void onSwipedDown() {
                SoftInputManager.hideSoftInput(getActivity());
            }
        });
        return binding.getRoot();
    }

    protected void setupRecyclerView(@NonNull final RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onStart() {
        super.onStart();
        mModel.setDisplayedChild(ANIMATOR_CHILD_PROGRESS);
        mSearchSubscription = mSearchSubject.asObservable().subscribe(mSearchQueryObserver);
    }

    @Override
    public void onStop() {
        super.onStop();
        onDataReset();
        if (mSearchSubscription != null) {
            mSearchSubscription.unsubscribe();
            mSearchSubscription = null;
        }

        if (mSubscription != null) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }

        if (mOldSubscription != null) {
            mOldSubscription.unsubscribe();
            mOldSubscription = null;
        }
    }

    private void restartLoader(@Nullable final String searchFilter) {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            mOldSubscription = mSubscription;
            mSubscription = load(searchFilter)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(mObserver);
        } else {
            Log.w(TAG, "restartLoader is called, READ_EXTERNAL_STORAGE is not granted");
        }
    }

    protected abstract Observable<Cursor> load(@Nullable final String filter);

    protected abstract void onDataLoaded(@Nullable Cursor data);

    protected abstract void onDataReset();

    protected final void setRecyclerAdapter(@Nullable final RecyclerView.Adapter<?> adapter) {
        mModel.setRecyclerAdapter(adapter);
    }

    protected final void setEmptyMessage(@Nullable final CharSequence emptyMessage) {
        mModel.setEmptyMessage(emptyMessage);
    }

    private final Observer<Cursor> mObserver = new ObserverAdapter<Cursor>() {

        @Override
        public void onError(final Throwable e) {
            if (mOldSubscription != null) {
                mOldSubscription.unsubscribe();
                mOldSubscription = null;
            }
            onDataReset();
            if (isAdded()) {
                mModel.setErrorText(getString(R.string.Failed_to_load_data_s, e));
                mModel.setDisplayedChild(ANIMATOR_CHILD_ERROR);
            }
        }

        @Override
        public void onNext(final Cursor cursor) {
            onDataLoaded(cursor);
            if (mOldSubscription != null) {
                mOldSubscription.unsubscribe();
                mOldSubscription = null;
            }
            mModel.setDisplayedChild(cursor != null && cursor.getCount() != 0
                    ? ANIMATOR_CHILD_CONTENT : ANIMATOR_CHILD_EMPTY);
        }
    };

    private final Action1<String> mSearchQueryObserver = this::restartLoader;

}
