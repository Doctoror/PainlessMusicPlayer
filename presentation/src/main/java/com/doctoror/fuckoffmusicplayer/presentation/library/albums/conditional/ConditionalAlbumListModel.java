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
package com.doctoror.fuckoffmusicplayer.presentation.library.albums.conditional;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;
import androidx.recyclerview.widget.RecyclerView;

/**
 * {@link ConditionalAlbumListFragment} model
 */
public final class ConditionalAlbumListModel {

    private final ObservableField<RecyclerView.Adapter> mRecyclerAdpter = new ObservableField<>();

    @NonNull
    public ObservableField<RecyclerView.Adapter> getRecyclerAdapter() {
        return mRecyclerAdpter;
    }

    void setRecyclerAdpter(@Nullable final RecyclerView.Adapter<?> adapter) {
        mRecyclerAdpter.set(adapter);
    }
}
