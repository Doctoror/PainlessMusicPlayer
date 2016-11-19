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
package com.doctoror.fuckoffmusicplayer.base;

import com.doctoror.fuckoffmusicplayer.R;

import android.support.wearable.view.WearableListView;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Yaroslav Mytkalyk on 17.10.16.
 */
public final class TwoLineItemViewHolder extends WearableListView.ViewHolder {

    private final float mAlphaUnselected;
    private final float mAlphaSelected;

    public final TextView text1;
    public final TextView text2;

    public TwoLineItemViewHolder(final View itemView) {
        super(itemView);
        text1 = (TextView) itemView.findViewById(android.R.id.text1);
        text2 = (TextView) itemView.findViewById(android.R.id.text2);

        final TypedValue tv = new TypedValue();
        itemView.getResources().getValue(R.dimen.alpha_list_item_unselected, tv, false);
        mAlphaUnselected = tv.getFloat();

        itemView.getResources().getValue(R.dimen.alpha_list_item_selected, tv, false);
        mAlphaSelected = tv.getFloat();
    }

    @Override
    protected void onCenterProximity(final boolean isCentralItem, final boolean animate) {
        setAlpha(isCentralItem ? mAlphaSelected : mAlphaUnselected, animate);
    }

    private void setAlpha(final float alpha, final boolean animate) {
        if (animate) {
            text1.animate().alpha(alpha).start();
            text2.animate().alpha(alpha).start();
        } else {
            text1.setAlpha(alpha);
            text2.setAlpha(alpha);
        }
    }
}
