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
package com.doctoror.fuckoffmusicplayer.wear.view;

import com.doctoror.fuckoffmusicplayer.R;

import android.support.wearable.view.WearableListView;
import android.util.TypedValue;
import android.view.View;

/**
 * {@link WearableListView.ViewHolder} that applies alpha to unselected items
 */
public abstract class AlphaSelectionViewHolder extends WearableListView.ViewHolder {

    private final float mAlphaUnselected;
    private final float mAlphaSelected;

    public AlphaSelectionViewHolder(final View itemView) {
        super(itemView);

        final TypedValue tv = new TypedValue();
        itemView.getResources().getValue(R.dimen.alpha_list_item_unselected, tv, false);
        mAlphaUnselected = tv.getFloat();

        itemView.getResources().getValue(R.dimen.alpha_list_item_selected, tv, false);
        mAlphaSelected = tv.getFloat();

        itemView.setAlpha(mAlphaUnselected);
    }

    @Override
    protected void onCenterProximity(final boolean isCentralItem, final boolean animate) {
        setAlpha(isCentralItem ? mAlphaSelected : mAlphaUnselected, animate);
    }

    private void setAlpha(final float alpha, final boolean animate) {
        if (animate) {
            itemView.animate().alpha(alpha).start();
        } else {
            itemView.setAlpha(alpha);
        }
    }
}
