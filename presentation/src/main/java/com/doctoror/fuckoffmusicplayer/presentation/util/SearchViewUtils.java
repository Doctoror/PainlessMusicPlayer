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
package com.doctoror.fuckoffmusicplayer.presentation.util;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.ImageView;

/**
 * {@link SearchView} utils
 */
public final class SearchViewUtils {

    private SearchViewUtils() {
        throw new UnsupportedOperationException();
    }

    public static void setSearchIcon(@NonNull final SearchView searchView,
            @DrawableRes final int icon) {
        View iconView = searchView
                .findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
        if (iconView instanceof ImageView) {
            ((ImageView) iconView).setImageResource(icon);
        }

        iconView = searchView
                .findViewById(android.support.v7.appcompat.R.id.search_button);
        if (iconView instanceof ImageView) {
            ((ImageView) iconView).setImageResource(icon);
        }
    }

}
