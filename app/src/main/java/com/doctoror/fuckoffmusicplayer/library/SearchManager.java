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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Observable;

/**
 * Created by Yaroslav Mytkalyk on 18.10.16.
 */

final class SearchManager extends Observable {

    private static final SearchManager INSTANCE = new SearchManager();

    @NonNull
    public static SearchManager getInstance() {
        return INSTANCE;
    }

    private String mSearchQuery;

    private SearchManager() {

    }

    public void updateQuery(@Nullable final String searchQuery) {
        if (!TextUtils.equals(mSearchQuery, searchQuery)) {
            mSearchQuery = searchQuery;
            setChanged();
            notifyObservers(searchQuery);
        }
    }

    @Nullable
    public String getSearchQuery() {
        return mSearchQuery;
    }
}
